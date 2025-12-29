# Focus Filter - Complete Prototype Plan

**Last Updated**: 2025-12-28  
**Status**: Backend Complete ✅ | Android In Progress 🚧

---

## 📋 Table of Contents

1. [Overview](#overview)
2. [Architecture](#architecture)
3. [Development Phases](#development-phases)
4. [Technical Stack](#technical-stack)
5. [Key Features](#key-features)
6. [Implementation Details](#implementation-details)

---

## Overview

### Project Goal

Build an intelligent, agentic notification management system for Android that:
- Uses on-device AI (Gemini Nano) for privacy and speed
- Considers context (activity, weather, calendar, health)
- Takes intelligent actions (promote, silence, store)
- Provides non-intrusive user experience (Shadow Shade)

### Current Status

- ✅ **Backend API**: Complete and validated
- 🚧 **Android App**: Foundation phase
- 📅 **Timeline**: ~6 weeks total (1 week backend ✅, 5 weeks Android)

---

## Architecture

### Agentic Loop

```
┌─────────────────────────────────────────────────────────────┐
│                    Android Device                            │
│                                                              │
│  1. INPUT INTERCEPTOR                                       │
│     NotificationListenerService                              │
│     - Intercepts all notifications                           │
│     - Extracts title, body, app, quick replies              │
│                                                              │
│  2. CONTEXT PROVIDER                                         │
│     - Awareness API: Activity, weather, location            │
│     - Health Connect: Workout, sleep window                 │
│     - Calendar Provider: Meetings, events                   │
│                                                              │
│  3. REASONING ENGINE                                         │
│     Primary: Gemini Nano (AICore) - On-device               │
│     Fallback: Backend API (Gemini 2.0 Flash)                │
│     Prompt: "Given [Context], should I alert about           │
│             [Notification]?"                                 │
│                                                              │
│  4. EFFECTOR (Actions)                                       │
│     - NotificationAssistantService: Re-rank, smart replies  │
│     - ZenMode APIs: DND control                             │
│     - App Actions: Deep-link to Calendar, etc.               │
│                                                              │
│  5. MEMORY & STORAGE                                         │
│     - AppSearch: Vector search, semantic search              │
│     - Room: Simple queries (fallback)                        │
│                                                              │
│  6. SHADOW SHADE (UI)                                        │
│     Agent Summary notification                               │
│     "Silenced 4 shopping alerts, highlighted 1 urgent"       │
└──────────────────────────────────────────────────────────────┘
                        │
                        │ (Optional Fallback)
                        ▼
        ┌───────────────────────────────┐
        │   Backend API Service         │
        │   (Python FastAPI)            │
        │   - Cloud Gemini 2.0 Flash   │
        │   - Used when Nano unavailable│
        └───────────────────────────────┘
```

### Component Details

#### 1. Input Interceptor
- **Technology**: `NotificationListenerService`
- **Function**: Intercepts all incoming notifications
- **Data Extracted**: Title, body, app name, package, icon, quick replies

#### 2. Context Provider
- **Awareness API**: 
  - Activity detection (walking, driving, still)
  - Weather context
  - Location fences
  - Headphone state
- **Health Connect**:
  - Workout detection
  - Sleep window detection
- **Calendar Provider**:
  - Current/upcoming meetings
  - Event context

#### 3. Reasoning Engine
- **Primary**: Gemini Nano via AICore
  - On-device, private, fast
  - Works offline
  - No API costs
- **Fallback**: Backend API
  - For devices without AICore
  - For complex reasoning tasks
  - For testing

#### 4. Effector
- **NotificationAssistantService**:
  - Re-rank notifications
  - Generate smart replies
  - Suggest snooze times
- **ZenMode APIs**:
  - Programmatic DND control
  - Time-based rules
  - Location-based rules
- **App Actions**:
  - Deep-link to Calendar
  - Execute tasks in other apps

#### 5. Memory & Storage
- **AppSearch**:
  - Vector embeddings for semantic search
  - Full-text search
  - Pattern detection
- **Room**:
  - Simple queries
  - User preferences
  - Fallback storage

#### 6. Shadow Shade
- Single persistent notification
- Shows agent actions summary
- Non-intrusive
- Expandable for details

---

## Development Phases

### Phase 1: Backend API Service ✅ COMPLETE

**Goal**: Extract agent logic into deployable API service

**Status**: ✅ Complete and validated

**Deliverables**:
- [x] DevContainer & Docker setup
- [x] Multi-agent system (Classification, Action, Memory, Orchestrator)
- [x] FastAPI endpoints (`/classify`, `/process`)
- [x] Database integration (SQLite/PostgreSQL)
- [x] Authentication & error handling
- [x] Tests & validation

**Note**: Backend now serves as optional fallback for on-device reasoning.

---

### Phase 2: Android App Foundation (Week 2-3)

**Goal**: Create basic Android app with notification listener and on-device reasoning

#### Tasks

1. **Project Setup**
   - [ ] Create Android Studio project (Kotlin)
   - [ ] Configure build.gradle.kts:
     - Min SDK 26, Target SDK 34
     - AICore dependency
     - AppSearch dependency
     - Awareness API dependency
     - Health Connect dependency
     - Retrofit (for backend fallback)
     - Room (fallback storage)
     - Coroutines, Flow, WorkManager
     - Material Design Components
     - Hilt (dependency injection)

2. **NotificationListenerService**
   - [ ] Create `FocusFilterNotificationService`
   - [ ] Request notification access permission
   - [ ] Implement `onNotificationPosted()`
   - [ ] Extract notification data
   - [ ] Cancel original notification

3. **Context Provider Setup**
   - [ ] Awareness API integration:
     - Activity detection
     - Weather context
     - Location fences
     - Headphone state
   - [ ] Health Connect integration (optional):
     - Workout detection
     - Sleep window detection
   - [ ] Calendar Provider integration:
     - Current/upcoming meetings
     - Event context

4. **On-Device Reasoning Engine**
   - [ ] Set up AICore SDK
   - [ ] Create `OnDeviceReasoningEngine` class
   - [ ] Build context-aware prompts
   - [ ] Implement classification (URGENT/IRRELEVANT/LESS_URGENT)
   - [ ] Add confidence scoring
   - [ ] Fallback to backend API

5. **Storage Setup**
   - [ ] Set up AppSearch:
     - Vector embeddings
     - Full-text indexing
     - Notification history
   - [ ] Set up Room (fallback):
     - Simple notification storage
     - User preferences
   - [ ] Create Repository pattern

6. **Basic UI**
   - [ ] MainActivity:
     - Enable notification access button
     - Status indicator
     - Link to settings
     - Agent summary display
   - [ ] SettingsActivity:
     - Enable/disable filtering
     - On-device vs cloud preference
     - Backend API endpoint (fallback)
     - API key input (secure storage)
     - Context provider toggles

**Deliverable**: Android app that intercepts notifications and classifies them on-device

**Estimated Time**: 1.5 weeks

---

### Phase 3: Agent Integration & Advanced Actions (Week 4-5)

**Goal**: Connect full agent pipeline with enhanced Android actions

#### Tasks

1. **Notification Processing Flow**
   - [ ] Create `NotificationProcessor` class
   - [ ] Implement agentic loop:
     - Intercept → Gather context → Reason → Act
   - [ ] Handle classification results
   - [ ] Execute actions based on category

2. **Enhanced Action Implementation**
   - [ ] **URGENT**:
     - Use NotificationAssistantService to promote
     - Generate smart replies via Gemini Nano
     - High priority, sound, vibration
   - [ ] **IRRELEVANT**:
     - Use NotificationAssistantService to suppress
     - Don't re-display
   - [ ] **LESS_URGENT**:
     - Store in AppSearch with embeddings
     - Extract memory (on-device or cloud)
     - Queue for daily summary

3. **NotificationAssistantService**
   - [ ] Extend `NotificationAssistantService`
   - [ ] Implement `onNotificationSnoozedUntilContext()`
   - [ ] Implement `onNotificationsSeen()`
   - [ ] Re-rank notifications
   - [ ] Generate smart replies

4. **Device Controls**
   - [ ] ZenMode APIs:
     - Programmatic DND
     - Time-based rules
     - Location-based rules
   - [ ] App Actions:
     - Deep-link to Calendar
     - Deep-link to other apps

5. **Agent Summary Notification**
   - [ ] Create persistent "Agent Summary" notification
   - [ ] Update with counts and actions
   - [ ] Make expandable
   - [ ] Keep at top of shade

6. **Memory & Search**
   - [ ] Store notifications in AppSearch:
     - Vector embeddings
     - Full-text indexing
     - Metadata
   - [ ] Implement memory search:
     - Semantic search
     - Keyword search
     - Pattern detection

**Deliverable**: App that intelligently filters notifications using on-device AI with Android-native actions

**Estimated Time**: 1.5 weeks

---

### Phase 4: UI & User Experience (Week 6)

**Goal**: Build user interface for viewing filtered notifications and managing settings

#### Tasks

1. **Agent Summary Screen**
   - [ ] Expandable view of agent summary
   - [ ] Statistics:
     - Notifications processed today
     - Classification breakdown
     - Context-aware decisions
   - [ ] Recent actions

2. **Notification History Screen**
   - [ ] RecyclerView:
     - Urgent notifications (highlighted)
     - Blocked notifications (collapsed)
     - Stored memories (searchable)
   - [ ] Filter options (app, classification, date)
   - [ ] AppSearch-powered search:
     - Semantic search
     - Keyword search
     - Vector similarity

3. **Memory Viewer**
   - [ ] Activity showing stored memories
   - [ ] AppSearch search interface
   - [ ] View original notification context
   - [ ] Delete/consolidate memories
   - [ ] Pattern visualization

4. **Settings & Preferences**
   - [ ] Reasoning mode:
     - On-device only
     - Cloud fallback enabled
     - Cloud only
   - [ ] Context providers:
     - Enable/disable Awareness API
     - Enable/disable Health Connect
     - Enable/disable Calendar
   - [ ] App-specific rules
   - [ ] Time-based rules
   - [ ] Location-based rules
   - [ ] Statistics dashboard

5. **Notification Channels**
   - [ ] "Focus Filter - Urgent" channel
   - [ ] "Focus Filter - Summary" channel
   - [ ] Customizable settings

**Deliverable**: Complete UI for managing filtered notifications with advanced search

**Estimated Time**: 1 week

---

### Phase 5: Polish & Optimization (Week 7)

**Goal**: Improve reliability, performance, and user experience

#### Tasks

1. **Performance Optimization**
   - [ ] Optimize Gemini Nano inference
   - [ ] Batch processing
   - [ ] Cache classification results
   - [ ] Optimize AppSearch queries
   - [ ] Reduce battery impact
   - [ ] Lazy loading

2. **Error Handling & Resilience**
   - [ ] Handle AICore unavailability
   - [ ] Automatic fallback to backend
   - [ ] Offline mode
   - [ ] Rate limiting
   - [ ] User feedback for errors

3. **Security & Privacy**
   - [ ] Secure API key storage (Keystore)
   - [ ] Encrypt sensitive data
   - [ ] HTTPS only
   - [ ] Privacy controls
   - [ ] Data sharing preferences

4. **Testing**
   - [ ] Unit tests
   - [ ] Integration tests (AICore, AppSearch)
   - [ ] UI tests
   - [ ] Test on multiple Android versions
   - [ ] Test on devices with/without AICore

5. **Documentation**
   - [ ] User guide
   - [ ] Developer documentation
   - [ ] API documentation
   - [ ] Troubleshooting guide

**Deliverable**: Polished, production-ready prototype

**Estimated Time**: 1 week

---

## Technical Stack

### Android App

**Core**:
- **Language**: Kotlin
- **Min SDK**: 26 (Android 8.0)
- **Target SDK**: 34 (Android 14)
- **Architecture**: MVVM with Repository pattern

**AI & Reasoning**:
- **AICore**: Gemini Nano (on-device)
- **Backend API**: Gemini 2.0 Flash (fallback)

**Context Providers**:
- **Google Awareness API**: Activity, weather, location
- **Health Connect**: Well-being context
- **Calendar Provider**: Meeting/event context

**Storage**:
- **AppSearch**: Vector search, semantic search
- **Room**: Simple queries, user preferences

**Notification Management**:
- **NotificationListenerService**: Intercept notifications
- **NotificationAssistantService**: Re-rank, smart replies
- **ZenMode APIs**: DND control

**Libraries**:
- Retrofit + OkHttp (backend fallback)
- Coroutines + Flow (async)
- WorkManager (background tasks)
- Material Design Components (UI)
- Hilt (dependency injection)

### Backend API (Optional Fallback)

**Core**:
- **Language**: Python 3.11+
- **Framework**: FastAPI
- **LLM**: Gemini 2.0 Flash (via Google API)
- **Database**: SQLite (dev) / PostgreSQL (prod)

**Containerization**:
- DevContainers (development)
- Docker Compose (local)
- Production Dockerfiles (CPU/GPU)

---

## Key Features

### 1. On-Device Intelligence
- Primary reasoning on-device (Gemini Nano)
- Privacy-first (data stays on device)
- Fast (no network latency)
- Works offline
- No API costs

### 2. Context-Aware Decisions
- Considers activity, weather, calendar, health
- "Don't notify about outdoor gym when raining"
- "User is in meeting, silence non-urgent"

### 3. Advanced Storage
- AppSearch for semantic search
- Vector embeddings for similarity
- Pattern detection across notifications

### 4. Native Android Actions
- NotificationAssistantService for re-ranking
- Smart replies generated on-device
- ZenMode APIs for DND control
- App Actions for deep-linking

### 5. Shadow Shade Strategy
- Non-intrusive agent summary
- User stays in control
- Clear visibility of agent decisions

---

## Implementation Details

### Classification Categories

- **URGENT**: Important notifications requiring immediate attention
- **IRRELEVANT**: Spam, ads, unimportant notifications
- **LESS_URGENT**: Notifications that can wait (stored for later)

### Context-Aware Prompt Example

```
Given context:
- Activity: Walking
- Weather: Raining
- Calendar: Meeting in 10 minutes
- Health: Just finished workout

Should I alert the user about this notification:
- Title: "Outdoor gym session reminder"
- Body: "Your outdoor gym session starts in 30 minutes"
- App: FitnessApp

Classification: IRRELEVANT
Reasoning: User is walking in rain, has meeting soon, just finished workout. 
Outdoor gym reminder is not relevant right now.
```

### Agent Summary Notification Example

```
┌─────────────────────────────────────┐
│ Focus Filter Agent Summary          │
├─────────────────────────────────────┤
│ Silenced 4 shopping alerts         │
│ Highlighted 1 urgent message        │
│ Stored 3 less urgent notifications  │
│                                      │
│ [Tap to view details]                │
└─────────────────────────────────────┘
```

---

## Success Criteria

### MVP (Minimum Viable Product)
- ✅ App intercepts Android notifications
- ✅ Notifications classified on-device (Gemini Nano) or via backend
- ✅ Urgent notifications promoted
- ✅ Irrelevant notifications silenced
- ✅ Less urgent notifications stored
- ✅ Basic UI for viewing history
- ✅ Settings for configuration
- ✅ Agent summary notification

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

## Timeline Summary

- **Phase 1 (Backend)**: ✅ 1 week - **COMPLETE**
- **Phase 2 (Foundation)**: 1.5 weeks
- **Phase 3 (Integration)**: 1.5 weeks
- **Phase 4 (UI)**: 1 week
- **Phase 5 (Polish)**: 1 week

**Total**: ~6 weeks (1 week complete, 5 weeks remaining)

---

## Resources

- [Android NotificationListenerService](https://developer.android.com/reference/android/service/notification/NotificationListenerService)
- [Android NotificationAssistantService](https://developer.android.com/reference/android/service/notification/NotificationAssistantService)
- [AICore (Gemini Nano)](https://ai.google.dev/edge/ai-core)
- [Google Awareness API](https://developers.google.com/awareness)
- [Health Connect](https://developer.android.com/guide/health-and-fitness/health-connect)
- [AppSearch](https://developer.android.com/guide/topics/search/app-search)
- [Backend README](./backend/README.md)

---

**Status**: 🚧 Active Development - Ready to begin Phase 2
