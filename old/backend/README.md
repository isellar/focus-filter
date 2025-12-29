# Focus Filter Backend

Backend API service for the Focus Filter notification filtering system.

## Quick Start

### Option 1: DevContainer (Recommended)

1. Open the `backend/` folder in VS Code or Cursor
2. When prompted, click "Reopen in Container"
3. The container will build and install dependencies automatically
4. You're ready to develop!

### Option 2: Docker Compose

**CPU Mode (default):**
```bash
cd backend
docker-compose up -d
docker-compose exec backend bash
```

**GPU Mode (if NVIDIA GPU available):**
```bash
cd backend
docker-compose -f docker-compose.gpu.yml up -d
docker-compose -f docker-compose.gpu.yml exec backend bash
```

### Check GPU Availability

```bash
# Run the GPU detection script
docker-compose run --rm backend bash scripts/check_gpu.sh
```

Or manually:
```bash
# Check if nvidia-smi works
nvidia-smi

# Test Docker GPU support
docker run --rm --gpus all nvidia/cuda:12.1.0-base-ubuntu22.04 nvidia-smi
```

## Environment Setup

1. Copy `.env.example` to `.env`:
   ```bash
   cp .env.example .env
   ```

2. Edit `.env` and add your API keys:
   ```
   GOOGLE_API_KEY=your_key_here
   API_KEY=your_api_key_here
   ```

## Development

### Install Dependencies

If not using DevContainer, install dependencies manually:
```bash
docker-compose run --rm backend pip install -r requirements.txt
```

### Run Tests

```bash
docker-compose run --rm backend pytest tests/ -v
```

### Run API Server

```bash
docker-compose run --rm backend uvicorn app.main:app --host 0.0.0.0 --port 8000 --reload
```

Then access at: http://localhost:8000

## Project Structure

```
backend/
├── .devcontainer/       # DevContainer configuration
├── app/                  # FastAPI application
├── focus_filter/        # Core package
├── tests/               # Test suite
├── scripts/             # Utility scripts
├── docker-compose.yml   # CPU development
├── docker-compose.gpu.yml  # GPU development
└── requirements.txt     # Python dependencies
```

## Notes

- The DevContainer automatically installs dependencies on first open
- GPU support requires NVIDIA drivers and nvidia-container-toolkit
- All development happens in containers for consistency
- The `.env` file is optional for basic testing but required for API functionality

