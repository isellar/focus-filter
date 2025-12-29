# Agent Prompt for Backend Development

## Quick Start

**Read this first**: `PROJECT_CONTEXT.md` in the current directory for complete project context.

## Current Task: Issue 2 - Project Structure & Base Dockerfile

**Status**: Issue 1 (DevContainer setup) is complete. Container is running.

**Task**: According to `../notes/PHASE1_PLAN.md`, create the Python package structure:

1. Create `focus_filter/` package with `__init__.py`
2. Create subdirectories with `__init__.py` files:
   - `models/`
   - `agents/`
   - `tools/`
   - `memory/`
   - `context/`
   - `observability/`
3. Create `app/` directory for FastAPI application
4. Create `tests/` directory
5. Test that the package structure can be imported

## Important Guidelines

- **Read PROJECT_CONTEXT.md** for full project details
- **Container-First**: All development happens in this container
- **Test as You Go**: Write and run tests as it makes sense
- **Ask Before Deciding**: Stop and ask about architectural/product decisions
- **User is Product Vision**: Check with user on important choices
- **Reference**: Original code is in `../kaggle/submission.ipynb`

## Getting Started

1. Read `PROJECT_CONTEXT.md` for complete context
2. Check `../notes/PHASE1_PLAN.md` for Issue 2 details
3. Create the directory structure
4. Test imports work: `python -c "import focus_filter; print('OK')"`
5. Commit progress

Let's begin with Issue 2!
