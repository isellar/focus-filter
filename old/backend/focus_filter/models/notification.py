"""
Notification data models.
"""

from datetime import datetime
from enum import Enum
from typing import Optional

from pydantic import BaseModel, ConfigDict, Field


class NotificationCategory(str, Enum):
    """Classification categories for notifications."""

    URGENT = "URGENT"
    IRRELEVANT = "IRRELEVANT"
    LESS_URGENT = "LESS_URGENT"


class Notification(BaseModel):
    """Represents a notification to be classified and processed."""

    id: Optional[str] = Field(None, description="Unique identifier for the notification")
    title: str = Field(..., description="Notification title")
    body: str = Field(..., description="Notification body/content")
    app_name: str = Field(..., description="Name of the app that generated the notification")
    timestamp: datetime = Field(default_factory=datetime.now, description="When the notification was received")
    package_name: Optional[str] = Field(None, description="Android package name")
    extras: Optional[dict] = Field(default_factory=dict, description="Additional notification metadata")

    model_config = ConfigDict(
        json_encoders={
            datetime: lambda v: v.isoformat(),
        },
        use_enum_values=True,
    )

