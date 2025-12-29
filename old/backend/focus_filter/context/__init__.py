"""
Context building functions for agent prompts and few-shot examples.
"""

from focus_filter.context.prompts import (
    build_classification_context,
    build_memory_extraction_context,
)

__all__ = [
    "build_classification_context",
    "build_memory_extraction_context",
]
