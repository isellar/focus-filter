"""
Orchestrator for the multi-agent notification processing pipeline.
"""

import logging
from typing import Optional

from focus_filter.agents.action_agent import ActionAgent
from focus_filter.agents.classification_agent import ClassificationAgent
from focus_filter.agents.memory_agent import MemoryAgent
from focus_filter.models.classification import ClassificationResult
from focus_filter.models.notification import Notification
from focus_filter.memory.manager import NotificationMemory

logger = logging.getLogger(__name__)


def process_notification_multi_agent(
    notification: Notification,
    memory: Optional[NotificationMemory] = None,
    api_key: Optional[str] = None,
    test_mode: bool = False,
) -> dict:
    """
    Process a notification through the multi-agent pipeline.

    The pipeline consists of three sequential agents:
    1. Classification Agent: Analyzes and classifies the notification
    2. Memory Agent: Extracts key facts (if needed)
    3. Action Agent: Executes the appropriate action based on classification

    Args:
        notification: The notification to process
        memory: Optional NotificationMemory instance for context and storage
        api_key: Optional Google API key for agents

    Returns:
        dict with processing results including classification, extracted facts, and action result
    """
    logger.info(f"Processing notification: {notification.title} from {notification.app_name}")

    # Initialize memory if not provided
    if memory is None:
        memory = NotificationMemory()

    # Initialize agents
    classification_agent = ClassificationAgent(api_key=api_key, test_mode=test_mode)
    memory_agent = MemoryAgent(api_key=api_key, test_mode=test_mode)
    action_agent = ActionAgent()

    # Step 1: Classification Agent
    logger.debug("Step 1: Running Classification Agent")
    classification = classification_agent.classify(notification, memory=memory)

    # Step 2: Memory Agent (extract facts if needed)
    logger.debug("Step 2: Running Memory Agent")
    extracted_facts = []
    # Handle both enum and string values
    category_value = (
        classification.category.value
        if hasattr(classification.category, "value")
        else str(classification.category)
    )
    if category_value in ["LESS_URGENT", "URGENT"]:
        # Extract memory for less urgent or urgent notifications
        extracted_facts = memory_agent.extract_memory(notification, memory)
    else:
        logger.debug("Skipping memory extraction for IRRELEVANT notifications")

    # Step 3: Action Agent
    logger.debug("Step 3: Running Action Agent")
    action_result = action_agent.execute_action(
        notification,
        classification,
        extracted_facts=extracted_facts if extracted_facts else None,
    )

    # Compile results
    result = {
        "notification_id": notification.id,
        "classification": {
            "category": category_value,
            "confidence": classification.confidence,
            "reasoning": classification.reasoning,
        },
        "extracted_facts": extracted_facts,
        "action": action_result,
        "memory_count": memory.get_memory_count(),
    }

    logger.info(
        f"Processing complete: {category_value} -> {action_result['action']}"
    )

    return result

