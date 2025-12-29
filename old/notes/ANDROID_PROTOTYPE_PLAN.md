# Android Prototype Implementation Plan
## Focus Filter - From Demo to Working Android App

### Overview
This plan outlines the steps to transform the Focus Filter demo (currently running in Kaggle notebooks with simulated notifications) into a working Android prototype that can actually filter real notifications on your phone.

**Updated with Android/Google Ecosystem Enhancements** (see `enhancements.md`)

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
- ❌ **In-memory storage** → Need persistent database (AppSearch for vector search)
- ❌ **Jupyter notebook execution** → Need Android app with background service
- ❌ **Cloud-only LLM** → Use Gemini Nano (on-device) with cloud fallback
- ❌ **No UI** → Need Android UI for viewing filtered notifications and settings
- ❌ **No notification blocking** → Use NotificationAssistantService for re-ranking

---

## Enhanced Architecture Overview

### Agentic Loop Architecture (from enhancements.md)

```
┌─────────────────────────────────────────────────────────────┐
│                    Android Device                            │
│                                                              │
│  ┌──────────────────────────────────────────────────────┐  │
│  │              INPUT INTERCEPTOR                        │  │
│  │  NotificationListenerService (intercepts all)         │  │
│  └──────────────────┬───────────────────────────────────┘  │
│                       │                                       │
│  ┌───────────────────▼───────────────────────────────────┐  │
│  │              CONTEXT PROVIDER                           │  │
│  │  - Google Awareness API (activity, weather, location)  │  │
│  │  - Health Connect (well-being context)                  │  │
│  │  - Calendar Provider (meetings, events)                │  │
│  └───────────────────┬───────────────────────────────────┘  │
│                       │                                       │
│  ┌───────────────────▼───────────────────────────────────┐  │
│  │              REASONING ENGINE                          │  │
│  │  Primary: Gemini Nano (on-device via AICore)           │  │
│  │  Fallback: Backend API (cloud Gemini 2.0 Flash)         │  │
│  │  Prompt: "Given [Context], should I alert about        │  │
│  │           [Notification]?"                              │  │
│  └───────────────────┬───────────────────────────────────┘  │
│                       │                                       │
│  ┌───────────────────▼───────────────────────────────────┐  │
│  │              EFFECTOR (Actions)                        │  │
│  │  - NotificationAssistantService (re-rank, smart replies)│  │
│  │  - ZenMode APIs (DND control)                          │  │
│  │  - App Actions (deep-link to Calendar, etc.)            │  │
│  │  - Agent Summary Notification (Shadow Shade)           │  │
│  └───────────────────┬───────────────────────────────────┘  │
│                       │                                       │
│  ┌───────────────────▼───────────────────────────────────┐  │
│  │              MEMORY & STORAGE                          │  │
│  │  - AppSearch (vector search, full-text search)          │  │
│  │  - Room (fallback for simple queries)                   │  │
│  └────────────────────────────────────────────────────────┘  │
│                                                              │
│  ┌──────────────────────────────────────────────────────┐  │
│  │              SHADOW SHADE (UI Strategy)               │  │
│  │  - Don't replace system notification shade             │  │
│  │  - Provide single "Agent Summary" notification         │  │
│  │  - Shows: "Silenced 4 shopping alerts, highlighted     │  │
│  │           1 urgent message from boss"                  │  │
│  └────────────────────────────────────────────────────────┘  │
└──────────────────────────────────────────────────────────────┘
                        │
                        │ (Optional Fallback)
                        ▼
        ┌───────────────────────────────┐
        │   Backend API Service         │
        │   (Python FastAPI)            │
        │   - Cloud Gemini 2.0 Flash    │
        │   - Used when Nano unavailable│
        │   - Used for complex reasoning │
        └───────────────────────────────┘
```

---

## Implementation Phases

### Phase 1: Backend API Service (Week 1) ✅ COMPLETE
**Goal**: Extract agent logic from notebook into a deployable, containerized API service

**Status**: ✅ **COMPLETE** - Backend is ready and validated

**Note**: Backend now serves as **optional fallback** for on-device reasoning. Primary reasoning will be on-device with Gemini Nano.

---

### Phase 2: Android App Foundation (Week 2)
**Goal**: Create basic Android app structure with notification listener and on-device reasoning

