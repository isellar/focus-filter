"""
FastAPI main application entry point.
"""

from fastapi import FastAPI, Request
from fastapi.middleware.cors import CORSMiddleware
from fastapi.responses import JSONResponse

from app.api.notifications import router as notifications_router
from app.database.base import init_db
from app.exceptions import (
    ClassificationError,
    DatabaseError,
    FocusFilterException,
    MemoryExtractionError,
    NotificationProcessingError,
)
from focus_filter.observability.logging import setup_logging

# Set up logging
setup_logging()

# Initialize database
init_db()

# Create FastAPI app
app = FastAPI(
    title="Focus Filter API",
    description="Intelligent notification filtering system using multi-agent AI",
    version="0.1.0",
    docs_url="/docs",
    redoc_url="/redoc",
)

# Configure CORS
app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],  # In production, specify actual origins
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

# Include routers
app.include_router(notifications_router)


# Global exception handlers
@app.exception_handler(NotificationProcessingError)
async def notification_processing_exception_handler(request: Request, exc: NotificationProcessingError):
    """Handle notification processing errors."""
    return JSONResponse(
        status_code=500,
        content={"error": "NotificationProcessingError", "detail": str(exc)},
    )


@app.exception_handler(ClassificationError)
async def classification_exception_handler(request: Request, exc: ClassificationError):
    """Handle classification errors."""
    return JSONResponse(
        status_code=500,
        content={"error": "ClassificationError", "detail": str(exc)},
    )


@app.exception_handler(MemoryExtractionError)
async def memory_extraction_exception_handler(request: Request, exc: MemoryExtractionError):
    """Handle memory extraction errors."""
    return JSONResponse(
        status_code=500,
        content={"error": "MemoryExtractionError", "detail": str(exc)},
    )


@app.exception_handler(DatabaseError)
async def database_exception_handler(request: Request, exc: DatabaseError):
    """Handle database errors."""
    return JSONResponse(
        status_code=500,
        content={"error": "DatabaseError", "detail": str(exc)},
    )


@app.exception_handler(FocusFilterException)
async def focus_filter_exception_handler(request: Request, exc: FocusFilterException):
    """Handle general Focus Filter errors."""
    return JSONResponse(
        status_code=500,
        content={"error": "FocusFilterException", "detail": str(exc)},
    )


@app.get("/")
async def root():
    """Health check endpoint."""
    return {
        "message": "Focus Filter API",
        "status": "running",
        "version": "0.1.0",
    }


@app.get("/health")
async def health():
    """Detailed health check endpoint."""
    return {
        "status": "healthy",
        "service": "Focus Filter API",
        "version": "0.1.0",
    }

