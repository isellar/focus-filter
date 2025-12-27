#!/usr/bin/env python3
"""
Simple test script to verify the backend setup is working.
Run this in the container to verify the environment.
"""

import sys
import os

def test_python_version():
    """Test Python version is 3.11+"""
    version = sys.version_info
    assert version.major == 3, f"Expected Python 3, got {version.major}"
    assert version.minor >= 11, f"Expected Python 3.11+, got {version.major}.{version.minor}"
    print(f"✅ Python version: {version.major}.{version.minor}.{version.micro}")

def test_imports():
    """Test that we can import key packages"""
    try:
        import fastapi
        print(f"✅ FastAPI imported: {fastapi.__version__}")
    except ImportError as e:
        print(f"❌ FastAPI import failed: {e}")
        return False
    
    try:
        import pydantic
        print(f"✅ Pydantic imported: {pydantic.__version__}")
    except ImportError as e:
        print(f"❌ Pydantic import failed: {e}")
        return False
    
    return True

def test_environment():
    """Test environment variables"""
    python_unbuffered = os.environ.get("PYTHONUNBUFFERED")
    if python_unbuffered == "1":
        print("✅ PYTHONUNBUFFERED is set correctly")
    else:
        print(f"⚠️  PYTHONUNBUFFERED is {python_unbuffered} (expected '1')")
    
    workspace = os.environ.get("PWD") or os.getcwd()
    print(f"✅ Working directory: {workspace}")

if __name__ == "__main__":
    print("=" * 50)
    print("Testing Backend Setup")
    print("=" * 50)
    
    test_python_version()
    print()
    
    if not test_imports():
        print("\n❌ Some imports failed. Run: pip install -r requirements.txt")
        sys.exit(1)
    print()
    
    test_environment()
    print()
    
    print("=" * 50)
    print("✅ All basic tests passed!")
    print("=" * 50)

