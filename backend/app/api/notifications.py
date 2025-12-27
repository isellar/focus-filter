"""
Notification processing API endpoints.
"""

import logging
import os
from datetime import datetime
from typing import Optional

from fastapi import APIRouter, Depends, HTTPException, status
from pydantic import BaseModel, Field
from sqlalchemy.orm import Session

from app.api.auth import get_api_key
from app.database.base import get_db
from app.database.models import NotificationRecord, ProcessingResult
from focus_filter.agents import process_notification_multi_agent
from focus_filter.agents.classification_agent import ClassificationAgent
from focus_filter.memory.manager import NotificationMemory
from focus_filter.models.notification import Notification

logger = logging.getLogger(__name__)

router = APIRouter(prefix="/api/v1/notifications", tags=["notifications"])


class NotificationRequest(BaseModel):
    """Request model for processing a notification."""

    title: str = Field(..., description="Notification title")
    body: str = Field(..., description="Notification body/content")
    app_name: str = Field(..., description="Name of the app that generated the notification")
    package_name: Optional[str] = Field(None, description="Android package name")
    timestamp: Optional[datetime] = Field(default_factory=datetime.now, description="When the notification was received")
    extras: Optional[dict] = Field(default_factory=dict, description="Additional notification metadata")


class ClassificationResponse(BaseModel):
    """Response model for classification only."""

    notification_id: str
    category: str
    confidence: float
    reasoning: str


class NotificationResponse(BaseModel):
    """Response model for notification processing."""

    notification_id: str
    classification: dict
    extracted_facts: list[str]
    action: dict
    memory_count: int


@router.post("/classify", response_model=ClassificationResponse, status_code=status.HTTP_200_OK)
async def classify_notification(
    request: NotificationRequest,
    db: Session = Depends(get_db),
    api_key: str = Depends(get_api_key),
) -> ClassificationResponse:
    """
    Classify a notification without full processing.

    This endpoint only runs the classification agent and returns the category,
    confidence, and reasoning. It does not execute actions or extract memory.
    """
    try:
        # Convert request to Notification model
        notification = Notification(
            title=request.title,
            body=request.body,
            app_name=request.app_name,
            package_name=request.package_name,
            timestamp=request.timestamp or datetime.now(),
            extras=request.extras or {},
        )

        # Generate notification ID if not provided
        if not notification.id:
            import uuid

            notification.id = str(uuid.uuid4())

        # Initialize memory for context (optional)
        memory = NotificationMemory()

        # Get API key from environment
        api_key = os.getenv("GOOGLE_API_KEY")
        test_mode = api_key is None

        # Run only classification agent
        classification_agent = ClassificationAgent(api_key=api_key, test_mode=test_mode)
        classification = classification_agent.classify(notification, memory=memory)

        # Store notification in database (but not processing result)
        db_notification = NotificationRecord(
            id=notification.id,
            title=notification.title,
            body=notification.body,
            app_name=notification.app_name,
            package_name=notification.package_name,
            timestamp=notification.timestamp,
            extras=notification.extras,
        )
        db.add(db_notification)
        db.commit()

        logger.info(
            f"Notification {notification.id} classified: {classification.category} "
            f"(confidence: {classification.confidence})"
        )

        return ClassificationResponse(
            notification_id=notification.id,
            category=classification.category.value if hasattr(classification.category, "value") else str(classification.category),
            confidence=classification.confidence,
            reasoning=classification.reasoning,
        )

    except Exception as e:
        logger.error(f"Error classifying notification: {e}", exc_info=True)
        db.rollback()
        raise HTTPException(
            status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
            detail=f"Error classifying notification: {str(e)}",
        )


