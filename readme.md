# Focus Filter

Intelligent notification filtering system using multi-agent AI to classify and manage notifications. Eventually becoming an Android app.

## Project Structure

```
focus-filter/
├── notes/              # Project plans and documentation
│   ├── PHASE1_PLAN.md  # Backend API development plan
│   └── ANDROID_PROTOTYPE_PLAN.md  # Overall Android prototype plan
├── kaggle/             # Original demo/reference implementation
│   └── submission.ipynb  # Jupyter notebook with agent logic
├── backend/            # Backend API service (Phase 1 - In Progress)
│   ├── .devcontainer/  # DevContainer for backend development
│   └── [FastAPI app, agents, etc.]
└── [root]/             # This directory - project root
```

## Current Status

**Phase 1: Backend API Service** (Week 1)
- ✅ DevContainer & Docker setup complete
- 🔄 Project structure and code extraction in progress

## Quick Start

### Backend Development

1. **Open in DevContainer** (Recommended):
   - Open `backend/` folder in VS Code/Cursor
   - Click "Reopen in Container" when prompted
   - Dependencies install automatically

2. **Or use Docker Compose**:
   ```bash
   cd backend
   docker-compose up -d
   docker-compose exec backend bash
   ```

See `backend/README.md` for detailed backend setup instructions.

### Reference Materials

- **Plans**: See `notes/` folder
- **Original Demo**: See `kaggle/submission.ipynb`
- **Backend Context**: See `backend/PROJECT_CONTEXT.md`

## Development Approach

- **Container-First**: All development happens in DevContainers
- **Component-Based**: Each component (backend, android) has its own DevContainer
- **Incremental**: Working through Phase 1 issues step-by-step

## Components

### Backend (`backend/`)
FastAPI service with multi-agent system for notification classification.
- Python 3.11
- FastAPI
- Google ADK with Gemini
- See `backend/README.md` for details

### Kaggle Demo (`kaggle/`)
Original reference implementation (Jupyter notebook).
- Kept for reference
- Contains the agent logic we're extracting

## Documentation

- **Backend Development**: `backend/PROJECT_CONTEXT.md`
- **Phase 1 Plan**: `notes/PHASE1_PLAN.md`
- **Overall Plan**: `notes/ANDROID_PROTOTYPE_PLAN.md`

## Notes

- Each component is self-contained with its own DevContainer
- Root directory contains reference materials and plans
- Development happens in component directories (e.g., `backend/`)
