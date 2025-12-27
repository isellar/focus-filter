"""
Test that all required dependencies can be imported.
"""


def test_core_dependencies():
    """Test that core dependencies can be imported."""
    import fastapi
    import uvicorn
    import pydantic
    import dotenv

    assert fastapi is not None
    assert uvicorn is not None
    assert pydantic is not None
    assert dotenv is not None


def test_google_dependencies():
    """Test that Google ADK and GenAI can be imported."""
    try:
        from google import adk
        import google.genai
        assert adk is not None
        assert google.genai is not None
    except ImportError as e:
        # If imports fail, provide helpful error message
        raise ImportError(f"Failed to import Google dependencies: {e}")


def test_database_dependencies():
    """Test that database dependencies can be imported."""
    import sqlalchemy
    import aiosqlite

    assert sqlalchemy is not None
    assert aiosqlite is not None


def test_testing_dependencies():
    """Test that testing dependencies can be imported."""
    import pytest
    import pytest_asyncio
    import httpx

    assert pytest is not None
    assert pytest_asyncio is not None
    assert httpx is not None


def test_development_dependencies():
    """Test that development dependencies can be imported."""
    import black
    import isort
    import pylint

    assert black is not None
    assert isort is not None
    assert pylint is not None


def test_fastapi_app_creation():
    """Test that FastAPI can create an app."""
    from fastapi import FastAPI

    app = FastAPI(title="Test App")
    assert app.title == "Test App"


def test_sqlalchemy_version():
    """Test that SQLAlchemy is version 2.x (async support)."""
    import sqlalchemy

    # SQLAlchemy 2.x has async support
    assert sqlalchemy.__version__.startswith("2."), "SQLAlchemy 2.x required for async support"

