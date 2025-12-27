"""
API authentication dependencies.
"""

import os
from fastapi import Depends, HTTPException, Security, status
from fastapi.security import APIKeyHeader

API_KEY_HEADER = APIKeyHeader(name="X-API-Key", auto_error=False)


def get_api_key(api_key_header: str = Security(API_KEY_HEADER)) -> str:
    """
    Validate API key from request header.

    Args:
        api_key_header: API key from X-API-Key header

    Returns:
        The validated API key

    Raises:
        HTTPException: If API key is missing or invalid
    """
    # Get expected API key from environment
    expected_api_key = os.getenv("API_KEY")

    # If no API key is configured, allow all requests (for development)
    if not expected_api_key:
        return "development_mode"

    # If API key is configured, require it
    if not api_key_header:
        raise HTTPException(
            status_code=status.HTTP_401_UNAUTHORIZED,
            detail="API key required. Provide X-API-Key header.",
        )

    if api_key_header != expected_api_key:
        raise HTTPException(
            status_code=status.HTTP_403_FORBIDDEN,
            detail="Invalid API key.",
        )

    return api_key_header

