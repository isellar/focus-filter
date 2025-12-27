# Android Prototype Implementation Plan
## Focus Filter - From Demo to Working Android App

### Overview
This plan outlines the steps to transform the Focus Filter demo (currently running in Kaggle notebooks with simulated notifications) into a working Android prototype that can actually filter real notifications on your phone.

---

## Current State Analysis

### What Works (Demo)
- ✅ Multi-agent system architecture (Classification → Action → Memory)
- ✅ Google ADK integration with Gemini 2.0 Flash
- ✅ Notification classification logic (URGENT / IRRELEVANT / LESS_URGENT)
- ✅ Memory management with deduplication
- ✅ Observability and logging
- ✅ User preference learning

### Current Shortcuts (To Be Replaced)
- ❌ **Simulated notifications** → Need real Android NotificationListenerService
- ❌ **In-memory storage** → Need persistent database (SQLite/Room)
- ❌ **Jupyter notebook execution** → Need Android app with background service
- ❌ **Kaggle-specific API key loading** → Need secure Android credential storage
- ❌ **No UI** → Need Android UI for viewing filtered notifications and settings
- ❌ **No notification blocking** → Need Android notification cancellation API

---

## Architecture Overview

### Proposed System Architecture

```
┌─────────────────────────────────────────────────────────────┐
│                    Android Device                            │
│                                                              │
│  ┌──────────────┐      ┌──────────────────────────────┐    │
│  │   Android    │      │   Focus Filter App           │    │
│  │ Notification │─────▶│   - MainActivity (UI)        │    │
│  │   System     │      │   - SettingsActivity         │    │
│  └──────────────┘      │   - NotificationListActivity │    │
│                        └──────────────┬─────────────────┘    │
│                                       │                       │
│                        ┌──────────────▼─────────────────┐    │
│                        │  NotificationListenerService   │    │
│                        │  (Background Service)          │    │
│                        └──────────────┬─────────────────┘    │
│                                       │                       │
│                        ┌──────────────▼─────────────────┐    │
│                        │   Agent Processing Service      │    │
│                        │   - Classification Agent        │    │
│                        │   - Action Agent                │    │
│                        │   - Memory Agent                │    │
│                        └──────────────┬─────────────────┘    │
│                                       │                       │
│                        ┌──────────────▼─────────────────┐    │
│                        │   Local Database (Room/SQLite)  │    │
│                        │   - Notifications               │    │
│                        │   - Memories                    │    │
│                        │   - Preferences                 │    │
│                        └──────────────┬─────────────────┘    │
│                                       │                       │
│                        ┌──────────────▼─────────────────┐    │
│                        │   API Client (Retrofit/OkHttp) │    │
│                        └──────────────┬─────────────────┘    │
└───────────────────────────────────────┼───────────────────────┘
                                        │
                                        │ HTTPS
                                        ▼
                        ┌───────────────────────────────┐
                        │   Backend API Service         │
                        │   (Python FastAPI/Flask)      │
                        │   - Google ADK Integration    │
                        │   - Gemini API Calls          │
                        │   - Agent Orchestration       │
                        └───────────────────────────────┘
```

---

## Implementation Phases

### Phase 1: Backend API Service (Week 1)
**Goal**: Extract agent logic from notebook into a deployable, containerized API service

#### Tasks:
1. **Containerization Setup** ⭐ **NEW - Container-First Approach**
   - [ ] Set up DevContainer for VS Code/Cursor
   - [ ] Create Docker Compose configuration (CPU and GPU variants)
   - [ ] Add GPU detection and automatic fallback to CPU
   - [ ] Configure development environment in containers
   - [ ] Ensure portability between powerful GPU machines and low-power laptops

2. **Extract Core Agent Logic**
   - [ ] Create Python package structure (`focus_filter/`)
   - [ ] Extract agent classes from notebook:
     - Classification Agent
     - Action Agent  
     - Memory Agent
   - [ ] Extract tool functions (display_urgent, block, save_memory)
   - [ ] Extract memory management classes
   - [ ] **All development and testing happens in containers**

3. **Create FastAPI Backend**
   - [ ] Set up FastAPI project structure
   - [ ] Create `/classify` endpoint:
     ```python
     POST /api/v1/classify
     {
       "app": "string",
       "title": "string", 
       "body": "string",
       "user_id": "string"
     }
     ```
   - [ ] Create `/process` endpoint (full multi-agent pipeline)
   - [ ] Add authentication/API key management
   - [ ] Add error handling and logging
   - [ ] **API runs and tested in containers**

