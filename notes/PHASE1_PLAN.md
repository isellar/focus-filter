# Phase 1: Backend API Service - Step-by-Step Plan

**Goal**: Extract agent logic from notebook into a deployable API service

**Timeline**: Week 1 (5 days)

---

## Project Structure Overview

```
backend/
├── focus_filter/
│   ├── __init__.py
│   ├── models/
│   │   ├── __init__.py
│   │   ├── notification.py
│   │   └── memory.py
│   ├── agents/
│   │   ├── __init__.py
│   ├── tools/
│   │   ├── __init__.py
│   │   └── notification_tools.py
│   ├── memory/
│   │   ├── __init__.py
│   │   └── memory_manager.py
│   ├── context/
│   │   ├── __init__.py
│   │   └── context_builder.py
│   └── observability/
│       ├── __init__.py
│       └── logger.py
├── app/
│   ├── __init__.py
│   ├── main.py
│   ├── api/
│   │   ├── __init__.py
│   │   └── routes.py
│   └── database/
│       ├── __init__.py
│       ├── models.py
│       └── crud.py
├── tests/
│   ├── __init__.py
│   ├── test_models.py
│   ├── test_tools.py
│   ├── test_agents.py
│   └── test_api.py
├── .devcontainer/
│   ├── devcontainer.json
│   └── Dockerfile
├── docker-compose.yml
├── docker-compose.gpu.yml  # GPU-enabled compose file
├── Dockerfile
├── Dockerfile.gpu          # GPU-enabled Dockerfile
├── requirements.txt
├── .env.example
├── .dockerignore
└── README.md
```

## Containerization Strategy

**Development Approach:**
- **Primary**: Use DevContainers in VS Code/Cursor for seamless development
- **Secondary**: Use Docker Compose for multi-container orchestration
- **GPU Support**: Optional GPU acceleration with automatic CPU fallback
- **Flexibility**: Works on both powerful GPU machines and low-power laptops

**GPU Configuration:**
- Detects NVIDIA GPU availability automatically
- Falls back to CPU if GPU unavailable
- Uses NVIDIA Container Toolkit when GPU is present
- All tests and development work in containers

---

## Issue 1: DevContainer & Docker Setup
**Estimated Time**: 1.5 hours  
**Priority**: Critical  
**Dependencies**: None

### Tasks:
- [ ] Create `.devcontainer/` directory
- [ ] Create `.devcontainer/devcontainer.json`:
  ```json
  {
    "name": "Focus Filter Backend",
    "dockerComposeFile": "../docker-compose.yml",
    "service": "backend",
    "workspaceFolder": "/workspace",
    "customizations": {
      "vscode": {
        "extensions": [
          "ms-python.python",
          "ms-python.vscode-pylance",
          "ms-python.black-formatter",
          "ms-python.isort"
        ],
        "settings": {
          "python.defaultInterpreterPath": "/usr/local/bin/python",
          "python.linting.enabled": true,
          "python.formatting.provider": "black"
        }
      }
    },
    "forwardPorts": [8000],
    "postCreateCommand": "pip install -r requirements.txt"
  }
  ```
- [ ] Create `.devcontainer/Dockerfile` (base image for dev)
- [ ] Create `docker-compose.yml`:
  ```yaml
  version: '3.8'
  services:
    backend:
      build:
        context: .
        dockerfile: .devcontainer/Dockerfile
      volumes:
        - .:/workspace
      ports:
        - "8000:8000"
      env_file:
        - .env
      environment:
        - PYTHONUNBUFFERED=1
      command: tail -f /dev/null  # Keep container running
  ```
- [ ] Create `docker-compose.gpu.yml` (GPU-enabled version):
  ```yaml
  version: '3.8'
  services:
    backend:
      build:
        context: .
        dockerfile: .devcontainer/Dockerfile.gpu
      volumes:
        - .:/workspace
      ports:
        - "8000:8000"
      env_file:
        - .env
      environment:
        - PYTHONUNBUFFERED=1
      deploy:
        resources:
          reservations:
            devices:
              - driver: nvidia
                count: 1
                capabilities: [gpu]
      command: tail -f /dev/null
  ```
- [ ] Create GPU detection script `scripts/check_gpu.sh`:
  ```bash
  #!/bin/bash
  if command -v nvidia-smi &> /dev/null; then
    nvidia-smi --query-gpu=name --format=csv,noheader
    echo "GPU detected - use docker-compose.gpu.yml"
    exit 0
  else
    echo "No GPU detected - using CPU-only docker-compose.yml"
    exit 1
  fi
  ```
- [ ] Create `.dockerignore`:
  ```
  __pycache__/
  *.pyc
  .env
  *.db
  .venv/
  venv/
  .git/
  .pytest_cache/
  ```

### Acceptance Criteria:
- ✅ DevContainer configuration created
- ✅ Docker Compose files created (CPU and GPU versions)
- ✅ Can open project in DevContainer
- ✅ Container starts and mounts workspace
- ✅ GPU detection script works

### Test:
```bash
# Test GPU detection
chmod +x scripts/check_gpu.sh
./scripts/check_gpu.sh

# Test CPU-only compose
docker-compose up -d
docker-compose exec backend python --version

# Test GPU compose (if GPU available)
docker-compose -f docker-compose.gpu.yml up -d
docker-compose -f docker-compose.gpu.yml exec backend python -c "import torch; print(torch.cuda.is_available())"
```

### Commit Message:
```
feat: Set up DevContainer and Docker Compose

- Create DevContainer configuration for VS Code/Cursor
- Add docker-compose.yml for CPU-only development
- Add docker-compose.gpu.yml for GPU-enabled development
- Add GPU detection script
- Configure workspace mounting and port forwarding
```