#### Tasks:
1. **Project Setup**
   - [ ] Create new Android Studio project (Kotlin)
   - [ ] Set minimum SDK 26 (Android 8.0) for NotificationListenerService
   - [ ] Set target SDK 34 (Android 14) for latest features
   - [ ] Add dependencies:
     - **AICore** (Gemini Nano on-device)
     - **AppSearch** (vector search, full-text search)
     - **Awareness API** (context provider)
     - **Health Connect** (well-being context)
     - Retrofit + OkHttp (for backend fallback)
     - Room (simple queries, AppSearch fallback)
     - Coroutines + Flow (async operations)
     - WorkManager (background tasks)
     - Material Design Components (UI)

2. **NotificationListenerService**
   - [ ] Create `FocusFilterNotificationService` extending `NotificationListenerService`
   - [ ] Request notification access permission
   - [ ] Implement `onNotificationPosted()` to intercept notifications
   - [ ] Extract notification data (app, title, body, icon, quick replies)
   - [ ] Cancel original notification (to prevent duplicate)

3. **Context Provider Setup**
   - [ ] Integrate Google Awareness API:
     - Activity detection (walking, driving, still)
     - Weather context
     - Location fences
     - Headphone state
   - [ ] Integrate Health Connect (optional):
     - Workout detection
     - Sleep window detection
   - [ ] Integrate Calendar Provider:
     - Current/upcoming meetings
     - Event context

4. **On-Device Reasoning Engine**
   - [ ] Set up AICore SDK for Gemini Nano
   - [ ] Create `OnDeviceReasoningEngine` class
   - [ ] Build context-aware prompts:
     ```kotlin
     "Given context: [Activity: walking, Weather: raining, Meeting in 10min],
      should I alert the user about this notification: [Title, Body, App]?"
     ```
   - [ ] Implement classification (URGENT/IRRELEVANT/LESS_URGENT)
   - [ ] Add confidence scoring
   - [ ] Fallback to backend API if Nano unavailable

5. **Basic UI**
   - [ ] MainActivity with:
     - Button to enable notification access
     - Status indicator (service running/stopped)
     - Link to settings
     - Agent summary display
   - [ ] SettingsActivity:
     - Enable/disable filtering toggle
     - On-device vs cloud reasoning preference
     - Backend API endpoint (for fallback)
     - API key input (secure storage)
     - Context provider toggles (Awareness, Health Connect)

6. **Storage Setup**
   - [ ] Set up AppSearch for notification memory:
     - Vector embeddings for semantic search
     - Full-text search for keywords
     - Notification history indexing
   - [ ] Set up Room (fallback):
     - Simple notification storage
     - User preferences
   - [ ] Create Repository pattern (abstracts AppSearch/Room)

**Deliverable**: Android app that intercepts notifications and classifies them on-device

---

### Phase 3: Agent Integration & Advanced Actions (Week 3)
**Goal**: Connect full agent pipeline with enhanced Android actions

#### Tasks:
1. **Notification Processing Flow**
   - [ ] Create `NotificationProcessor` class
   - [ ] Implement agentic loop:
     - Intercept notification
     - Gather context (Awareness API, Health Connect, Calendar)
     - Run on-device reasoning (Gemini Nano)
     - Fallback to backend API if needed
     - Execute action based on classification

2. **Enhanced Action Implementation**
   - [ ] **URGENT**: 
     - Use NotificationAssistantService to promote notification
     - Generate smart replies via Gemini Nano
     - High priority, sound, vibration
   - [ ] **IRRELEVANT**: 
     - Use NotificationAssistantService to suppress
     - Don't re-display (already cancelled)
   - [ ] **LESS_URGENT**: 
     - Store in AppSearch with embeddings
     - Extract memory via on-device or cloud
     - Queue for daily summary

3. **NotificationAssistantService**
   - [ ] Extend `NotificationAssistantService`
   - [ ] Implement `onNotificationSnoozedUntilContext()` for smart snoozing
   - [ ] Implement `onNotificationsSeen()` for learning
   - [ ] Re-rank notifications based on agent reasoning
   - [ ] Generate smart replies using Gemini Nano

4. **Device Controls**
   - [ ] Integrate ZenMode APIs:
     - Programmatically enable DND based on context
     - Time-based rules (quiet hours)
     - Location-based rules (work, home)
   - [ ] App Actions integration:
     - Deep-link to Calendar for meeting notifications
     - Deep-link to other apps based on notification type

5. **Agent Summary Notification (Shadow Shade)**
   - [ ] Create persistent "Agent Summary" notification
   - [ ] Update summary with:
     - Count of silenced notifications
     - Count of highlighted urgent notifications
     - Recent actions taken
   - [ ] Make it expandable to show details
   - [ ] Keep at top of notification shade