@router.post("/process", response_model=NotificationResponse, status_code=status.HTTP_200_OK)
async def process_notification(
    request: NotificationRequest,
    db: Session = Depends(get_db),
    api_key: str = Depends(get_api_key),
) -> NotificationResponse:
    """
    Process a notification through the multi-agent pipeline.

    This endpoint:
    1. Receives a notification
    2. Processes it through the classification, memory, and action agents
    3. Stores the results in the database
    4. Returns the processing results
    """
    try:
        # Convert request to Notification model
        notification = Notification(
            title=request.title,
            body=request.body,
            app_name=request.app_name,
            package_name=request.package_name,
            timestamp=request.timestamp or datetime.now(),
            extras=request.extras or {},
        )

        # Generate notification ID if not provided
        if not notification.id:
            import uuid

            notification.id = str(uuid.uuid4())

        # Initialize memory (in production, this would be persistent)
        memory = NotificationMemory()

        # Get API key from environment
        api_key = os.getenv("GOOGLE_API_KEY")
        # Allow test mode if no API key (for development/testing)
        test_mode = api_key is None

        # Process notification through multi-agent pipeline
        result = process_notification_multi_agent(
            notification,
            memory=memory,
            api_key=api_key,
            test_mode=test_mode,
        )

        # Store notification in database
        db_notification = NotificationRecord(
            id=notification.id,
            title=notification.title,
            body=notification.body,
            app_name=notification.app_name,
            package_name=notification.package_name,
            timestamp=notification.timestamp,
            extras=notification.extras,
        )
        db.add(db_notification)

        # Store processing result in database
        db_result = ProcessingResult(
            notification_id=notification.id,
            category=result["classification"]["category"],
            confidence=result["classification"]["confidence"],
            reasoning=result["classification"]["reasoning"],
            action_taken=result["action"]["action"],
            extracted_facts=result["extracted_facts"],
        )
        db.add(db_result)

        db.commit()

        logger.info(
            f"Notification {notification.id} processed: "
            f"{result['classification']['category']} -> {result['action']['action']}"
        )

        return NotificationResponse(
            notification_id=result["notification_id"],
            classification=result["classification"],
            extracted_facts=result["extracted_facts"],
            action=result["action"],
            memory_count=result["memory_count"],
        )

    except Exception as e:
        logger.error(f"Error processing notification: {e}", exc_info=True)
        db.rollback()
        raise HTTPException(
            status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
            detail=f"Error processing notification: {str(e)}",
        )


@router.get("/{notification_id}", status_code=status.HTTP_200_OK)
async def get_notification(
    notification_id: str,
    db: Session = Depends(get_db),
):
    """
    Get a notification and its processing result by ID.
    """
    notification = db.query(NotificationRecord).filter(NotificationRecord.id == notification_id).first()
    if not notification:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail=f"Notification {notification_id} not found",
        )

    result = (
        db.query(ProcessingResult)
        .filter(ProcessingResult.notification_id == notification_id)
        .order_by(ProcessingResult.created_at.desc())
        .first()
    )

    return {
        "notification": {
            "id": notification.id,
            "title": notification.title,
            "body": notification.body,
            "app_name": notification.app_name,
            "package_name": notification.package_name,
            "timestamp": notification.timestamp.isoformat(),
            "extras": notification.extras,
        },
        "processing_result": {
            "category": result.category if result else None,
            "confidence": result.confidence if result else None,
            "reasoning": result.reasoning if result else None,
            "action_taken": result.action_taken if result else None,
            "extracted_facts": result.extracted_facts if result else None,
        } if result else None,
    }


@router.get("/", status_code=status.HTTP_200_OK)
async def list_notifications(
    skip: int = 0,
    limit: int = 100,
    db: Session = Depends(get_db),
):
    """
    List notifications with pagination.
    """
    notifications = (
        db.query(NotificationRecord)
        .order_by(NotificationRecord.timestamp.desc())
        .offset(skip)
        .limit(limit)
        .all()
    )

    return {
        "notifications": [
            {
                "id": n.id,
                "title": n.title,
                "app_name": n.app_name,
                "timestamp": n.timestamp.isoformat(),
            }
            for n in notifications
        ],
        "total": db.query(NotificationRecord).count(),
        "skip": skip,
        "limit": limit,
    }
