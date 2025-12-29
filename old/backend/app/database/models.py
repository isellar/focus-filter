"""
SQLAlchemy database models.
"""

from datetime import datetime
from typing import Optional

from sqlalchemy import Column, String, Float, Text, DateTime, JSON, Integer
from sqlalchemy.sql import func

from app.database.base import Base


class NotificationRecord(Base):
    """
    Database model for storing notification records.
    """

    __tablename__ = "notifications"

    id = Column(String, primary_key=True, index=True)
    title = Column(String, nullable=False, index=True)
    body = Column(Text, nullable=False)
    app_name = Column(String, nullable=False, index=True)
    package_name = Column(String, nullable=True)
    timestamp = Column(DateTime, default=datetime.utcnow, nullable=False, index=True)
    extras = Column(JSON, default=dict)
    created_at = Column(DateTime, default=datetime.utcnow, server_default=func.now())

    def __repr__(self) -> str:
        return f"<NotificationRecord(id={self.id}, title={self.title}, app={self.app_name})>"


class ProcessingResult(Base):
    """
    Database model for storing notification processing results.
    """

    __tablename__ = "processing_results"

    id = Column(Integer, primary_key=True, index=True, autoincrement=True)
    notification_id = Column(String, nullable=False, index=True)
    category = Column(String, nullable=False, index=True)  # URGENT, IRRELEVANT, LESS_URGENT
    confidence = Column(Float, nullable=False)
    reasoning = Column(Text, nullable=True)
    action_taken = Column(String, nullable=False)  # display_urgent_notification, block_notification, etc.
    extracted_facts = Column(JSON, default=list)
    created_at = Column(DateTime, default=datetime.utcnow, server_default=func.now())

    def __repr__(self) -> str:
        return (
            f"<ProcessingResult(id={self.id}, notification_id={self.notification_id}, "
            f"category={self.category}, action={self.action_taken})>"
        )

