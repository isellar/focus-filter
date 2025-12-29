"""
Test that the package structure is correctly set up.
"""


def test_focus_filter_package_imports():
    """Test that focus_filter package and subpackages can be imported."""
    import focus_filter
    from focus_filter import models, agents, tools, memory, context, observability

    assert focus_filter.__version__ == "0.1.0"
    assert models is not None
    assert agents is not None
    assert tools is not None
    assert memory is not None
    assert context is not None
    assert observability is not None


def test_app_package_imports():
    """Test that app package and subpackages can be imported."""
    from app import main
    import app.api
    import app.database

    assert main.app is not None
    assert main.app.title == "Focus Filter API"
    assert app.api is not None
    assert app.database is not None


def test_package_structure():
    """Test that all required directories exist."""
    import os

    base_path = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))

    required_dirs = [
        "focus_filter",
        "focus_filter/models",
        "focus_filter/agents",
        "focus_filter/tools",
        "focus_filter/memory",
        "focus_filter/context",
        "focus_filter/observability",
        "app",
        "app/api",
        "app/database",
        "tests",
    ]

    for dir_path in required_dirs:
        full_path = os.path.join(base_path, dir_path)
        assert os.path.isdir(full_path), f"Directory {dir_path} does not exist"

