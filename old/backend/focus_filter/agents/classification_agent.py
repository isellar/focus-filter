"""
Classification Agent - Analyzes notifications and determines urgency.
"""

import logging
from typing import Optional

from focus_filter.context.prompts import build_classification_context
from focus_filter.models.classification import ClassificationResult
from focus_filter.models.notification import Notification, NotificationCategory
from focus_filter.memory.manager import NotificationMemory

logger = logging.getLogger(__name__)


class ClassificationAgent:
    """
    Agent that classifies notifications into URGENT, IRRELEVANT, or LESS_URGENT categories.

    This agent uses Google ADK with Gemini to analyze notifications and determine
    their urgency level.
    """

    def __init__(self, api_key: Optional[str] = None, test_mode: bool = False):
        """
        Initialize the Classification Agent.

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
            logger.warning("ClassificationAgent running in test mode without API key")

        logger.info("ClassificationAgent initialized")

    def classify(
        self,
        notification: Notification,
        memory: Optional[NotificationMemory] = None,
    ) -> ClassificationResult:
        """
        Classify a notification into one of the categories.

        Args:
            notification: The notification to classify
            memory: Optional memory context for better classification

        Returns:
            ClassificationResult with category, confidence, and reasoning
        """
        logger.info(f"Classifying notification: {notification.title} from {notification.app_name}")

        # Build context for the agent
        context = build_classification_context(memory.get_memory() if memory else None)

        # Prepare the notification text for classification
        notification_text = f"""
Title: {notification.title}
Body: {notification.body}
App: {notification.app_name}
"""

        # TODO: Integrate with Google ADK for actual classification
        # For now, return a placeholder result
        # In the actual implementation, this would use Google ADK with Gemini
        # to analyze the notification and return a proper classification

        # Placeholder implementation - will be replaced with actual ADK integration
        category = self._simple_classify(notification)
        confidence = 0.8
        reasoning = f"Classified based on content analysis of '{notification.title}'"

        result = ClassificationResult(
            category=category,
            confidence=confidence,
            reasoning=reasoning,
            notification_id=notification.id,
        )

        logger.info(f"Classification result: {result.category} (confidence: {result.confidence})")
        return result

    def _simple_classify(self, notification: Notification) -> NotificationCategory:
        """
        Simple classification logic (placeholder until ADK integration).

        This is a temporary implementation for testing purposes.
        Will be replaced with actual Google ADK integration.

        Args:
            notification: The notification to classify

        Returns:
            NotificationCategory
        """
        # Simple keyword-based classification for testing
        urgent_keywords = ["urgent", "alert", "security", "meeting", "reminder", "important"]
        irrelevant_keywords = ["sale", "promotion", "discount", "spam", "ad"]

        text_lower = f"{notification.title} {notification.body}".lower()

        if any(keyword in text_lower for keyword in urgent_keywords):
            return NotificationCategory.URGENT
        elif any(keyword in text_lower for keyword in irrelevant_keywords):
            return NotificationCategory.IRRELEVANT
        else:
            return NotificationCategory.LESS_URGENT