---

## Issue 2: Project Structure & Base Dockerfile
**Estimated Time**: 45 minutes  
**Priority**: Critical  
**Dependencies**: Issue 1

### Tasks:
- [ ] Create `backend/` directory structure
- [ ] Create `focus_filter/` package with `__init__.py`
- [ ] Create subdirectories: `models/`, `agents/`, `tools/`, `memory/`, `context/`, `observability/`
- [ ] Create `app/` directory for FastAPI application
- [ ] Create `tests/` directory
- [ ] Create `.devcontainer/Dockerfile` (CPU base):
  ```dockerfile
  FROM python:3.11-slim
  
  WORKDIR /workspace
  
  # Install system dependencies
  RUN apt-get update && apt-get install -y \
      git \
      curl \
      && rm -rf /var/lib/apt/lists/*
  
  # Install Python dependencies will be done via postCreateCommand
  # or manually: pip install -r requirements.txt
  ```
- [ ] Create `.devcontainer/Dockerfile.gpu` (GPU base):
  ```dockerfile
  FROM nvidia/cuda:12.1.0-runtime-ubuntu22.04
  
  # Install Python 3.11
  RUN apt-get update && apt-get install -y \
      python3.11 \
      python3.11-dev \
      python3-pip \
      git \
      curl \
      && rm -rf /var/lib/apt/lists/*
  
  # Create symlink for python
  RUN ln -s /usr/bin/python3.11 /usr/bin/python
  
  WORKDIR /workspace
  ```
- [ ] Ensure proper `.gitignore` in root

### Acceptance Criteria:
- ✅ Directory structure matches plan above
- ✅ All `__init__.py` files created
- ✅ Dockerfiles build successfully
- ✅ Can import `focus_filter` package in container

### Test:
```bash
# Build and test in container
docker-compose build
docker-compose run --rm backend python -c "import focus_filter; print('✅ Package structure OK')"
```

### Commit Message:
```
feat: Create project structure and base Dockerfiles

- Create focus_filter package with subdirectories
- Set up app/ directory for FastAPI
- Create tests/ directory structure
- Add CPU and GPU base Dockerfiles
```

---

## Issue 3: Core Dependencies & Requirements
**Estimated Time**: 30 minutes  
**Priority**: Critical  
**Dependencies**: Issue 2

### Tasks:
- [ ] Create `requirements.txt` with:
  - `fastapi>=0.104.0`
  - `uvicorn[standard]>=0.24.0`
  - `google-adk` (or specific package name)
  - `google-genai`
  - `python-dotenv>=1.0.0`
  - `pydantic>=2.0.0`
  - `sqlalchemy>=2.0.0`
  - `pytest>=7.4.0`
  - `httpx>=0.25.0` (for testing)
- [ ] Create `.env.example` with:
  ```
  GOOGLE_API_KEY=your_key_here
  DATABASE_URL=sqlite:///./focus_filter.db
  LOG_LEVEL=INFO
  ```
- [ ] Create `.gitignore` to exclude:
  - `__pycache__/`
  - `*.pyc`
  - `.env`
  - `*.db`
  - `.venv/`
  - `venv/`

### Acceptance Criteria:
- ✅ `requirements.txt` includes all necessary packages
- ✅ `.env.example` documents required environment variables
- ✅ `.gitignore` prevents committing sensitive files

### Test:
```bash
# Install in container
docker-compose run --rm backend pip install -r requirements.txt
docker-compose run --rm backend python -c "import fastapi, google.adk; print('✅ Dependencies OK')"

# Or if using DevContainer, dependencies install automatically via postCreateCommand
```

### Commit Message:
```
feat: Add core dependencies and configuration files

- Add requirements.txt with FastAPI, Google ADK, and dependencies
- Create .env.example for environment variables
- Add .gitignore for Python projects
```

---

## Issue 4: Extract Notification Data Model
**Estimated Time**: 30 minutes  
**Priority**: Critical  
**Dependencies**: Issue 2

### Tasks:
- [ ] Create `focus_filter/models/notification.py`
- [ ] Extract `Notification` dataclass from notebook
- [ ] Convert to Pydantic model for API validation:
  ```python
  from pydantic import BaseModel
  from typing import Optional
  from datetime import datetime
  
  class Notification(BaseModel):
      id: str
      app: str
      title: str
      body: str
      timestamp: str
      category: Optional[str] = None
  ```
- [ ] Add `to_dict()` method
- [ ] Add `__str__()` method for logging

### Acceptance Criteria:
- ✅ `Notification` model matches notebook implementation
- ✅ Can create Notification instances
- ✅ Serializes to/from JSON correctly
- ✅ Type hints are correct

### Test:
```bash
# Run in container
docker-compose run --rm backend python -c "
from focus_filter.models.notification import Notification
from datetime import datetime

notif = Notification(
    id='test_1',
    app='TestApp',
    title='Test',
    body='Test body',
    timestamp=datetime.now().isoformat()
)
assert notif.app == 'TestApp'
assert notif.to_dict()['app'] == 'TestApp'
print('✅ Notification model OK')
"
```

### Commit Message:
```
feat: Extract Notification data model

- Create Notification Pydantic model
- Match functionality from notebook
- Add serialization methods
```

---

## Issue 5: Extract Memory Data Model
**Estimated Time**: 30 minutes  
**Priority**: Critical  
**Dependencies**: Issue 4

### Tasks:
- [ ] Create `focus_filter/models/memory.py`
- [ ] Extract `NotificationMemory` class structure from notebook
- [ ] Create Pydantic models:
  - `MemoryEntry` (for individual memory items)
  - `UserPreferences` (for user preference storage)
