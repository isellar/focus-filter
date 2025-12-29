"""
NotificationMemory class for managing notification memories with deduplication.
"""

import logging
from typing import Optional

from focus_filter.models.memory import Memory, MemoryEntry

logger = logging.getLogger(__name__)


class NotificationMemory:
    """
    Manages notification memories with deduplication and retrieval.

    This class handles storing, retrieving, and managing memory entries
    extracted from notifications.
    """

    def __init__(self):
        """Initialize an empty NotificationMemory instance."""
        self._memory = Memory()
        logger.info("NotificationMemory initialized")

    def add_memory(self, content: str, source_notification_id: Optional[str] = None, tags: Optional[list[str]] = None) -> bool:
        """
        Add a memory entry with deduplication.

        Args:
            content: The memory content to store
            source_notification_id: Optional ID of the source notification
            tags: Optional list of tags for categorization

        Returns:
            True if memory was added, False if it was a duplicate
        """
        entry = MemoryEntry(
            content=content,
            source_notification_id=source_notification_id,
            tags=tags or [],
        )

        added = self._memory.add_entry(entry)
        if added:
            logger.debug(f"Added memory entry: {content[:50]}...")
        else:
            logger.debug(f"Duplicate memory entry skipped: {content[:50]}...")

        return added

    def get_recent_memories(self, limit: int = 10) -> list[MemoryEntry]:
        """
        Get the most recent memory entries.

        Args:
            limit: Maximum number of entries to return

        Returns:
            List of recent MemoryEntry objects
        """
        entries = self._memory.entries[-limit:]
        logger.debug(f"Retrieved {len(entries)} recent memory entries")
        return entries

    def search_memories(self, query: str) -> list[MemoryEntry]:
        """
        Search memory entries by content.

        Args:
            query: Search query (case-insensitive substring match)

        Returns:
            List of matching MemoryEntry objects
        """
        query_lower = query.lower()
        matches = [
            entry
            for entry in self._memory.entries
            if query_lower in entry.content.lower()
        ]
        logger.debug(f"Found {len(matches)} memory entries matching '{query}'")
        return matches

    def get_memory_by_tags(self, tags: list[str]) -> list[MemoryEntry]:
        """
        Get memory entries that have any of the specified tags.

        Args:
            tags: List of tags to search for

        Returns:
            List of MemoryEntry objects with matching tags
        """
        tag_set = set(tags)
        matches = [
            entry
            for entry in self._memory.entries
            if tag_set.intersection(set(entry.tags))
        ]
        logger.debug(f"Found {len(matches)} memory entries with tags {tags}")
        return matches

    def get_all_memories(self) -> list[MemoryEntry]:
        """
        Get all memory entries.

        Returns:
            List of all MemoryEntry objects
        """
        return self._memory.entries.copy()

    def clear_memories(self) -> None:
        """Clear all memory entries."""
        self._memory.entries.clear()
        logger.info("All memories cleared")

    def get_memory_count(self) -> int:
        """
        Get the total number of memory entries.

        Returns:
            Number of memory entries
        """
        return len(self._memory.entries)

    def get_memory(self) -> Memory:
        """
        Get the underlying Memory object.

        Returns:
            The Memory object
        """
        return self._memory

