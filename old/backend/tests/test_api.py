"""
Tests for API endpoints.
"""

import os
from datetime import datetime

import pytest
from fastapi.testclient import TestClient
from sqlalchemy import create_engine
from sqlalchemy.orm import sessionmaker

from app.api.auth import get_api_key
from app.database.base import Base, get_db
from app.main import app

# Create test database
TEST_DATABASE_URL = "sqlite:///./test_focus_filter.db"
test_engine = create_engine(TEST_DATABASE_URL, connect_args={"check_same_thread": False})
TestSessionLocal = sessionmaker(autocommit=False, autoflush=False, bind=test_engine)


@pytest.fixture(scope="function")
def db_session():
    """Create a test database session."""
    Base.metadata.create_all(bind=test_engine)
    db = TestSessionLocal()
    try:
        yield db
    finally:
        db.close()
        Base.metadata.drop_all(bind=test_engine)


@pytest.fixture(scope="function")
def client(db_session):
    """Create a test client with database override."""
    def override_get_db():
        try:
            yield db_session
        finally:
            pass

    def override_get_api_key():
        # Allow all requests in tests (no auth required)
        return "test_api_key"

    app.dependency_overrides[get_db] = override_get_db
    app.dependency_overrides[get_api_key] = override_get_api_key
    yield TestClient(app)
    app.dependency_overrides.clear()


def test_root_endpoint(client):
    """Test root health check endpoint."""
    response = client.get("/")
    assert response.status_code == 200
    data = response.json()
    assert data["status"] == "running"
    assert data["message"] == "Focus Filter API"


def test_health_endpoint(client):
    """Test health check endpoint."""
    response = client.get("/health")
    assert response.status_code == 200
    data = response.json()
    assert data["status"] == "healthy"
    assert "version" in data


def test_process_notification_endpoint(client):
    """Test notification processing endpoint."""
    request_data = {
        "title": "Test Notification",
        "body": "This is a test notification body",
        "app_name": "TestApp",
        "package_name": "com.test.app",
    }

    response = client.post("/api/v1/notifications/process", json=request_data)

    # Should succeed (even without API key, uses placeholder implementation)
    assert response.status_code == 200
    data = response.json()

    assert "notification_id" in data
    assert "classification" in data
    assert "action" in data
    assert "extracted_facts" in data
    assert "memory_count" in data

    assert "category" in data["classification"]
    assert "confidence" in data["classification"]
    assert "reasoning" in data["classification"]

    assert "action" in data["action"]
    assert "status" in data["action"]


def test_process_notification_urgent(client):
    """Test processing an urgent notification."""
    request_data = {
        "title": "Urgent: Security Alert",
        "body": "Your account has a suspicious login attempt",
        "app_name": "SecurityApp",
    }

    response = client.post("/api/v1/notifications/process", json=request_data)
    assert response.status_code == 200
    data = response.json()

    # Should be classified (may vary based on placeholder logic)
    assert data["classification"]["category"] in ["URGENT", "IRRELEVANT", "LESS_URGENT"]


def test_get_notification(client):
    """Test getting a notification by ID."""
    # First, create a notification
    request_data = {
        "title": "Test Notification",
        "body": "Test body",
        "app_name": "TestApp",
    }

    create_response = client.post("/api/v1/notifications/process", json=request_data)
    assert create_response.status_code == 200
    notification_id = create_response.json()["notification_id"]

    # Then retrieve it
    response = client.get(f"/api/v1/notifications/{notification_id}")
    assert response.status_code == 200
    data = response.json()

    assert "notification" in data
    assert data["notification"]["id"] == notification_id
    assert data["notification"]["title"] == "Test Notification"


def test_get_nonexistent_notification(client):
    """Test getting a notification that doesn't exist."""
    response = client.get("/api/v1/notifications/nonexistent-id")
    assert response.status_code == 404
    assert "not found" in response.json()["detail"].lower()


def test_list_notifications(client):
    """Test listing notifications."""
    # Create a few notifications
    for i in range(3):
        request_data = {
            "title": f"Notification {i}",
            "body": f"Body {i}",
            "app_name": "TestApp",
        }
        client.post("/api/v1/notifications/process", json=request_data)

    # List them
    response = client.get("/api/v1/notifications/")
    assert response.status_code == 200
    data = response.json()

    assert "notifications" in data
    assert "total" in data
    assert len(data["notifications"]) >= 3
    assert data["total"] >= 3


def test_list_notifications_pagination(client):
    """Test listing notifications with pagination."""
    # Create notifications
    for i in range(5):
        request_data = {
            "title": f"Notification {i}",
            "body": f"Body {i}",
            "app_name": "TestApp",
        }
        client.post("/api/v1/notifications/process", json=request_data)

    # List with limit
    response = client.get("/api/v1/notifications/?skip=0&limit=2")
    assert response.status_code == 200
    data = response.json()

    assert len(data["notifications"]) == 2
    assert data["skip"] == 0
    assert data["limit"] == 2


def test_classify_endpoint(client):
    """Test the classify endpoint."""
    request_data = {
        "title": "Urgent: Security Alert",
        "body": "Your account has a suspicious login attempt",
        "app_name": "SecurityApp",
    }

    response = client.post("/api/v1/notifications/classify", json=request_data)
    assert response.status_code == 200
    data = response.json()

    assert "notification_id" in data
    assert "category" in data
    assert "confidence" in data
    assert "reasoning" in data
    assert data["category"] in ["URGENT", "IRRELEVANT", "LESS_URGENT"]
    assert 0.0 <= data["confidence"] <= 1.0