4. **Database Integration**
   - [ ] Replace in-memory storage with SQLite/PostgreSQL
   - [ ] Create database models:
     - Notifications table
     - Memories table
     - User preferences table
   - [ ] Implement persistence layer
   - [ ] **Database runs in container or as separate service**

5. **Production Dockerfiles**
   - [ ] Create CPU-optimized production Dockerfile
   - [ ] Create GPU-enabled production Dockerfile (optional)
   - [ ] Optimize for deployment
   - [ ] Test both variants

6. **Deployment**
   - [ ] Deploy to Cloud Run / Railway / Render
   - [ ] Set up environment variables (GOOGLE_API_KEY)
   - [ ] Add health check endpoint
   - [ ] Test API endpoints with Postman/curl
   - [ ] **Deploy using container images**

**Deliverable**: Working API service that can classify notifications, fully containerized and portable

---

### Phase 2: Android App Foundation (Week 2)
**Goal**: Create basic Android app structure with notification listener

#### Tasks:
1. **Project Setup**
   - [ ] Create new Android Studio project (Kotlin)
   - [ ] Set minimum SDK 26 (Android 8.0) for NotificationListenerService
   - [ ] Add dependencies:
     - Retrofit for API calls
     - Room for local database
     - Coroutines for async operations
     - WorkManager for background tasks

2. **NotificationListenerService**
   - [ ] Create `FocusFilterNotificationService` extending `NotificationListenerService`
   - [ ] Request notification access permission
   - [ ] Implement `onNotificationPosted()` to intercept notifications
   - [ ] Extract notification data (app, title, body, icon)
   - [ ] Cancel original notification (to prevent duplicate)

3. **Basic UI**
   - [ ] MainActivity with:
     - Button to enable notification access
     - Status indicator (service running/stopped)
     - Link to settings
   - [ ] SettingsActivity:
     - API endpoint configuration
     - API key input (secure storage)
     - Enable/disable filtering toggle

4. **API Integration**
   - [ ] Create Retrofit interface for backend API
   - [ ] Create data models (Notification, ClassificationResult)
   - [ ] Implement API client with error handling
   - [ ] Add network request logging

**Deliverable**: Android app that can intercept notifications and call backend API

---

### Phase 3: Agent Integration & Processing (Week 3)
**Goal**: Connect Android app to backend agents and process notifications

#### Tasks:
1. **Notification Processing Flow**
   - [ ] Create `NotificationProcessor` class
   - [ ] Implement async processing:
     - Intercept notification
     - Call `/classify` endpoint
     - Handle response (URGENT/IRRELEVANT/LESS_URGENT)
     - Execute action based on classification

2. **Action Implementation**
   - [ ] **URGENT**: Re-display notification with priority
     - Use NotificationManager to create new notification
     - Add custom channel "Focus Filter - Urgent"
     - High priority, sound, vibration
   - [ ] **IRRELEVANT**: Block (do nothing, already cancelled)
   - [ ] **LESS_URGENT**: Store in local database
     - Call `/process` endpoint for memory extraction
     - Save to Room database

3. **Local Database**
   - [ ] Create Room entities:
     - `NotificationEntity` (all intercepted notifications)
     - `MemoryEntity` (stored facts from less urgent)
     - `PreferenceEntity` (user preferences)
   - [ ] Create DAOs for database operations
   - [ ] Create Repository pattern for data access

4. **Background Processing**
   - [ ] Use WorkManager for reliable background processing
   - [ ] Handle network failures gracefully
   - [ ] Queue notifications if API is unavailable
   - [ ] Retry logic with exponential backoff

**Deliverable**: App that filters notifications using backend agents

---

### Phase 4: UI & User Experience (Week 4)
**Goal**: Build user interface for viewing filtered notifications and managing settings

#### Tasks:
1. **Notification History Screen**
   - [ ] RecyclerView showing:
     - Urgent notifications (highlighted)
     - Blocked notifications (collapsed, expandable)
     - Stored memories (searchable)
   - [ ] Filter options (by app, by classification, by date)
   - [ ] Search functionality

2. **Memory Viewer**
   - [ ] Activity showing stored memories
   - [ ] Search and filter memories
   - [ ] View original notification context
   - [ ] Delete/consolidate memories

3. **Settings & Preferences**
   - [ ] API configuration (endpoint, API key)
   - [ ] App-specific rules:
     - Always urgent apps
     - Always blocked apps
   - [ ] Time-based rules (e.g., quiet hours)
   - [ ] Statistics dashboard:
     - Notifications processed today
     - Classification breakdown
     - Time saved estimate

