"""
Action Agent - Executes actions based on classification results.
"""

import logging
from typing import Optional

from focus_filter.models.classification import ClassificationResult
from focus_filter.models.notification import Notification
from focus_filter.tools.actions import (
    block_notification,
    display_urgent_notification,
    save_notification_memory,
)

logger = logging.getLogger(__name__)


class ActionAgent:
    """
    Agent that executes actions based on classification results.

    This agent calls the appropriate tool function based on the classification category.
    """

    def __init__(self):
        """Initialize the Action Agent."""
        logger.info("ActionAgent initialized")

    def execute_action(
        self,
        notification: Notification,
        classification: ClassificationResult,
        extracted_facts: Optional[list[str]] = None,
    ) -> dict:
        """
        Execute the appropriate action based on classification.

        Args:
            notification: The notification to act upon
            classification: The classification result
            extracted_facts: Optional facts extracted from the notification

        Returns:
            dict with action result
        """
        logger.info(
            f"Executing action for notification {notification.id} "
            f"with category {classification.category}"
        )

        # Handle both enum and string values (Pydantic with use_enum_values=True converts to string)
        category_value = (
            classification.category.value
            if hasattr(classification.category, "value")
            else str(classification.category)
        )

        if category_value == "URGENT":
            return display_urgent_notification(
                notification,
                reason=classification.reasoning,
            )
        elif category_value == "IRRELEVANT":
            return block_notification(
                notification,
                reason=classification.reasoning,
            )
        elif category_value == "LESS_URGENT":
            return save_notification_memory(
                notification,
                extracted_facts=extracted_facts,
                reason=classification.reasoning,
            )
        else:
            logger.warning(f"Unknown category: {classification.category}")
            return {
                "action": "unknown",
                "status": "error",
                "message": f"Unknown category: {classification.category}",
            }

