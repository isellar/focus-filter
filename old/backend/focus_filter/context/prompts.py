"""
Context and prompt building functions for agents.
"""

from typing import Optional

from focus_filter.models.memory import Memory


def build_classification_context(memory: Optional[Memory] = None) -> str:
    """
    Build context for the classification agent with few-shot examples.

    Args:
        memory: Optional memory entries to include for context

    Returns:
        Formatted context string for the classification agent
    """
    context = """You are a notification classification agent. Your task is to analyze notifications and classify them into one of three categories:

1. URGENT: Notifications that require immediate attention (e.g., security alerts, time-sensitive messages, important reminders)
2. IRRELEVANT: Notifications that are spam, ads, or not relevant to the user (e.g., promotional emails, game notifications)
3. LESS_URGENT: Notifications that are informative but don't require immediate action (e.g., news updates, social media likes)

Few-shot examples:

Example 1:
Notification: "Your bank account has a suspicious login attempt"
Classification: URGENT
Reasoning: Security-related notifications require immediate attention

Example 2:
Notification: "50% off sale! Limited time only!"
Classification: IRRELEVANT
Reasoning: Promotional content that is not relevant to user's immediate needs

Example 3:
Notification: "New article published: Tech trends 2024"
Classification: LESS_URGENT
Reasoning: Informative content that can be reviewed later

Example 4:
Notification: "Meeting reminder: Team standup in 5 minutes"
Classification: URGENT
Reasoning: Time-sensitive reminder requires immediate attention

Example 5:
Notification: "You have 3 new likes on your post"
Classification: IRRELEVANT
Reasoning: Social media engagement is not urgent

Example 6:
Notification: "Weather update: Sunny, 72°F"
Classification: LESS_URGENT
Reasoning: Informative but not time-critical
"""

    # Add memory context if available
    if memory and memory.entries:
        context += "\n\nRelevant context from previous notifications:\n"
        for entry in memory.entries[-5:]:  # Last 5 entries
            context += f"- {entry.content}\n"

    return context


def build_memory_extraction_context() -> str:
    """
    Build context for the memory extraction agent.

    Returns:
        Formatted context string for the memory extraction agent
    """
    context = """You are a memory extraction agent. Your task is to extract key facts and information from notifications that should be remembered for future context.

Guidelines:
- Extract factual information (dates, times, names, events)
- Extract user preferences and patterns
- Extract important relationships or connections
- Avoid extracting redundant or obvious information
- Format facts as concise, standalone statements

Examples:

Notification: "Meeting with John at 3pm tomorrow in Conference Room A"
Extracted facts:
- User has a meeting with John
- Meeting scheduled for tomorrow at 3pm
- Location: Conference Room A

Notification: "Your package from Amazon will arrive on Friday"
Extracted facts:
- User has a package delivery expected
- Delivery date: Friday
- Source: Amazon

Notification: "You've been subscribed to TechNews newsletter"
Extracted facts:
- User subscribed to TechNews newsletter
- Indicates interest in tech news
"""

    return context

