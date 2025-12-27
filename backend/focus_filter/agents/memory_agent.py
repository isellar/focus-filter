"""
Memory Agent - Handles memory extraction and storage.
"""

import logging
from typing import Optional

from focus_filter.context.prompts import build_memory_extraction_context
from focus_filter.models.notification import Notification
from focus_filter.memory.manager import NotificationMemory

logger = logging.getLogger(__name__)


class MemoryAgent:
    """
    Agent that extracts key facts from notifications and stores them in memory.

    This agent uses Google ADK with Gemini to extract relevant information
    from notifications for future context.
    """

    def __init__(self, api_key: Optional[str] = None, test_mode: bool = False):
        """
        Initialize the Memory Agent.

        Args:
            api_key: Optional Google API key. If not provided, reads from GOOGLE_API_KEY env var.
            test_mode: If True, allows operation without API key (uses placeholder implementation)
        """
        import os

        self.api_key = api_key or os.getenv("GOOGLE_API_KEY")
        self.test_mode = test_mode

        if not self.api_key and not test_mode:
            raise ValueError("Google API key is required. Set GOOGLE_API_KEY environment variable.")

        if test_mode and not self.api_key:
            logger.warning("MemoryAgent running in test mode without API key")

        logger.info("MemoryAgent initialized")

    def extract_memory(
        self,
        notification: Notification,
        memory: NotificationMemory,
    ) -> list[str]:
        """
        Extract key facts from a notification and store them in memory.

        Args:
            notification: The notification to extract facts from
            memory: The NotificationMemory instance to store facts in

        Returns:
            List of extracted fact strings
        """
        logger.info(f"Extracting memory from notification: {notification.title}")

        # Build context for extraction
        context = build_memory_extraction_context()

        # Prepare the notification text
        notification_text = f"""
Title: {notification.title}
Body: {notification.body}
App: {notification.app_name}
"""

        # TODO: Integrate with Google ADK for actual memory extraction
        # For now, return a placeholder implementation
        # In the actual implementation, this would use Google ADK with Gemini
        # to extract relevant facts from the notification

        # Placeholder implementation - will be replaced with actual ADK integration
        extracted_facts = self._simple_extract(notification)

        # Store extracted facts in memory
        for fact in extracted_facts:
            memory.add_memory(
                content=fact,
                source_notification_id=notification.id,
                tags=self._extract_tags(notification),
            )

        logger.info(f"Extracted {len(extracted_facts)} facts from notification")
        return extracted_facts

    def _simple_extract(self, notification: Notification) -> list[str]:
        """
        Simple memory extraction logic (placeholder until ADK integration).

        This is a temporary implementation for testing purposes.
        Will be replaced with actual Google ADK integration.

        Args:
            notification: The notification to extract from

        Returns:
            List of extracted fact strings
        """
        facts = []

        # Extract basic information
        if "meeting" in notification.body.lower() or "meeting" in notification.title.lower():
            facts.append(f"User has a meeting: {notification.title}")

        if "package" in notification.body.lower() or "delivery" in notification.body.lower():
            facts.append(f"User has a delivery: {notification.body[:50]}")

        # If no specific facts found, store a general fact
        if not facts:
            facts.append(f"Notification from {notification.app_name}: {notification.title}")

        return facts

    def _extract_tags(self, notification: Notification) -> list[str]:
        """
        Extract tags from notification for categorization.

        Args:
            notification: The notification to extract tags from

        Returns:
            List of tag strings
        """
        tags = [notification.app_name.lower()]

        text_lower = f"{notification.title} {notification.body}".lower()

        if "meeting" in text_lower:
            tags.append("meeting")
        if "package" in text_lower or "delivery" in text_lower:
            tags.append("delivery")
        if "reminder" in text_lower:
            tags.append("reminder")

        return tags

