"""
Tests for tool functions.
"""

from datetime import datetime

from focus_filter.models.notification import Notification
from focus_filter.tools.actions import (
    block_notification,
    display_urgent_notification,
    save_notification_memory,
)


def test_display_urgent_notification():
    """Test display_urgent_notification tool."""
    notification = Notification(
        id="test-1",
        title="Urgent Alert",
        body="This is urgent",
        app_name="TestApp",
        timestamp=datetime.now(),
    )

    result = display_urgent_notification(notification, reason="Time-sensitive")

    assert result["action"] == "display_urgent_notification"
    assert result["status"] == "success"
    assert result["notification_id"] == "test-1"
    assert result["title"] == "Urgent Alert"
    assert result["app_name"] == "TestApp"
    assert result["reason"] == "Time-sensitive"


def test_block_notification():
    """Test block_notification tool."""
    notification = Notification(
        id="test-2",
        title="Spam Notification",
        body="This is spam",
        app_name="SpamApp",
        timestamp=datetime.now(),
    )

    result = block_notification(notification, reason="Irrelevant content")

    assert result["action"] == "block_notification"
    assert result["status"] == "success"
    assert result["notification_id"] == "test-2"
    assert result["title"] == "Spam Notification"
    assert result["reason"] == "Irrelevant content"


def test_save_notification_memory():
    """Test save_notification_memory tool."""
    notification = Notification(
        id="test-3",
        title="News Update",
        body="Some news content",
        app_name="NewsApp",
        timestamp=datetime.now(),
    )

    facts = ["User likes tech news", "Update received at 3pm"]
    result = save_notification_memory(notification, extracted_facts=facts, reason="Not urgent")

    assert result["action"] == "save_notification_memory"
    assert result["status"] == "success"
    assert result["notification_id"] == "test-3"
    assert result["extracted_facts"] == facts
    assert result["reason"] == "Not urgent"


def test_save_notification_memory_without_facts():
    """Test save_notification_memory tool without extracted facts."""
    notification = Notification(
        id="test-4",
        title="Regular Update",
        body="Some content",
        app_name="App",
        timestamp=datetime.now(),
    )

    result = save_notification_memory(notification)

    assert result["action"] == "save_notification_memory"
    assert result["extracted_facts"] == []
    assert result["status"] == "success"