4. **Notification Channels**
   - [ ] Create custom notification channels:
     - "Focus Filter - Urgent"
     - "Focus Filter - Summary" (daily digest)
   - [ ] Allow user to customize channel settings

**Deliverable**: Complete UI for managing filtered notifications

---

### Phase 5: Polish & Optimization (Week 5)
**Goal**: Improve reliability, performance, and user experience

#### Tasks:
1. **Performance Optimization**
   - [ ] Optimize API calls (batch processing for multiple notifications)
   - [ ] Cache classification results for similar notifications
   - [ ] Implement local classification fallback (simple rule-based)
   - [ ] Reduce battery impact (efficient background processing)

2. **Error Handling & Resilience**
   - [ ] Handle API failures gracefully
   - [ ] Offline mode (queue notifications, process when online)
   - [ ] Rate limiting protection
   - [ ] User feedback for errors

3. **Security**
   - [ ] Secure API key storage (Android Keystore)
   - [ ] Encrypt sensitive data in database
   - [ ] HTTPS only for API calls
   - [ ] Privacy: local processing option

4. **Testing**
   - [ ] Unit tests for notification processing logic
   - [ ] Integration tests for API calls
   - [ ] UI tests for critical flows
   - [ ] Test on multiple Android versions

5. **Documentation**
   - [ ] User guide (how to set up and use)
   - [ ] Developer documentation
   - [ ] API documentation
   - [ ] Troubleshooting guide

**Deliverable**: Polished, production-ready prototype

---

## Technical Stack

### Backend
- **Language**: Python 3.11+
- **Framework**: FastAPI
- **Agent Framework**: Google ADK (Agent Developer Kit)
- **LLM**: Gemini 2.0 Flash (via Google API)
- **Database**: SQLite (development) / PostgreSQL (production)
- **Containerization**: 
  - DevContainers for development (VS Code/Cursor)
  - Docker Compose for local orchestration
  - Production Dockerfiles (CPU and GPU variants)
- **GPU Support**: Optional NVIDIA GPU acceleration with automatic CPU fallback
- **Deployment**: Cloud Run / Railway / Render (container-based)

### Android
- **Language**: Kotlin
- **Min SDK**: 26 (Android 8.0)
- **Architecture**: MVVM with Repository pattern
- **Libraries**:
  - Retrofit + OkHttp (API calls)
  - Room (local database)
  - Coroutines + Flow (async operations)
  - WorkManager (background tasks)
  - Material Design Components (UI)

---

## Containerization Strategy

### Why Containerization First?

**Development Benefits:**
- ✅ **Consistent Environment**: Same environment on powerful GPU machine and low-power laptop
- ✅ **Easy Setup**: New developers can start immediately with DevContainer
- ✅ **Isolation**: No conflicts with system Python or dependencies
- ✅ **Reproducibility**: Exact same environment for all developers
- ✅ **GPU Flexibility**: Automatic detection and fallback

**GPU Support:**
- **Automatic Detection**: Script detects NVIDIA GPU availability
- **Dual Mode**: Separate Docker Compose files for CPU and GPU
- **Graceful Fallback**: Automatically uses CPU if GPU unavailable
- **Future-Proof**: Ready for local LLM models if needed later

**Workflow:**
1. **Development**: Use DevContainer in VS Code/Cursor (recommended)
2. **Quick Testing**: Use `docker-compose up` for CPU mode
3. **GPU Testing**: Use `docker-compose -f docker-compose.gpu.yml up` if GPU available
4. **Production**: Deploy using production Dockerfiles

**Portability:**
- Works seamlessly on:
  - Powerful GPU workstations (uses GPU when available)
  - Low-power laptops (automatically falls back to CPU)
  - Cloud deployments (container-based)
  - CI/CD pipelines (consistent containers)

---

## Key Challenges & Solutions

### Challenge 1: Notification Access Permission
**Problem**: Android requires special permission to access notifications, and users must manually enable it in system settings.

**Solution**: 
- Create clear onboarding flow with screenshots
- Detect when permission is missing and show dialog
- Deep link to notification access settings

### Challenge 2: Battery Life
**Problem**: Processing every notification through API calls could drain battery.

**Solution**:
- Batch process notifications (every 30 seconds)
- Cache similar notifications
- Use WorkManager with constraints (only on WiFi, charging)
- Implement local rule-based filtering for common cases

### Challenge 3: API Costs
**Problem**: Google Gemini API calls cost money per request.

**Solution**:
- Implement caching for similar notifications
- Rate limiting per user
- Local classification for obvious cases (spam patterns)
- Batch API calls when possible

