"""
Tests for agent classes and orchestrator.
"""

import os
from datetime import datetime

import pytest

from focus_filter.agents import (
    ActionAgent,
    ClassificationAgent,
    MemoryAgent,
    process_notification_multi_agent,
)
from focus_filter.models.notification import Notification, NotificationCategory
from focus_filter.memory.manager import NotificationMemory


@pytest.fixture
def mock_api_key():
    """Fixture for mock API key."""
    return "test-api-key" if os.getenv("GOOGLE_API_KEY") else "test-api-key"


@pytest.fixture
def sample_notification():
    """Fixture for a sample notification."""
    return Notification(
        id="test-1",
        title="Urgent Meeting Reminder",
        body="You have a meeting in 5 minutes",
        app_name="Calendar",
        timestamp=datetime.now(),
    )


def test_classification_agent_initialization(mock_api_key):
    """Test ClassificationAgent initialization."""
    # This will fail if GOOGLE_API_KEY is not set, which is expected
    # In a real test environment, we'd mock the API key
    if not os.getenv("GOOGLE_API_KEY"):
        with pytest.raises(ValueError, match="Google API key is required"):
            ClassificationAgent(api_key=None)
    else:
        agent = ClassificationAgent()
        assert agent is not None


def test_classification_agent_classify(sample_notification, mock_api_key):
    """Test classification agent classification."""
    if not os.getenv("GOOGLE_API_KEY"):
        pytest.skip("GOOGLE_API_KEY not set")

    agent = ClassificationAgent(api_key=mock_api_key)
    result = agent.classify(sample_notification)

    assert result.category in NotificationCategory
    assert 0.0 <= result.confidence <= 1.0
    assert result.reasoning is not None


def test_action_agent_initialization():
    """Test ActionAgent initialization."""
    agent = ActionAgent()
    assert agent is not None


def test_action_agent_execute_urgent(sample_notification):
    """Test ActionAgent with URGENT classification."""
    from focus_filter.models.classification import ClassificationResult

    agent = ActionAgent()
    classification = ClassificationResult(
        category=NotificationCategory.URGENT,
        confidence=0.9,
        reasoning="Time-sensitive",
    )

    result = agent.execute_action(sample_notification, classification)
    assert result["action"] == "display_urgent_notification"
    assert result["status"] == "success"


def test_action_agent_execute_irrelevant(sample_notification):
    """Test ActionAgent with IRRELEVANT classification."""
    from focus_filter.models.classification import ClassificationResult

    agent = ActionAgent()
    classification = ClassificationResult(
        category=NotificationCategory.IRRELEVANT,
        confidence=0.8,
        reasoning="Spam",
    )

    result = agent.execute_action(sample_notification, classification)
    assert result["action"] == "block_notification"
    assert result["status"] == "success"


def test_action_agent_execute_less_urgent(sample_notification):
    """Test ActionAgent with LESS_URGENT classification."""
    from focus_filter.models.classification import ClassificationResult

    agent = ActionAgent()
    classification = ClassificationResult(
        category=NotificationCategory.LESS_URGENT,
        confidence=0.7,
        reasoning="Informative",
    )

    result = agent.execute_action(sample_notification, classification, extracted_facts=["Fact 1"])
    assert result["action"] == "save_notification_memory"
    assert result["status"] == "success"


def test_memory_agent_initialization(mock_api_key):
    """Test MemoryAgent initialization."""
    if not os.getenv("GOOGLE_API_KEY"):
        with pytest.raises(ValueError, match="Google API key is required"):
            MemoryAgent(api_key=None)
    else:
        agent = MemoryAgent()
        assert agent is not None


def test_memory_agent_extract_memory(sample_notification, mock_api_key):
    """Test MemoryAgent memory extraction."""
    if not os.getenv("GOOGLE_API_KEY"):
        pytest.skip("GOOGLE_API_KEY not set")

    memory = NotificationMemory()
    agent = MemoryAgent(api_key=mock_api_key)
    facts = agent.extract_memory(sample_notification, memory)

    assert isinstance(facts, list)
    assert len(facts) > 0
    assert memory.get_memory_count() > 0


def test_process_notification_multi_agent(sample_notification, mock_api_key):
    """Test the full multi-agent pipeline."""
    if not os.getenv("GOOGLE_API_KEY"):
        pytest.skip("GOOGLE_API_KEY not set")

    memory = NotificationMemory()
    result = process_notification_multi_agent(
        sample_notification,
        memory=memory,
        api_key=mock_api_key,
    )

    assert "classification" in result
    assert "action" in result
    assert "extracted_facts" in result
    assert result["notification_id"] == sample_notification.id
    assert "category" in result["classification"]
    assert "action" in result["action"]