6. **Memory & Search**
   - [ ] Store notifications in AppSearch with:
     - Vector embeddings (for semantic search)
     - Full-text indexing (for keyword search)
     - Metadata (app, timestamp, classification)
   - [ ] Implement memory search:
     - "Find notifications about meetings"
     - "Show shopping notifications from last week"
   - [ ] Pattern detection across notifications

**Deliverable**: App that intelligently filters notifications using on-device AI with Android-native actions

---

### Phase 4: UI & User Experience (Week 4)
**Goal**: Build user interface for viewing filtered notifications and managing settings

#### Tasks:
1. **Agent Summary Screen**
   - [ ] Expandable view of agent summary notification
   - [ ] Show statistics:
     - Notifications processed today
     - Classification breakdown
     - Context-aware decisions made
   - [ ] Recent actions taken

2. **Notification History Screen**
   - [ ] RecyclerView showing:
     - Urgent notifications (highlighted)
     - Blocked notifications (collapsed, expandable)
     - Stored memories (searchable via AppSearch)
   - [ ] Filter options (by app, by classification, by date)
   - [ ] AppSearch-powered search:
     - Semantic search ("notifications about meetings")
     - Keyword search
     - Vector similarity search

3. **Memory Viewer**
   - [ ] Activity showing stored memories
   - [ ] AppSearch search interface
   - [ ] View original notification context
   - [ ] Delete/consolidate memories
   - [ ] Pattern visualization

4. **Settings & Preferences**
   - [ ] Reasoning mode:
     - On-device only (Gemini Nano)
     - Cloud fallback enabled
     - Cloud only (for testing)
   - [ ] Context providers:
     - Enable/disable Awareness API
     - Enable/disable Health Connect
     - Enable/disable Calendar integration
   - [ ] App-specific rules:
     - Always urgent apps
     - Always blocked apps
   - [ ] Time-based rules (quiet hours)
   - [ ] Location-based rules
   - [ ] Statistics dashboard

5. **Notification Channels**
   - [ ] Create custom notification channels:
     - "Focus Filter - Urgent" (high priority)
     - "Focus Filter - Summary" (daily digest)
   - [ ] Allow user to customize channel settings

**Deliverable**: Complete UI for managing filtered notifications with advanced search

---

### Phase 5: Polish & Optimization (Week 5)
**Goal**: Improve reliability, performance, and user experience

#### Tasks:
1. **Performance Optimization**
   - [ ] Optimize Gemini Nano inference (batch processing)
   - [ ] Cache classification results for similar notifications
   - [ ] Optimize AppSearch queries
   - [ ] Reduce battery impact (efficient background processing)
   - [ ] Lazy loading for notification history

2. **Error Handling & Resilience**
   - [ ] Handle AICore unavailability gracefully
   - [ ] Fallback to backend API automatically
   - [ ] Offline mode (queue notifications, process when online)
   - [ ] Rate limiting protection
   - [ ] User feedback for errors

3. **Security & Privacy**
   - [ ] Secure API key storage (Android Keystore)
   - [ ] Encrypt sensitive data in AppSearch
   - [ ] HTTPS only for API calls
   - [ ] Privacy: on-device processing by default
   - [ ] User control over data sharing

4. **Testing**
   - [ ] Unit tests for notification processing logic
   - [ ] Integration tests for AICore
   - [ ] Integration tests for AppSearch
   - [ ] UI tests for critical flows
   - [ ] Test on multiple Android versions
   - [ ] Test on devices with/without AICore

5. **Documentation**
   - [ ] User guide (how to set up and use)
   - [ ] Developer documentation
   - [ ] API documentation (for backend fallback)
   - [ ] Troubleshooting guide

**Deliverable**: Polished, production-ready prototype

---

## Technical Stack

### Backend (Optional Fallback)
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

### Android (Primary)
- **Language**: Kotlin
- **Min SDK**: 26 (Android 8.0) - Required for NotificationListenerService
- **Target SDK**: 34 (Android 14) - For latest features
- **Architecture**: MVVM with Repository pattern
- **On-Device AI**: 
  - **AICore** (Gemini Nano) - Primary reasoning engine
  - **Gemini 2.0 Flash** (via backend) - Fallback for complex reasoning
- **Context Providers**:
  - **Google Awareness API** - Activity, weather, location
  - **Health Connect** - Well-being context
  - **Calendar Provider** - Meeting/event context
- **Storage**:
  - **AppSearch** - Vector search, full-text search, semantic search
  - **Room** - Simple queries, user preferences (fallback)
- **Notification Management**:
  - **NotificationListenerService** - Intercept notifications
  - **NotificationAssistantService** - Re-rank, smart replies
  - **ZenMode APIs** - DND control