- [ ] Keep structure similar to notebook for now (will add database later)

### Acceptance Criteria:
- ✅ Memory models match notebook structure
- ✅ Can create and manipulate memory entries
- ✅ User preferences model is defined

### Test:
```python
from focus_filter.models.memory import MemoryEntry, UserPreferences

memory = MemoryEntry(
    notification_id="test_1",
    app="TestApp",
    extracted_fact="Test fact",
    timestamp="2025-01-01T00:00:00"
)
assert memory.app == "TestApp"
print("✅ Memory models OK")
```

### Commit Message:
```
feat: Extract memory data models

- Create MemoryEntry and UserPreferences models
- Match notebook structure
- Prepare for database integration
```

---

## Issue 6: Extract Notification Tools
**Estimated Time**: 1 hour  
**Priority**: Critical  
**Dependencies**: Issue 4

### Tasks:
- [ ] Create `focus_filter/tools/notification_tools.py`
- [ ] Extract `display_urgent_notification()` function from notebook
- [ ] Extract `block_notification()` function
- [ ] Extract `save_notification_memory()` function
- [ ] Extract `retrieve_user_preferences()` function
- [ ] Convert to async functions (for FastAPI compatibility)
- [ ] Add type hints
- [ ] Keep tool signatures matching notebook (for agent compatibility)

### Acceptance Criteria:
- ✅ All four tools extracted and functional
- ✅ Tools return dictionaries as expected by agents
- ✅ Type hints added
- ✅ Functions are async-compatible

### Test:
```python
from focus_filter.tools.notification_tools import (
    display_urgent_notification,
    block_notification,
    save_notification_memory,
    retrieve_user_preferences
)

result = await display_urgent_notification("TestApp", "Title", "Body")
assert result["status"] == "displayed"
print("✅ Notification tools OK")
```

### Commit Message:
```
feat: Extract notification tools from notebook

- Extract display_urgent_notification, block_notification
- Extract save_notification_memory, retrieve_user_preferences
- Convert to async functions with type hints
- Maintain compatibility with agent expectations
```

---

## Issue 7: Extract Memory Manager Class
**Estimated Time**: 1.5 hours  
**Priority**: High  
**Dependencies**: Issue 5, Issue 6

### Tasks:
- [ ] Create `focus_filter/memory/memory_manager.py`
- [ ] Extract `NotificationMemory` class from notebook
- [ ] Extract all methods:
  - `store()`
  - `get_all()`
  - `search()`
  - `enhanced_store()` (if exists)
  - `consolidate_memories()`
  - `get_user_preferences()`
  - `update_preference()`
  - `learn_from_patterns()`
  - `compact_context()`
- [ ] Keep in-memory implementation for now (database in later issue)
- [ ] Add type hints
- [ ] Add docstrings

### Acceptance Criteria:
- ✅ All memory manager methods extracted
- ✅ Methods work identically to notebook version
- ✅ Can store, retrieve, and search memories
- ✅ User preferences functionality works

### Test:
```python
from focus_filter.memory.memory_manager import NotificationMemory
from focus_filter.models.notification import Notification

memory = NotificationMemory()
notif = Notification(
    id="test_1",
    app="TestApp",
    title="Test",
    body="Test body",
    timestamp="2025-01-01T00:00:00"
)
memory.store(notif, "Test fact")
assert len(memory.get_all()) == 1
print("✅ Memory manager OK")
```

### Commit Message:
```
feat: Extract memory manager class

- Extract NotificationMemory class with all methods
- Maintain in-memory storage for now
- Add type hints and docstrings
- Preserve notebook functionality
```

---

## Issue 8: Extract Context Builder Functions
**Estimated Time**: 1 hour  
**Priority**: High  
**Dependencies**: Issue 7

### Tasks:
- [ ] Create `focus_filter/context/context_builder.py`
- [ ] Extract context building functions from notebook:
  - `build_few_shot_context()`
  - `build_user_preference_context()`
  - `build_dynamic_context()`
  - `get_optimized_classification_instruction()`
  - `get_enhanced_classification_prompt()`
  - `get_optimized_action_instruction()`
  - `get_optimized_memory_instruction()`
- [ ] Add type hints
- [ ] Add docstrings explaining each function

### Acceptance Criteria:
- ✅ All context building functions extracted
- ✅ Functions produce same output as notebook
- ✅ Can build context for classification agent
- ✅ Can build context for action agent
- ✅ Can build context for memory agent

### Test:
```python
from focus_filter.context.context_builder import (
    build_few_shot_context,
    get_optimized_classification_instruction
)

context = build_few_shot_context()
assert len(context) > 0
instruction = get_optimized_classification_instruction()
assert "URGENT" in instruction or "classification" in instruction.lower()
print("✅ Context builder OK")
```

### Commit Message:
```
feat: Extract context building functions

- Extract all context engineering functions
- Maintain few-shot examples and prompts
- Add type hints and documentation
- Preserve notebook behavior
```

---

## Issue 9: Extract Classification Agent Logic
**Estimated Time**: 2 hours  
**Priority**: Critical  
**Dependencies**: Issue 6, Issue 8

### Tasks:
- [ ] Create `focus_filter/agents/classification_agent.py`
- [ ] Extract `classify_notification()` function from notebook
- [ ] Extract agent setup code:
  - LLM initialization (Gemini)
  - System instructions
  - Tool registration
  - Runner setup
