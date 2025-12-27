"""
Classification result models.
"""

from typing import Optional

from pydantic import BaseModel, ConfigDict, Field

from focus_filter.models.notification import NotificationCategory


class ClassificationResult(BaseModel):
    """Result from the classification agent."""

    category: NotificationCategory = Field(..., description="Classified category")
    confidence: float = Field(..., ge=0.0, le=1.0, description="Confidence score (0.0 to 1.0)")
    reasoning: str = Field(..., description="Explanation for the classification")
    notification_id: Optional[str] = Field(None, description="ID of the classified notification")

    model_config = ConfigDict(use_enum_values=True)

