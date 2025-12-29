"""
Logging configuration and utilities.
"""

import logging
import os
import sys
from typing import Optional


def setup_logging(level: Optional[str] = None) -> None:
    """
    Set up logging configuration for the application.

    Args:
        level: Log level (DEBUG, INFO, WARNING, ERROR, CRITICAL).
               If None, reads from LOG_LEVEL environment variable or defaults to INFO.
    """
    if level is None:
        level = os.getenv("LOG_LEVEL", "INFO").upper()

    # Convert string level to logging constant
    numeric_level = getattr(logging, level, logging.INFO)

    # Configure root logger
    logging.basicConfig(
        level=numeric_level,
        format="%(asctime)s - %(name)s - %(levelname)s - %(message)s",
        datefmt="%Y-%m-%d %H:%M:%S",
        stream=sys.stdout,
    )

    # Set specific logger levels
    logging.getLogger("google").setLevel(logging.WARNING)  # Reduce Google API noise
    logging.getLogger("urllib3").setLevel(logging.WARNING)  # Reduce HTTP noise

    logger = logging.getLogger(__name__)
    logger.info(f"Logging configured at level: {level}")