- [ ] Create `ClassificationAgent` class:
  ```python
  class ClassificationAgent:
      def __init__(self, api_key: str):
          # Initialize agent
      
      async def classify(self, app: str, title: str, body: str) -> dict:
          # Classify notification
  ```
- [ ] Add error handling
- [ ] Add logging

### Acceptance Criteria:
- ✅ ClassificationAgent class created
- ✅ Can initialize agent with API key
- ✅ Can classify notifications (URGENT/IRRELEVANT/LESS_URGENT)
- ✅ Returns structured result matching notebook format
- ✅ Handles errors gracefully

### Test:
```python
import os
from focus_filter.agents.classification_agent import ClassificationAgent

api_key = os.getenv("GOOGLE_API_KEY")
agent = ClassificationAgent(api_key)
result = await agent.classify("Bank", "Security Alert", "Suspicious activity detected")
assert result["classification"] in ["URGENT", "IRRELEVANT", "LESS_URGENT"]
print("✅ Classification agent OK")
```

### Commit Message:
```
feat: Extract classification agent

- Create ClassificationAgent class
- Extract agent initialization and classification logic
- Add error handling and logging
- Maintain compatibility with notebook implementation
```

---

## Issue 10: Extract Action Agent Logic
**Estimated Time**: 1.5 hours  
**Priority**: Critical  
**Dependencies**: Issue 6, Issue 9

### Tasks:
- [ ] Create `focus_filter/agents/action_agent.py`
- [ ] Extract action agent setup from notebook
- [ ] Create `ActionAgent` class:
  ```python
  class ActionAgent:
      def __init__(self, api_key: str):
          # Initialize agent
      
      async def execute_action(self, classification: str, notification: Notification) -> dict:
          # Execute action based on classification
  ```
- [ ] Integrate with notification tools
- [ ] Add error handling
- [ ] Add logging

### Acceptance Criteria:
- ✅ ActionAgent class created
- ✅ Can execute actions based on classification
- ✅ Calls appropriate tools (display/block/save)
- ✅ Returns action result

### Test:
```python
from focus_filter.agents.action_agent import ActionAgent
from focus_filter.models.notification import Notification

agent = ActionAgent(api_key)
notif = Notification(...)
result = await agent.execute_action("URGENT", notif)
assert result["action"] in ["displayed", "blocked", "saved"]
print("✅ Action agent OK")
```

### Commit Message:
```
feat: Extract action agent

- Create ActionAgent class
- Extract action execution logic
- Integrate with notification tools
- Add error handling
```

---

## Issue 11: Extract Memory Agent Logic
**Estimated Time**: 1.5 hours  
**Priority**: High  
**Dependencies**: Issue 7, Issue 10

### Tasks:
- [ ] Create `focus_filter/agents/memory_agent.py`
- [ ] Extract memory agent setup from notebook
- [ ] Create `MemoryAgent` class:
  ```python
  class MemoryAgent:
      def __init__(self, api_key: str, memory_manager: NotificationMemory):
          # Initialize agent
      
      async def extract_and_store(self, notification: Notification) -> dict:
          # Extract fact and store in memory
  ```
- [ ] Integrate with memory manager
- [ ] Add error handling
- [ ] Add logging

### Acceptance Criteria:
- ✅ MemoryAgent class created
- ✅ Can extract facts from notifications
- ✅ Stores extracted facts in memory manager
- ✅ Returns extraction result

### Test:
```python
from focus_filter.agents.memory_agent import MemoryAgent
from focus_filter.memory.memory_manager import NotificationMemory

memory_manager = NotificationMemory()
agent = MemoryAgent(api_key, memory_manager)
notif = Notification(...)
result = await agent.extract_and_store(notif)
assert "extracted_fact" in result
assert len(memory_manager.get_all()) > 0
print("✅ Memory agent OK")
```

### Commit Message:
```
feat: Extract memory agent

- Create MemoryAgent class
- Extract memory extraction logic
- Integrate with memory manager
- Add error handling
```

---

## Issue 12: Create Agent Orchestrator
**Estimated Time**: 2 hours  
**Priority**: Critical  
**Dependencies**: Issue 9, Issue 10, Issue 11

### Tasks:
- [ ] Create `focus_filter/agents/orchestrator.py`
- [ ] Extract `process_notification_multi_agent()` function from notebook
- [ ] Create `AgentOrchestrator` class:
  ```python
  class AgentOrchestrator:
      def __init__(self, api_key: str, memory_manager: NotificationMemory):
          # Initialize all agents
      
      async def process_notification(self, notification: Notification) -> dict:
          # Run full pipeline: Classification → Action → Memory
  ```
- [ ] Implement sequential agent flow
- [ ] Add comprehensive error handling
- [ ] Add logging for each step

### Acceptance Criteria:
- ✅ AgentOrchestrator coordinates all three agents
- ✅ Processes notifications through full pipeline
- ✅ Returns complete result with classification, action, and memory
- ✅ Handles errors at each step gracefully

### Test:
```python
from focus_filter.agents.orchestrator import AgentOrchestrator
from focus_filter.memory.memory_manager import NotificationMemory

memory_manager = NotificationMemory()
orchestrator = AgentOrchestrator(api_key, memory_manager)
notif = Notification(...)
result = await orchestrator.process_notification(notif)
assert "classification" in result
assert "action" in result
print("✅ Agent orchestrator OK")
```

### Commit Message:
```
feat: Create agent orchestrator

- Implement sequential multi-agent pipeline
- Coordinate Classification → Action → Memory flow
- Add comprehensive error handling
- Add step-by-step logging
```

---

## Issue 13: Create Basic FastAPI Application
**Estimated Time**: 1 hour  
**Priority**: Critical  
**Dependencies**: Issue 12

