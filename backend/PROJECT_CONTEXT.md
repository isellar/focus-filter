# Focus Filter Backend - Project Context

## Project Overview

**Focus Filter** is an intelligent notification filtering system that uses multi-agent AI to classify and manage notifications. The system will eventually become an Android app, but we're starting with the backend API service.

## Project Structure

```
focus-filter/
├── notes/                    # Project plans and documentation
│   ├── PHASE1_PLAN.md       # Detailed Phase 1 implementation plan
│   └── ANDROID_PROTOTYPE_PLAN.md  # Overall Android prototype plan
├── kaggle/                   # Original demo/reference implementation
│   └── submission.ipynb      # Jupyter notebook with agent logic
├── backend/                  # Current directory - Backend API service
│   ├── .devcontainer/       # DevContainer configuration
│   ├── focus_filter/        # Core Python package (to be created)
│   ├── app/                 # FastAPI application (to be created)
│   └── tests/               # Test suite (to be created)
└── [root]/                  # Reference materials (Kaggle setup, etc.)
```

## Current Phase: Phase 1 - Backend API Service

**Goal**: Extract agent logic from notebook into a deployable, containerized API service

**Timeline**: Week 1 (5 days, 26 issues)

### Progress

- ✅ **Issue 1 Complete**: DevContainer & Docker Setup
  - DevContainer configured for VS Code/Cursor
  - Docker Compose files (CPU and GPU variants)
  - Container is running and ready

- 🔄 **Current**: Issue 2 - Project Structure & Base Dockerfile

### Implementation Plan

See `../notes/PHASE1_PLAN.md` for the complete step-by-step plan with:
- 26 detailed issues
- Acceptance criteria for each
- Test commands
- Commit messages

## Architecture Overview

### Multi-Agent System (from notebook)

The system uses three sequential agents:

1. **Classification Agent**: Analyzes notifications and determines urgency
   - Categories: `URGENT`, `IRRELEVANT`, `LESS_URGENT`

2. **Action Agent**: Executes actions based on classification
   - `display_urgent_notification()` - Show urgent alerts
   - `block_notification()` - Suppress irrelevant
   - `save_notification_memory()` - Store less urgent

3. **Memory Agent**: Handles memory extraction and storage
   - Extracts key facts from notifications
   - Stores with deduplication
   - Manages user preferences

### Package Structure (to be created)

```
backend/
├── focus_filter/
│   ├── models/              # Data models (Notification, Memory, etc.)
│   ├── agents/              # Agent classes (Classification, Action, Memory)
│   ├── tools/               # Custom tools for agents
│   ├── memory/              # Memory management
│   ├── context/             # Context building functions
│   └── observability/       # Logging and tracing
├── app/
│   ├── main.py              # FastAPI application
│   ├── api/                 # API routes
│   └── database/            # Database models and CRUD
└── tests/                   # Test suite
```

## Development Guidelines

### Container-First Approach

- **All development happens in the DevContainer**
- Use `docker-compose` commands when needed
- GPU support available via `docker-compose.gpu.yml`

### Testing Strategy

- Write tests as you go
- Run tests in container: `pytest tests/ -v`
- Test each issue before moving to next

### Code Extraction

- Reference implementation: `../kaggle/submission.ipynb`
- Extract agent logic, tools, and memory management
- Convert notebook code to clean Python packages
- Maintain compatibility with Google ADK

### Decision Making

- **Ask before deciding**: Stop and ask about architectural/product decisions
- **User is Product Vision**: Check with user on important choices
- **Incremental commits**: Commit after each issue completion

## Technology Stack

- **Python**: 3.11
- **Framework**: FastAPI
- **Agent Framework**: Google ADK (Agent Developer Kit)
- **LLM**: Gemini 2.0 Flash (via Google API)
- **Database**: SQLite (dev) / PostgreSQL (production)
- **Testing**: pytest, pytest-asyncio, httpx

## Environment Setup

### Required Environment Variables

Create `.env` from `.env.example`:
```
GOOGLE_API_KEY=your_key_here
DATABASE_URL=sqlite:///./focus_filter.db
LOG_LEVEL=INFO
API_KEY=your_api_key_here
```

### Container Commands

```bash
# Start container
docker-compose up -d

# Run commands in container
docker-compose exec backend <command>

# Run one-off commands
docker-compose run --rm backend <command>

# Install dependencies
docker-compose run --rm backend pip install -r requirements.txt

# Run tests
docker-compose run --rm backend pytest tests/ -v
```

## Reference Implementation

The original demo code in `../kaggle/submission.ipynb` contains:

- Multi-agent system implementation
- Notification classification logic
- Memory management with deduplication
- Context engineering (few-shot examples)
- Observability and logging
- Agent evaluation framework

**Key Functions to Extract:**
- `classify_notification()` - Classification agent
- `process_notification_multi_agent()` - Full pipeline
- `NotificationMemory` class - Memory management
- Tool functions: `display_urgent_notification()`, `block_notification()`, etc.
- Context building functions

## Next Steps

1. **Issue 2**: Create package structure
2. **Issue 3**: Extract dependencies and requirements
3. **Issues 4-12**: Extract core logic (models, tools, agents, orchestrator)
4. **Issues 13-15**: Create FastAPI app and endpoints
5. **Issues 16-26**: Database, testing, deployment

See `../notes/PHASE1_PLAN.md` for complete details.

## Questions?

- Check the plan: `../notes/PHASE1_PLAN.md`
- Check overall architecture: `../notes/ANDROID_PROTOTYPE_PLAN.md`
- Reference implementation: `../kaggle/submission.ipynb`

