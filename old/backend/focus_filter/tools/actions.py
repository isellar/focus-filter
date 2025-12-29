"""
Action tools for agents to execute based on classification results.
"""

import logging
from typing import Optional

from focus_filter.models.notification import Notification

logger = logging.getLogger(__name__)


def display_urgent_notification(notification: Notification, reason: Optional[str] = None) -> dict:
    """
    Display an urgent notification to the user.

    This tool is called when a notification is classified as URGENT.
    The notification should be shown immediately to the user.

    Args:
        notification: The notification to display
        reason: Optional reason why this is urgent

    Returns:
        dict with action result and status
    """
    logger.info(
        f"Displaying urgent notification: {notification.title} from {notification.app_name}",
        extra={
            "notification_id": notification.id,
            "app_name": notification.app_name,
            "reason": reason,
        },
    )

    # In a real implementation, this would trigger the Android notification display
    # For now, we log the action
    return {
        "action": "display_urgent_notification",
        "status": "success",
        "notification_id": notification.id,
        "title": notification.title,
        "app_name": notification.app_name,
        "reason": reason,
    }


def block_notification(notification: Notification, reason: Optional[str] = None) -> dict:
    """
    Block/suppress an irrelevant notification.

    This tool is called when a notification is classified as IRRELEVANT.
    The notification should be suppressed and not shown to the user.

    Args:
        notification: The notification to block
        reason: Optional reason why this is irrelevant

    Returns:
        dict with action result and status
    """
    logger.info(
        f"Blocking notification: {notification.title} from {notification.app_name}",
        extra={
            "notification_id": notification.id,
            "app_name": notification.app_name,
            "reason": reason,
        },
    )

    # In a real implementation, this would suppress the notification
    # For now, we log the action
    return {
        "action": "block_notification",
        "status": "success",
        "notification_id": notification.id,
        "title": notification.title,
        "app_name": notification.app_name,
        "reason": reason,
    }


def save_notification_memory(
    notification: Notification,
    extracted_facts: Optional[list[str]] = None,
    reason: Optional[str] = None,
) -> dict:
    """
    Save a notification to memory for later reference.

    This tool is called when a notification is classified as LESS_URGENT.
    The notification content is stored in memory for future context.

    Args:
        notification: The notification to save
        extracted_facts: Optional list of facts extracted from the notification
        reason: Optional reason why this is less urgent

    Returns:
        dict with action result and status
    """
    logger.info(
        f"Saving notification to memory: {notification.title} from {notification.app_name}",
        extra={
            "notification_id": notification.id,
            "app_name": notification.app_name,
            "reason": reason,
            "extracted_facts_count": len(extracted_facts) if extracted_facts else 0,
        },
    )

    # In a real implementation, this would store the notification in the memory system
    # For now, we log the action
    return {
        "action": "save_notification_memory",
        "status": "success",
        "notification_id": notification.id,
        "title": notification.title,
        "app_name": notification.app_name,
        "extracted_facts": extracted_facts or [],
        "reason": reason,
    }