### Tasks:
- [ ] Create `app/main.py`
- [ ] Set up FastAPI app:
  ```python
  from fastapi import FastAPI
  from fastapi.middleware.cors import CORSMiddleware
  
  app = FastAPI(title="Focus Filter API", version="0.1.0")
  
  app.add_middleware(
      CORSMiddleware,
      allow_origins=["*"],  # Configure properly later
      allow_credentials=True,
      allow_methods=["*"],
      allow_headers=["*"],
  )
  ```
- [ ] Add environment variable loading with `python-dotenv`
- [ ] Add basic health check endpoint:
  ```python
  @app.get("/health")
  async def health_check():
      return {"status": "healthy"}
  ```
- [ ] Add root endpoint with API info

### Acceptance Criteria:
- ✅ FastAPI app starts without errors
- ✅ Health check endpoint returns 200
- ✅ Environment variables load correctly
- ✅ CORS middleware configured

### Test:
```bash
# Start API in container
docker-compose up -d backend
docker-compose exec backend uvicorn app.main:app --host 0.0.0.0 --port 8000 &
sleep 2
curl http://localhost:8000/health
# Should return: {"status":"healthy"}

# Or use DevContainer and run uvicorn directly in integrated terminal
```

### Commit Message:
```
feat: Create basic FastAPI application

- Set up FastAPI with CORS middleware
- Add health check endpoint
- Configure environment variable loading
- Add root endpoint with API info
```

---

## Issue 14: Create Classification API Endpoint
**Estimated Time**: 1.5 hours  
**Priority**: Critical  
**Dependencies**: Issue 13, Issue 9

### Tasks:
- [ ] Create `app/api/routes.py`
- [ ] Create Pydantic request model:
  ```python
  class ClassificationRequest(BaseModel):
      app: str
      title: str
      body: str
      user_id: str
  ```
- [ ] Create response model:
  ```python
  class ClassificationResponse(BaseModel):
      classification: str  # URGENT, IRRELEVANT, LESS_URGENT
      reasoning: str
      confidence: Optional[float] = None
  ```
- [ ] Create `/api/v1/classify` endpoint:
  ```python
  @app.post("/api/v1/classify", response_model=ClassificationResponse)
  async def classify_notification(request: ClassificationRequest):
      # Initialize agent, classify, return result
  ```
- [ ] Add error handling (400, 500)
- [ ] Add request validation

### Acceptance Criteria:
- ✅ Endpoint accepts POST requests with notification data
- ✅ Returns classification result
- ✅ Handles invalid input (400)
- ✅ Handles agent errors (500)
- ✅ Response matches schema

### Test:
```bash
curl -X POST http://localhost:8000/api/v1/classify \
  -H "Content-Type: application/json" \
  -d '{
    "app": "Bank",
    "title": "Security Alert",
    "body": "Suspicious activity detected",
    "user_id": "test_user"
  }'
# Should return classification result
```

### Commit Message:
```
feat: Add classification API endpoint

- Create POST /api/v1/classify endpoint
- Add request/response models
- Integrate with ClassificationAgent
- Add error handling and validation
```

---

## Issue 15: Create Full Processing API Endpoint
**Estimated Time**: 2 hours  
**Priority**: Critical  
**Dependencies**: Issue 14, Issue 12

### Tasks:
- [ ] Create request model for full processing:
  ```python
  class ProcessRequest(BaseModel):
      app: str
      title: str
      body: str
      user_id: str
  ```
- [ ] Create response model:
  ```python
  class ProcessResponse(BaseModel):
      classification: str
      action: str
      action_details: dict
      memory_extracted: Optional[str] = None
      timestamp: str
  ```
- [ ] Create `/api/v1/process` endpoint:
  ```python
  @app.post("/api/v1/process", response_model=ProcessResponse)
  async def process_notification(request: ProcessRequest):
      # Use AgentOrchestrator to process full pipeline
  ```
- [ ] Initialize orchestrator (with memory manager)
- [ ] Add error handling
- [ ] Add logging

### Acceptance Criteria:
- ✅ Endpoint processes notifications through full pipeline
- ✅ Returns complete result (classification + action + memory)
- ✅ Handles errors gracefully
- ✅ Logs processing steps

### Test:
```bash
curl -X POST http://localhost:8000/api/v1/process \
  -H "Content-Type: application/json" \
  -d '{
    "app": "Slack",
    "title": "Project Update",
    "body": "Deadline moved to Tuesday",
    "user_id": "test_user"
  }'
# Should return full processing result
```

### Commit Message:
```
feat: Add full processing API endpoint

- Create POST /api/v1/process endpoint
- Integrate with AgentOrchestrator
- Return complete pipeline results
- Add comprehensive error handling
```

---

## Issue 16: Add Basic Logging
**Estimated Time**: 1 hour  
**Priority**: Medium  
**Dependencies**: Issue 13

### Tasks:
- [ ] Create `focus_filter/observability/logger.py`
- [ ] Extract `ObservabilityManager` class from notebook (simplified version)
- [ ] Set up Python logging:
  ```python
  import logging
  logging.basicConfig(level=logging.INFO)
  logger = logging.getLogger(__name__)
  ```
- [ ] Add structured logging to:
  - API endpoints (request/response)
  - Agent operations
  - Errors
- [ ] Add log level configuration from environment

### Acceptance Criteria:
- ✅ Logging configured and working
- ✅ API requests are logged
- ✅ Agent operations are logged
- ✅ Errors are logged with context
- ✅ Log level configurable via environment

### Test:
```python
import logging
from focus_filter.observability.logger import logger

logger.info("Test log message")
# Should appear in console/logs
```

