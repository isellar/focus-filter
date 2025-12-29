"""
Tests for data models.
"""

from datetime import datetime

import pytest

from focus_filter.models import (
    ClassificationResult,
    Memory,
    MemoryEntry,
    Notification,
    NotificationCategory,
)


def test_notification_model():
    """Test Notification model creation and validation."""
    notification = Notification(
        title="Test Notification",
        body="This is a test notification",
        app_name="TestApp",
        timestamp=datetime.now(),
    )

    assert notification.title == "Test Notification"
    assert notification.body == "This is a test notification"
    assert notification.app_name == "TestApp"
    assert notification.timestamp is not None
    assert notification.extras == {}


def test_notification_category_enum():
    """Test NotificationCategory enum values."""
    assert NotificationCategory.URGENT.value == "URGENT"
    assert NotificationCategory.IRRELEVANT.value == "IRRELEVANT"
    assert NotificationCategory.LESS_URGENT.value == "LESS_URGENT"


def test_classification_result_model():
    """Test ClassificationResult model."""
    result = ClassificationResult(
        category=NotificationCategory.URGENT,
        confidence=0.95,
        reasoning="This is urgent because...",
    )

    assert result.category == NotificationCategory.URGENT
    assert result.confidence == 0.95
    assert result.reasoning == "This is urgent because..."
    assert 0.0 <= result.confidence <= 1.0


def test_classification_result_confidence_validation():
    """Test that confidence is validated to be between 0 and 1."""
    # Valid confidence
    result = ClassificationResult(
        category=NotificationCategory.URGENT,
        confidence=0.5,
        reasoning="Test",
    )
    assert result.confidence == 0.5

    # Invalid confidence (should raise validation error)
    with pytest.raises(Exception):  # Pydantic validation error
        ClassificationResult(
            category=NotificationCategory.URGENT,
            confidence=1.5,  # Invalid: > 1.0
            reasoning="Test",
        )


def test_memory_entry_model():
    """Test MemoryEntry model."""
    entry = MemoryEntry(
        content="User has a meeting at 3pm",
        source_notification_id="notif-123",
    )

    assert entry.content == "User has a meeting at 3pm"
    assert entry.source_notification_id == "notif-123"
    assert entry.extracted_at is not None
    assert entry.tags == []
    assert 0.0 <= entry.importance <= 1.0


def test_memory_model():
    """Test Memory model and deduplication."""
    memory = Memory()

    entry1 = MemoryEntry(content="User has a meeting at 3pm")
    entry2 = MemoryEntry(content="User has a meeting at 3pm")  # Duplicate
    entry3 = MemoryEntry(content="User likes coffee")

    # Add first entry
    assert memory.add_entry(entry1) is True
    assert len(memory.entries) == 1

    # Try to add duplicate
    assert memory.add_entry(entry2) is False
    assert len(memory.entries) == 1  # Should not increase

    # Add different entry
    assert memory.add_entry(entry3) is True
    assert len(memory.entries) == 2


def test_memory_model_json_serialization():
    """Test that Memory model can be serialized to JSON."""
    memory = Memory()
    entry = MemoryEntry(content="Test memory")
    memory.add_entry(entry)

    # Should be able to convert to dict (for JSON serialization)
    memory_dict = memory.model_dump()
    assert "entries" in memory_dict
    assert len(memory_dict["entries"]) == 1