- **Libraries**:
  - Retrofit + OkHttp (API calls - for backend fallback)
  - Coroutines + Flow (async operations)
  - WorkManager (background tasks)
  - Material Design Components (UI)
  - Hilt (Dependency Injection)

---

## Key Enhancements from enhancements.md

### 1. On-Device Intelligence ⭐ NEW
- **Primary**: Gemini Nano via AICore (on-device, private, fast)
- **Fallback**: Backend API with Gemini 2.0 Flash (for complex reasoning)
- **Benefits**: 
  - Privacy (data stays on device)
  - Speed (no network latency)
  - Cost (no API charges)
  - Offline capability

### 2. Enhanced Context ⭐ NEW
- **Google Awareness API**: Activity, weather, location fences
- **Health Connect**: Workout status, sleep windows
- **Calendar Provider**: Meeting context
- **Benefits**: 
  - Context-aware decisions
  - "Don't notify about outdoor gym when it's raining"
  - "User is in a meeting, silence non-urgent"

### 3. Advanced Storage ⭐ NEW
- **AppSearch**: Vector embeddings, semantic search, full-text search
- **Benefits**:
  - "Find notifications about meetings" (semantic)
  - Pattern detection across notifications
  - Better memory retrieval

### 4. Native Android Actions ⭐ NEW
- **NotificationAssistantService**: Re-rank notifications, smart replies
- **ZenMode APIs**: Programmatic DND control
- **App Actions**: Deep-link to other apps
- **Benefits**:
  - Better integration with Android
  - More powerful actions
  - Native user experience

### 5. Shadow Shade Strategy ⭐ NEW
- **Don't replace system UI**: Let notifications come in normally
- **Agent Summary**: Single notification showing agent actions
- **Benefits**:
  - Non-intrusive
  - User stays in control
  - Clear visibility of agent decisions

---

## Backend Changes Needed?

### ✅ Backend is Still Useful (But Optional)

**Current Backend Status**: ✅ Complete and validated

**New Role**: 
- **Primary**: On-device reasoning with Gemini Nano
- **Backend**: Optional fallback for:
  - Complex reasoning tasks
  - Devices without AICore support
  - Testing and development
  - Advanced memory extraction

**Backend Updates Needed**:
1. ⏭️ **Optional**: Add endpoint for context-aware classification
   - Accept context (activity, weather, calendar) in request
   - Use context in classification prompt
   - Not critical - can be added later if needed

2. ⏭️ **Optional**: Add batch processing endpoint
   - Process multiple notifications at once
   - Useful for queued notifications
   - Not critical - can be added later

3. ✅ **No Changes Required**: Current backend works as fallback
   - `/classify` endpoint works
   - `/process` endpoint works
   - Can be enhanced later if needed

**Recommendation**: 
- ✅ **Backend is ready** - No changes needed for MVP
- ⏭️ **Enhancements can be added later** if on-device reasoning needs fallback
- ✅ **Start Android development** - Backend can be enhanced in parallel

---

## Key Challenges & Solutions

### Challenge 1: Notification Access Permission
**Problem**: Android requires special permission to access notifications, and users must manually enable it in system settings.

**Solution**: 
- Create clear onboarding flow with screenshots
- Detect when permission is missing and show dialog
- Deep link to notification access settings

### Challenge 2: AICore Availability
**Problem**: Gemini Nano (AICore) is only available on Pixel 8/9 and S24+ devices.

**Solution**:
- Detect AICore availability at runtime
- Gracefully fallback to backend API
- Show user preference in settings
- Test on both AICore and non-AICore devices

### Challenge 3: Battery Life
**Problem**: Processing every notification could drain battery.

**Solution**:
- On-device processing is more efficient than cloud
- Batch process notifications (every 30 seconds)
- Cache similar notifications
- Use WorkManager with constraints (only on WiFi, charging)
- Optimize Gemini Nano inference

### Challenge 4: AppSearch Setup
**Problem**: AppSearch requires more setup than Room.

**Solution**:
- Start with Room for MVP
- Migrate to AppSearch in Phase 3
- Use Repository pattern to abstract storage
- Keep Room as fallback

### Challenge 5: Context Provider Permissions
**Problem**: Awareness API, Health Connect require additional permissions.

**Solution**:
- Request permissions incrementally
- Explain why each permission is needed
- Allow users to disable context providers
- Gracefully handle missing permissions

---

## Success Criteria