### Challenge 4: Notification Cancellation
**Problem**: Android doesn't allow apps to cancel notifications from other apps directly.

**Solution**:
- Cancel notification immediately upon intercept
- Re-display only if classified as URGENT
- For IRRELEVANT, simply don't re-display

### Challenge 5: Background Processing
**Problem**: Android limits background processing to save battery.

**Solution**:
- Use foreground service for NotificationListenerService
- Use WorkManager for reliable background tasks
- Request appropriate permissions and exemptions

---

## Success Criteria

### MVP (Minimum Viable Product)
- ✅ App can intercept Android notifications
- ✅ Notifications are sent to backend API for classification
- ✅ Urgent notifications are re-displayed
- ✅ Irrelevant notifications are blocked
- ✅ Less urgent notifications are stored locally
- ✅ Basic UI to view notification history
- ✅ Settings to configure API endpoint

### Enhanced Features (Post-MVP)
- [ ] Daily summary of stored memories
- [ ] App-specific rules and preferences
- [ ] Offline mode with queuing
- [ ] Statistics and analytics
- [ ] Widget for quick stats
- [ ] Export/import settings

---

## File Structure

```
focus-filter/
├── backend/
│   ├── .devcontainer/
│   │   ├── devcontainer.json
│   │   └── Dockerfile
│   ├── app/
│   │   ├── main.py              # FastAPI app
│   │   ├── agents/
│   │   │   ├── classification_agent.py
│   │   │   ├── action_agent.py
│   │   │   └── memory_agent.py
│   │   ├── models/
│   │   │   ├── notification.py
│   │   │   └── memory.py
│   │   ├── database/
│   │   │   ├── models.py
│   │   │   └── crud.py
│   │   └── api/
│   │       └── routes.py
│   ├── docker-compose.yml       # CPU-only development
│   ├── docker-compose.gpu.yml   # GPU-enabled development
│   ├── Dockerfile               # Production CPU
│   ├── Dockerfile.gpu           # Production GPU
│   ├── requirements.txt
│   └── README.md
│
├── android/
│   ├── app/
│   │   ├── src/main/java/com/focusfilter/
│   │   │   ├── MainActivity.kt
│   │   │   ├── service/
│   │   │   │   └── FocusFilterNotificationService.kt
│   │   │   ├── data/
│   │   │   │   ├── api/
│   │   │   │   ├── database/
│   │   │   │   └── repository/
│   │   │   ├── ui/
│   │   │   │   ├── notifications/
│   │   │   │   ├── memories/
│   │   │   │   └── settings/
│   │   │   └── util/
│   │   └── build.gradle.kts
│   └── README.md
│
└── ANDROID_PROTOTYPE_PLAN.md (this file)
```

---

## Questions to Consider

1. **Backend Deployment**: 
   - Where will you host the backend? (Cloud Run, Railway, Render, self-hosted?)
   - Budget for API calls? (Gemini API costs)

2. **Offline Capability**:
   - Should the app work offline with local rules?
   - How to handle queued notifications when back online?

3. **Privacy**:
   - Should notification content be encrypted before sending to API?
   - Option for fully local processing (no API calls)?

4. **User Onboarding**:
   - How to make notification access permission setup easy?
   - Tutorial/onboarding flow needed?

5. **Testing Strategy**:
   - How to test without spamming your own phone?
   - Need test notification generator?

---

## Next Steps

1. **Review this plan** and provide feedback
2. **Choose backend hosting** (I recommend Railway or Render for easy deployment)
3. **Set up development environment**:
   - Android Studio
   - Python environment for backend
4. **Start with Phase 1** (Backend API) - extract agent logic from notebook
5. **Iterate** based on testing and feedback

---

## Estimated Timeline

- **Phase 1 (Backend)**: 1 week
- **Phase 2 (Android Foundation)**: 1 week  
- **Phase 3 (Agent Integration)**: 1 week
- **Phase 4 (UI)**: 1 week
- **Phase 5 (Polish)**: 1 week

**Total**: ~5 weeks for working prototype

---

## Resources

- [Android NotificationListenerService Documentation](https://developer.android.com/reference/android/service/notification/NotificationListenerService)
- [Google ADK Documentation](https://github.com/google/agentic-developer-kit)
- [FastAPI Documentation](https://fastapi.tiangolo.com/)
- [Android Room Database](https://developer.android.com/training/data-storage/room)
- [Retrofit Documentation](https://square.github.io/retrofit/)

---

**Ready to start?** Let me know if you have questions or want to adjust the plan!
