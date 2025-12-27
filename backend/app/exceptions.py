"""
Custom exception classes for the application.
"""


class FocusFilterException(Exception):
    """Base exception for Focus Filter application."""

    pass


class NotificationProcessingError(FocusFilterException):
    """Raised when notification processing fails."""

    pass


class ClassificationError(FocusFilterException):
    """Raised when classification fails."""

    pass


class MemoryExtractionError(FocusFilterException):
    """Raised when memory extraction fails."""

    pass


class DatabaseError(FocusFilterException):
    """Raised when database operations fail."""

    pass