### MVP (Minimum Viable Product)
- ✅ App can intercept Android notifications
- ✅ Notifications are classified on-device (Gemini Nano) or via backend
- ✅ Urgent notifications are re-displayed/promoted
- ✅ Irrelevant notifications are blocked
- ✅ Less urgent notifications are stored locally
- ✅ Basic UI to view notification history
- ✅ Settings to configure reasoning mode
- ✅ Agent summary notification (Shadow Shade)

### Enhanced Features (Post-MVP)
- [ ] Full AppSearch integration with vector search
- [ ] Complete Awareness API integration
- [ ] Health Connect integration
- [ ] NotificationAssistantService with smart replies
- [ ] ZenMode API integration
- [ ] App Actions integration
- [ ] Advanced pattern detection
- [ ] Daily summary of stored memories
- [ ] Statistics and analytics
- [ ] Widget for quick stats

---

## File Structure

```
focus-filter/
├── backend/                    # ✅ COMPLETE
│   ├── .devcontainer/
│   ├── app/
│   ├── focus_filter/
│   ├── tests/
│   └── ...
│
├── android/
│   ├── app/
│   │   ├── src/main/java/com/focusfilter/
│   │   │   ├── MainActivity.kt
│   │   │   ├── service/
│   │   │   │   ├── FocusFilterNotificationService.kt
│   │   │   │   └── FocusFilterNotificationAssistantService.kt  # NEW
│   │   │   ├── reasoning/
│   │   │   │   ├── OnDeviceReasoningEngine.kt      # NEW - Gemini Nano
│   │   │   │   └── ReasoningEngine.kt              # Abstract interface
│   │   │   ├── context/
│   │   │   │   ├── AwarenessContextProvider.kt     # NEW
│   │   │   │   ├── HealthContextProvider.kt         # NEW
│   │   │   │   └── CalendarContextProvider.kt       # NEW
│   │   │   ├── data/
│   │   │   │   ├── api/                            # Backend fallback
│   │   │   │   ├── search/                         # AppSearch
│   │   │   │   │   ├── AppSearchRepository.kt       # NEW
│   │   │   │   │   └── NotificationSearchDocument.kt # NEW
│   │   │   │   ├── database/                        # Room (fallback)
│   │   │   │   └── repository/
│   │   │   ├── ui/
│   │   │   │   ├── notifications/
│   │   │   │   ├── memories/
│   │   │   │   ├── summary/                         # NEW - Agent Summary
│   │   │   │   └── settings/
│   │   │   └── util/
│   │   └── build.gradle.kts
│   └── README.md
│
└── notes/
    ├── ANDROID_PROTOTYPE_PLAN.md (this file)
    └── ...
```

---

## Questions to Consider

1. **Device Support**: 
   - Do you have a Pixel 8/9 or S24+ for AICore testing?
   - Should we support older devices (backend-only mode)?

2. **Privacy vs Features**:
   - On-device only (most private) vs cloud fallback (more features)?
   - User preference or automatic fallback?

3. **Context Providers**:
   - Which context providers are most important?
   - Should all be optional or some required?

4. **Backend Deployment**: 
   - Still deploy backend for fallback?
   - Or focus on on-device only?

---

## Next Steps

1. ✅ **Backend is complete** - Ready to use as fallback
2. **Start Android development** - Phase 2
3. **Set up Android Studio** - Install AICore SDK
4. **Create project structure** - With new architecture
5. **Begin with NotificationListenerService** - Foundation

---

## Estimated Timeline

- **Phase 1 (Backend)**: ✅ 1 week - **COMPLETE**
- **Phase 2 (Android Foundation)**: 1.5 weeks (added on-device reasoning setup)
- **Phase 3 (Agent Integration)**: 1.5 weeks (added NotificationAssistantService)
- **Phase 4 (UI)**: 1 week
- **Phase 5 (Polish)**: 1 week

**Total**: ~6 weeks for enhanced prototype (vs 5 weeks for basic)

---

## Resources

- [Android NotificationListenerService](https://developer.android.com/reference/android/service/notification/NotificationListenerService)
- [Android NotificationAssistantService](https://developer.android.com/reference/android/service/notification/NotificationAssistantService)
- [AICore (Gemini Nano)](https://ai.google.dev/edge/ai-core)
- [Google Awareness API](https://developers.google.com/awareness)
- [Health Connect](https://developer.android.com/guide/health-and-fitness/health-connect)
- [AppSearch](https://developer.android.com/guide/topics/search/app-search)
- [Google ADK Documentation](https://github.com/google/agentic-developer-kit)
- [FastAPI Documentation](https://fastapi.tiangolo.com/)

---

**Ready to start?** The backend is complete and ready to serve as fallback. We can begin Android development with the enhanced architecture! 🚀