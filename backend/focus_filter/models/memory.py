"""
Memory data models.
"""

from datetime import datetime
from typing import Optional

from pydantic import BaseModel, ConfigDict, Field


class MemoryEntry(BaseModel):
    """A single memory entry extracted from a notification."""

    id: Optional[str] = Field(None, description="Unique identifier for the memory entry")
    content: str = Field(..., description="The memory content/fact")
    source_notification_id: Optional[str] = Field(None, description="ID of the source notification")
    extracted_at: datetime = Field(default_factory=datetime.now, description="When the memory was extracted")
    tags: list[str] = Field(default_factory=list, description="Tags for categorization")
    importance: float = Field(default=0.5, ge=0.0, le=1.0, description="Importance score")

    model_config = ConfigDict(
        json_encoders={
            datetime: lambda v: v.isoformat(),
        },
    )


class Memory(BaseModel):
    """Collection of memory entries with deduplication support."""

    entries: list[MemoryEntry] = Field(default_factory=list, description="List of memory entries")
    last_updated: datetime = Field(default_factory=datetime.now, description="Last update timestamp")

    def add_entry(self, entry: MemoryEntry) -> bool:
        """
        Add a memory entry if it doesn't already exist (deduplication).

        Args:
            entry: The memory entry to add

        Returns:
            True if entry was added, False if it was a duplicate
        """
        # Simple deduplication: check if similar content exists
        for existing in self.entries:
            if existing.content.lower().strip() == entry.content.lower().strip():
                return False

        self.entries.append(entry)
        self.last_updated = datetime.now()
        return True

    model_config = ConfigDict(
        json_encoders={
            datetime: lambda v: v.isoformat(),
        },
    )

