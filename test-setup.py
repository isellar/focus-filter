#!/usr/bin/env python3
"""
Quick test script to verify your environment is set up correctly.
Run this before running the notebook to check if all dependencies are available.
"""

import sys

def test_imports():
    """Test if all required packages can be imported."""
    print("Testing imports...")
    
    try:
        import os
        print("✅ os")
    except ImportError as e:
        print(f"❌ os: {e}")
        return False
    
    try:
        from google.adk.agents import LlmAgent
        print("✅ google.adk.agents")
    except ImportError as e:
        print(f"❌ google.adk.agents: {e}")
        return False
    
    try:
        from google.adk.models.google_llm import Gemini
        print("✅ google.adk.models.google_llm")
    except ImportError as e:
        print(f"❌ google.adk.models.google_llm: {e}")
        return False
    
    try:
        from google.adk.runners import Runner
        print("✅ google.adk.runners")
    except ImportError as e:
        print(f"❌ google.adk.runners: {e}")
        return False
    
    try:
        from google.adk.sessions import InMemorySessionService
        print("✅ google.adk.sessions")
    except ImportError as e:
        print(f"❌ google.adk.sessions: {e}")
        return False
    
    try:
        from google.genai import types
        print("✅ google.genai")
    except ImportError as e:
        print(f"❌ google.genai: {e}")
        return False
    
    return True

def test_kaggle_secrets():
    """Test if Kaggle secrets can be accessed."""
    print("\nTesting Kaggle secrets...")
    
    try:
        from kaggle_secrets import UserSecretsClient
        client = UserSecretsClient()
        try:
            key = client.get_secret("GOOGLE_API_KEY")
            if key:
                print("✅ GOOGLE_API_KEY found")
                return True
            else:
                print("⚠️  GOOGLE_API_KEY is empty")
                return False
        except Exception as e:
            print(f"⚠️  Could not get GOOGLE_API_KEY: {e}")
            print("   This is OK if running locally (not in Kaggle)")
            return True  # Not a failure for local runs
    except ImportError:
        print("⚠️  kaggle_secrets not available (expected for local runs)")
        return True  # Not a failure for local runs

def main():
    print("=" * 60)
    print("Environment Setup Test")
    print("=" * 60)
    
    imports_ok = test_imports()
    secrets_ok = test_kaggle_secrets()
    
    print("\n" + "=" * 60)
    if imports_ok:
        print("✅ All imports successful!")
        print("\nYou can now run your notebook.")
    else:
        print("❌ Some imports failed.")
        print("Please install missing packages:")
        print("  pip install google-adk google-genai")
    
    if not secrets_ok:
        print("\n⚠️  Note: Kaggle secrets not available.")
        print("   For local runs, set GOOGLE_API_KEY environment variable:")
        print("   export GOOGLE_API_KEY='your_key_here'")
    
    print("=" * 60)
    
    return 0 if imports_ok else 1

if __name__ == "__main__":
    sys.exit(main())