### Commit Message:
```
feat: Add basic logging infrastructure

- Set up Python logging
- Extract simplified ObservabilityManager
- Add structured logging to API and agents
- Configure log level from environment
```

---

## Issue 17: Add Database Models (SQLAlchemy)
**Estimated Time**: 2 hours  
**Priority**: High  
**Dependencies**: Issue 5

### Tasks:
- [ ] Create `app/database/models.py`
- [ ] Create SQLAlchemy models:
  ```python
  from sqlalchemy import Column, String, Text, DateTime, Integer
  from sqlalchemy.ext.declarative import declarative_base
  
  Base = declarative_base()
  
  class NotificationDB(Base):
      __tablename__ = "notifications"
      id = Column(String, primary_key=True)
      app = Column(String)
      title = Column(String)
      body = Column(Text)
      classification = Column(String)
      timestamp = Column(DateTime)
      user_id = Column(String)
  
  class MemoryDB(Base):
      __tablename__ = "memories"
      id = Column(Integer, primary_key=True)
      notification_id = Column(String)
      app = Column(String)
      extracted_fact = Column(Text)
      timestamp = Column(DateTime)
      user_id = Column(String)
  
  class UserPreferenceDB(Base):
      __tablename__ = "user_preferences"
      id = Column(Integer, primary_key=True)
      user_id = Column(String)
      preference_type = Column(String)
      value = Column(String)
  ```
- [ ] Create database initialization function
- [ ] Update `.env.example` with `DATABASE_URL`

### Acceptance Criteria:
- ✅ Database models defined
- ✅ Can create database tables
- ✅ Models match data structures from notebook

### Test:
```python
from app.database.models import Base, NotificationDB
from sqlalchemy import create_engine

engine = create_engine("sqlite:///test.db")
Base.metadata.create_all(engine)
print("✅ Database models OK")
```

### Commit Message:
```
feat: Add database models with SQLAlchemy

- Create NotificationDB, MemoryDB, UserPreferenceDB models
- Set up database initialization
- Match notebook data structures
```

---

## Issue 18: Create Database CRUD Operations
**Estimated Time**: 2 hours  
**Priority**: High  
**Dependencies**: Issue 17

### Tasks:
- [ ] Create `app/database/crud.py`
- [ ] Create CRUD functions:
  - `create_notification()`
  - `get_notification()`
  - `create_memory()`
  - `get_memories_for_user()`
  - `get_user_preferences()`
  - `update_user_preference()`
- [ ] Add database session management
- [ ] Add error handling

### Acceptance Criteria:
- ✅ Can create and retrieve notifications
- ✅ Can create and retrieve memories
- ✅ Can manage user preferences
- ✅ Database operations are transactional

### Test:
```python
from app.database.crud import create_notification, get_notification

notif = create_notification(
    id="test_1",
    app="TestApp",
    title="Test",
    body="Test body",
    classification="URGENT",
    user_id="test_user"
)
retrieved = get_notification("test_1")
assert retrieved.app == "TestApp"
print("✅ CRUD operations OK")
```

### Commit Message:
```
feat: Add database CRUD operations

- Create functions for notification, memory, preference operations
- Add session management
- Add error handling
```

---

## Issue 19: Integrate Database with Memory Manager
**Estimated Time**: 2 hours  
**Priority**: High  
**Dependencies**: Issue 18, Issue 7

### Tasks:
- [ ] Update `NotificationMemory` class to use database
- [ ] Replace in-memory lists with database calls
- [ ] Update methods:
  - `store()` → calls `create_memory()`
  - `get_all()` → calls `get_memories_for_user()`
  - `search()` → uses database queries
- [ ] Maintain same interface (backward compatible)
- [ ] Add database session parameter

### Acceptance Criteria:
- ✅ Memory manager uses database instead of in-memory
- ✅ Interface remains the same
- ✅ All methods work with database
- ✅ No breaking changes to agent code

### Test:
```python
from focus_filter.memory.memory_manager import NotificationMemory
from app.database.crud import get_db_session

session = get_db_session()
memory = NotificationMemory(session)
notif = Notification(...)
memory.store(notif, "Test fact")
assert len(memory.get_all(user_id="test_user")) == 1
print("✅ Database integration OK")
```

### Commit Message:
```
feat: Integrate database with memory manager

- Replace in-memory storage with database
- Maintain backward-compatible interface
- Update all memory operations to use CRUD
```

---

## Issue 20: Add API Authentication (Basic)
**Estimated Time**: 1.5 hours  
**Priority**: Medium  
**Dependencies**: Issue 14

### Tasks:
- [ ] Create API key authentication:
  ```python
  from fastapi import Header, HTTPException
  
  async def verify_api_key(x_api_key: str = Header(...)):
      if x_api_key != os.getenv("API_KEY"):
          raise HTTPException(status_code=401, detail="Invalid API key")
      return x_api_key
  ```
- [ ] Add API key to `.env.example`
- [ ] Protect endpoints with authentication dependency
- [ ] Add error responses for unauthorized requests

### Acceptance Criteria:
- ✅ API endpoints require API key
- ✅ Invalid API key returns 401
- ✅ API key configurable via environment

### Test:
```bash
# Should fail without API key
curl http://localhost:8000/api/v1/classify

# Should work with API key
curl -X POST http://localhost:8000/api/v1/classify \
  -H "X-API-Key: test_key" \
  -H "Content-Type: application/json" \
  -d '{...}'
```

### Commit Message:
```
feat: Add basic API key authentication

- Implement API key header authentication
- Protect all API endpoints
- Add to environment configuration
```

---

