"""
Tests for memory management.
"""

from focus_filter.memory.manager import NotificationMemory


def test_notification_memory_initialization():
    """Test NotificationMemory initialization."""
    memory = NotificationMemory()
    assert memory.get_memory_count() == 0


def test_add_memory():
    """Test adding memory entries."""
    memory = NotificationMemory()

    # Add first memory
    assert memory.add_memory("User has a meeting at 3pm", source_notification_id="notif-1") is True
    assert memory.get_memory_count() == 1

    # Try to add duplicate
    assert memory.add_memory("User has a meeting at 3pm") is False
    assert memory.get_memory_count() == 1  # Should not increase

    # Add different memory
    assert memory.add_memory("User likes coffee") is True
    assert memory.get_memory_count() == 2


def test_get_recent_memories():
    """Test retrieving recent memories."""
    memory = NotificationMemory()

    memory.add_memory("Memory 1")
    memory.add_memory("Memory 2")
    memory.add_memory("Memory 3")

    recent = memory.get_recent_memories(limit=2)
    assert len(recent) == 2
    assert recent[-1].content == "Memory 3"


def test_search_memories():
    """Test searching memories."""
    memory = NotificationMemory()

    memory.add_memory("User has a meeting at 3pm")
    memory.add_memory("User likes coffee")
    memory.add_memory("Meeting with John tomorrow")

    results = memory.search_memories("meeting")
    assert len(results) == 2
    assert any("3pm" in entry.content for entry in results)
    assert any("John" in entry.content for entry in results)


def test_get_memory_by_tags():
    """Test retrieving memories by tags."""
    memory = NotificationMemory()

    memory.add_memory("Meeting at 3pm", tags=["meeting", "urgent"])
    memory.add_memory("Coffee break", tags=["break"])
    memory.add_memory("Another meeting", tags=["meeting"])

    results = memory.get_memory_by_tags(["meeting"])
    assert len(results) == 2

    results = memory.get_memory_by_tags(["urgent"])
    assert len(results) == 1


def test_clear_memories():
    """Test clearing all memories."""
    memory = NotificationMemory()

    memory.add_memory("Memory 1")
    memory.add_memory("Memory 2")
    assert memory.get_memory_count() == 2

    memory.clear_memories()
    assert memory.get_memory_count() == 0

