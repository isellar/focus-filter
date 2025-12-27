# Quick Start Guide

## For Backend Development

### Option 1: DevContainer (Easiest)

1. Open `backend/` folder in VS Code or Cursor
2. When prompted, click **"Reopen in Container"**
3. Wait for container to build and dependencies to install
4. Start coding!

The DevContainer automatically:
- Sets up Python 3.11 environment
- Installs all dependencies
- Configures VS Code extensions
- Mounts your code for live editing

### Option 2: Docker Compose

```bash
cd backend
docker-compose up -d
docker-compose exec backend bash
```

Then install dependencies:
```bash
pip install -r requirements.txt
```

### Option 3: GPU Mode (if you have NVIDIA GPU)

```bash
cd backend
docker-compose -f docker-compose.gpu.yml up -d
docker-compose -f docker-compose.gpu.yml exec backend bash
```

## First Steps

1. **Read the context**: Check `backend/PROJECT_CONTEXT.md`
2. **Check the plan**: See `notes/PHASE1_PLAN.md`
3. **Start with Issue 2**: Create package structure

## Project Navigation

- **Backend code**: `backend/`
- **Plans**: `notes/`
- **Reference demo**: `kaggle/submission.ipynb`
- **This guide**: Root directory

## Need Help?

- Backend setup issues? See `backend/README.md`
- What to work on? See `notes/PHASE1_PLAN.md`
- Project overview? See `backend/PROJECT_CONTEXT.md`