## Issue 21: Add Error Handling & Validation
**Estimated Time**: 1.5 hours  
**Priority**: High  
**Dependencies**: Issue 15

### Tasks:
- [ ] Create custom exception classes:
  ```python
  class AgentError(Exception):
      pass
  
  class ClassificationError(AgentError):
      pass
  ```
- [ ] Add global exception handler:
  ```python
  @app.exception_handler(AgentError)
  async def agent_error_handler(request, exc):
      return JSONResponse(
          status_code=500,
          content={"error": str(exc)}
      )
  ```
- [ ] Add input validation:
  - Check required fields
  - Validate string lengths
  - Sanitize inputs
- [ ] Add error logging

### Acceptance Criteria:
- ✅ Custom exceptions for different error types
- ✅ Global error handler returns proper responses
- ✅ Input validation prevents invalid requests
- ✅ Errors are logged with context

### Test:
```bash
# Should return 400 for invalid input
curl -X POST http://localhost:8000/api/v1/classify \
  -H "Content-Type: application/json" \
  -d '{"app": ""}'  # Missing required fields
```

### Commit Message:
```
feat: Add comprehensive error handling

- Create custom exception classes
- Add global exception handlers
- Add input validation
- Improve error logging
```

---

## Issue 22: Create Basic Unit Tests
**Estimated Time**: 2 hours  
**Priority**: High  
**Dependencies**: Issue 15

### Tasks:
- [ ] Set up pytest configuration
- [ ] Create `tests/test_models.py`:
  - Test Notification model
  - Test MemoryEntry model
- [ ] Create `tests/test_tools.py`:
  - Test notification tools (mocked)
- [ ] Create `tests/test_api.py`:
  - Test health check endpoint
  - Test classification endpoint (mocked agent)
  - Test process endpoint (mocked agent)
- [ ] Add test fixtures
- [ ] Add pytest to `requirements.txt` (if not already)

### Acceptance Criteria:
- ✅ Tests can run with `pytest`
- ✅ Models are tested
- ✅ API endpoints are tested (with mocks)
- ✅ Test coverage > 60%

### Test:
```bash
# Run tests in container
docker-compose run --rm backend pytest tests/ -v

# Or in DevContainer terminal
pytest tests/ -v
```

### Commit Message:
```
feat: Add basic unit tests

- Set up pytest configuration
- Test models and tools
- Test API endpoints with mocks
- Add test fixtures
```

---

## Issue 23: Create Production Dockerfiles
**Estimated Time**: 1.5 hours  
**Priority**: Medium  
**Dependencies**: Issue 15

### Tasks:
- [ ] Create `Dockerfile` (CPU production):
  ```dockerfile
  FROM python:3.11-slim
  
  WORKDIR /app
  
  # Install system dependencies if needed
  RUN apt-get update && apt-get install -y --no-install-recommends \
      && rm -rf /var/lib/apt/lists/*
  
  # Copy and install Python dependencies
  COPY requirements.txt .
  RUN pip install --no-cache-dir -r requirements.txt
  
  # Copy application code
  COPY . .
  
  # Expose port
  EXPOSE 8000
  
  # Run application
  CMD ["uvicorn", "app.main:app", "--host", "0.0.0.0", "--port", "8000"]
  ```
- [ ] Create `Dockerfile.gpu` (GPU production):
  ```dockerfile
  FROM nvidia/cuda:12.1.0-runtime-ubuntu22.04
  
  # Install Python 3.11
  RUN apt-get update && apt-get install -y --no-install-recommends \
      python3.11 \
      python3.11-dev \
      python3-pip \
      && rm -rf /var/lib/apt/lists/*
  
  RUN ln -s /usr/bin/python3.11 /usr/bin/python
  
  WORKDIR /app
  
  # Copy and install Python dependencies
  COPY requirements.txt .
  RUN pip install --no-cache-dir -r requirements.txt
  
  # Copy application code
  COPY . .
  
  EXPOSE 8000
  
  CMD ["uvicorn", "app.main:app", "--host", "0.0.0.0", "--port", "8000"]
  ```
- [ ] Update `docker-compose.yml` to use production Dockerfile (optional)
- [ ] Test both Dockerfiles build successfully

### Acceptance Criteria:
- ✅ Both Dockerfiles build successfully
- ✅ CPU Dockerfile runs and serves API
- ✅ GPU Dockerfile runs (if GPU available)
- ✅ Health check works in both containers
- ✅ Containers are optimized for production

### Test:
```bash
# Test CPU build
docker build -t focus-filter-api:cpu -f Dockerfile .
docker run -d -p 8000:8000 -e GOOGLE_API_KEY=test --name test-api focus-filter-api:cpu
sleep 2
curl http://localhost:8000/health
docker stop test-api && docker rm test-api

# Test GPU build (if GPU available)
docker build -t focus-filter-api:gpu -f Dockerfile.gpu .
docker run -d --gpus all -p 8001:8000 -e GOOGLE_API_KEY=test --name test-api-gpu focus-filter-api:gpu
sleep 2
curl http://localhost:8001/health
docker stop test-api-gpu && docker rm test-api-gpu
```

### Commit Message:
```
feat: Add production Dockerfiles (CPU and GPU)

- Create CPU-optimized Dockerfile
- Create GPU-enabled Dockerfile
- Support both deployment scenarios
- Optimize for production use
```

---

## Issue 24: Create README and Documentation
**Estimated Time**: 1.5 hours  
**Priority**: Medium  
**Dependencies**: Issue 23

### Tasks:
- [ ] Create `backend/README.md` with:
  - Project overview
  - Setup instructions (DevContainer and Docker Compose)
  - GPU vs CPU setup instructions
  - API documentation
  - Environment variables
  - Running locally (DevContainer)
  - Running with Docker Compose
  - Testing instructions
  - GPU detection and usage
- [ ] Add API endpoint documentation
- [ ] Add example requests/responses
- [ ] Add troubleshooting section (GPU issues, container issues)
- [ ] Document how to switch between CPU and GPU modes

### Acceptance Criteria:
- ✅ README is comprehensive
- ✅ Setup instructions are clear
- ✅ API documentation is complete
- ✅ Examples are provided

### Commit Message:
```
docs: Add comprehensive README

- Document setup and installation
- Add API endpoint documentation
- Include examples and troubleshooting
```

---

## Issue 25: Local Testing & Validation
**Estimated Time**: 2 hours  
**Priority**: Critical  
**Dependencies**: All previous issues

### Tasks:
- [ ] Test full pipeline in container:
  - Start API server in DevContainer or docker-compose
  - Send test notification
  - Verify classification works
  - Verify full processing works
  - Verify database storage works
- [ ] Test error cases:
  - Invalid API key
  - Missing fields
  - Agent errors
- [ ] Test with multiple notifications
- [ ] Verify logging works
- [ ] Check database persistence
- [ ] Test GPU mode (if available):
  - Verify GPU detection works
  - Test with GPU-enabled compose file
  - Verify fallback to CPU if GPU unavailable

### Acceptance Criteria:
- ✅ All endpoints work correctly
- ✅ Full pipeline processes notifications
- ✅ Database stores data correctly
- ✅ Error handling works
- ✅ Logging captures events

### Test Checklist:
- [ ] Health endpoint works
- - Classification endpoint works
- - Process endpoint works
- - Database stores notifications
- - Database stores memories
- - Error handling works
- - Logging works

### Commit Message:
```
test: Validate complete system locally

- Test all API endpoints
- Verify database integration
- Test error handling
- Validate logging
```

---

## Issue 26: Prepare for Deployment
**Estimated Time**: 1 hour  
**Priority**: Medium  
**Dependencies**: Issue 25

### Tasks:
- [ ] Create deployment documentation
- [ ] Document environment variables needed
- [ ] Create example deployment configs:
  - Railway
  - Render
  - Cloud Run
- [ ] Add production considerations:
  - Database (PostgreSQL vs SQLite)
  - Logging (structured logs)
  - Monitoring
  - Rate limiting (future)

### Acceptance Criteria:
- ✅ Deployment instructions documented
- ✅ Environment variables documented
- ✅ Multiple deployment options provided

### Commit Message:
```
docs: Add deployment documentation

- Document deployment options
- Add environment variable guide
- Include production considerations
```

---

## Summary Checklist

### Week 1 Goals - Backend API Service

- [x] **Containerization Setup** (Issues 1-2)
  - [x] DevContainer configuration
  - [x] Docker Compose (CPU and GPU)
  - [x] GPU detection
  - [x] Project structure
- [x] **Extract Core Logic** (Issues 4-12)
  - [x] Data models
  - [x] Tools
  - [x] Memory manager
  - [x] Context builder
  - [x] All three agents
  - [x] Orchestrator
- [x] **Create FastAPI Backend** (Issues 13-15)
  - [x] Basic app setup
  - [x] Classification endpoint
  - [x] Full processing endpoint
- [x] **Database Integration** (Issues 17-19)
  - [x] Database models
  - [x] CRUD operations
  - [x] Memory manager integration
- [x] **Polish & Testing** (Issues 16, 20-26)
  - [x] Logging
  - [x] Authentication
  - [x] Error handling
  - [x] Unit tests
  - [x] Production Dockerfiles (CPU and GPU)
  - [x] Documentation
  - [x] Local validation

---

## Daily Breakdown

### Day 1 (Issues 1-3)
- DevContainer and Docker setup
- Project structure
- Dependencies and requirements

### Day 2 (Issues 4-8)
- Extract data models
- Extract tools and memory manager
- Extract context builder

### Day 3 (Issues 9-12)
- Extract all three agents
- Create orchestrator
- Test agent pipeline in container

### Day 4 (Issues 13-16)
- Create FastAPI app
- Create API endpoints
- Add logging
- Test API in container

### Day 5 (Issues 17-26)
- Add database
- Add authentication
- Add error handling
- Write tests
- Create production Dockerfiles
- Write documentation
- Final validation

---

## Testing Strategy

### After Each Issue:
1. Run the test command provided
2. Verify acceptance criteria
3. Commit with provided message
4. Move to next issue

### Integration Testing:
- After Issue 11: Test full agent pipeline
- After Issue 14: Test full API
- After Issue 18: Test with database
- After Issue 24: Full system validation

---

## Notes

- **Containerization First**: All development happens in containers (DevContainer or Docker Compose)
- **GPU Support**: Optional, automatically detected. Falls back to CPU if unavailable
- **Development Workflow**: 
  - Use DevContainer in VS Code/Cursor for best experience
  - Use `docker-compose up` for quick testing
  - Use `docker-compose -f docker-compose.gpu.yml up` if GPU available
- **API Key Management**: Use environment variables, never commit keys
- **Database**: Start with SQLite for development, PostgreSQL for production
- **Error Handling**: Always return proper HTTP status codes
- **Logging**: Log all important operations for debugging
- **Testing**: Write tests as you go, don't leave for the end
- **Commits**: Commit after each issue completion
- **Portability**: Code works on both powerful GPU machines and low-power laptops

---

## Questions?

If you encounter issues or need clarification on any step, document them and we can adjust the plan accordingly.
