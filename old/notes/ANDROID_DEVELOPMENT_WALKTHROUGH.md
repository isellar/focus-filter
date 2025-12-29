# Android App Development - Process Walkthrough

## Overview

We're building an Android app that intercepts notifications, sends them to your backend API for classification, and then acts on them (display urgent, block irrelevant, store less urgent).

---

## High-Level Flow

```
1. User installs app
   ↓
2. User grants notification access permission (manual system setting)
   ↓
3. App intercepts all notifications
   ↓
4. For each notification:
   - Cancel original notification
   - Send to backend API (/api/v1/process)
   - Receive classification (URGENT/IRRELEVANT/LESS_URGENT)
   - Take action:
     * URGENT → Re-display with high priority
     * IRRELEVANT → Don't display (already cancelled)
     * LESS_URGENT → Store in local database
   ↓
5. User can view history, memories, and settings in the app
```

---

## Key Decisions Needed

### 1. Development Environment

**Options:**
- **A) Android Studio** (Standard approach)
  - Full IDE with emulator
  - Built-in tools and debugging
  - Standard Android development workflow
  
- **B) DevContainer** (Like backend)
  - Consistent environment
  - But Android development typically needs Android Studio
  - Could use DevContainer for CI/CD, but development in Android Studio

**Recommendation**: Use **Android Studio** for development (standard), but we can set up DevContainer for CI/CD if needed.

---

### 2. Backend Connection

**Current State**: Backend API is ready at `http://localhost:8000` (or your deployed URL)

**Questions:**
- **Where is backend deployed?**
  - Local development: `http://10.0.2.2:8000` (Android emulator → host)
  - Physical device: `http://YOUR_IP:8000` or deployed URL
  - Production: Your deployed backend URL

- **API Authentication**: Backend uses `X-API-Key` header
  - Store API key securely in Android Keystore
  - User enters it in settings

---

### 3. Architecture Pattern

**Recommended: MVVM (Model-View-ViewModel)**
```
UI (Activities/Fragments)
    ↓
ViewModel (UI logic, state)
    ↓
Repository (Data source abstraction)
    ↓
Data Sources:
  - Remote (Retrofit API)
  - Local (Room Database)
```

**Why MVVM?**
- Separation of concerns
- Testable
- Works well with Android lifecycle
- Standard Android pattern

---

### 4. Notification Handling Strategy

**Challenge**: Android doesn't let apps cancel other apps' notifications directly.

**Solution**:
1. **Intercept** notification in `NotificationListenerService`
2. **Cancel** it immediately (prevents duplicate)
3. **Process** through backend API
4. **Re-display** only if URGENT

**Flow**:
```
Notification arrives
  → NotificationListenerService.onNotificationPosted()
  → Cancel notification (cancelNotification())
  → Send to backend API
  → If URGENT: Create new notification via NotificationManager
  → If IRRELEVANT: Do nothing (already cancelled)
  → If LESS_URGENT: Store in database
```

---

### 5. Background Processing

**Options:**
- **A) Foreground Service** (NotificationListenerService)
  - Required for notification access
  - Always running
  - Shows persistent notification
  
- **B) WorkManager** (For API calls)
  - Reliable background processing
  - Handles retries
  - Respects battery optimization

**Recommendation**: Use both
- Foreground Service for notification interception
- WorkManager for API calls (with constraints: WiFi, charging)

---

## Development Phases

### Phase 2: Foundation (Week 2)
**Goal**: Basic app that intercepts notifications and calls API

**Steps:**
1. Create Android Studio project
2. Set up dependencies (Retrofit, Room, Coroutines, WorkManager)
3. Create NotificationListenerService
4. Request notification access permission
5. Basic UI (MainActivity with status)
6. API client setup
7. Test with real notifications

**Deliverable**: App intercepts notifications and sends to backend

---

### Phase 3: Agent Integration (Week 3)
**Goal**: Process notifications through full pipeline

**Steps:**
1. NotificationProcessor class
2. Call backend `/process` endpoint
3. Handle responses (URGENT/IRRELEVANT/LESS_URGENT)
4. Re-display urgent notifications
5. Store less urgent in Room database
6. WorkManager for reliable processing

**Deliverable**: App filters notifications using backend agents

---

### Phase 4: UI & UX (Week 4)
**Goal**: Complete user interface

**Steps:**
1. Notification history screen
2. Memory viewer
3. Settings screen
4. Statistics dashboard
5. Material Design UI

**Deliverable**: Complete UI for managing filtered notifications

---

### Phase 5: Polish (Week 5)
**Goal**: Production-ready

**Steps:**
1. Performance optimization
2. Error handling
3. Security (Keystore for API keys)
4. Testing
5. Documentation

**Deliverable**: Polished prototype

---

## Technical Stack

### Android App
- **Language**: Kotlin
- **Min SDK**: 26 (Android 8.0) - Required for NotificationListenerService
- **Target SDK**: 34 (Latest)
- **Architecture**: MVVM
- **Dependencies**:
  - Retrofit + OkHttp (API calls)
  - Room (local database)
  - Coroutines + Flow (async)
  - WorkManager (background tasks)
  - Material Design Components (UI)
  - Hilt (Dependency Injection - optional but recommended)

---

## Key Challenges & Solutions

### Challenge 1: Notification Access Permission
**Problem**: User must manually enable in system settings (can't be done programmatically)

**Solution**:
- Clear onboarding screen with instructions
- Deep link to notification access settings
- Check permission status and show prompt
- Screenshots/animations showing how to enable

### Challenge 2: Battery Life
**Problem**: API calls for every notification could drain battery

**Solution**:
- Batch notifications (process every 30 seconds)
- Cache similar notifications
- WorkManager constraints (only on WiFi, charging)
- Local rule-based filtering for obvious cases

### Challenge 3: Network Reliability
**Problem**: API might be unavailable

**Solution**:
- Queue notifications when offline
- Retry with exponential backoff
- Show user-friendly error messages
- Option for offline mode (local rules only)

### Challenge 4: Notification Cancellation
**Problem**: Can't cancel other apps' notifications directly

**Solution**:
- Cancel immediately in NotificationListenerService
- Re-display only if URGENT
- For IRRELEVANT, don't re-display

---

## Development Workflow

### Setup
1. **Android Studio**: Install latest version
2. **Create Project**: New Android project (Kotlin, MVVM template)
3. **Configure**: Min SDK 26, add dependencies
4. **Connect Backend**: 
   - Local: Use `http://10.0.2.2:8000` for emulator
   - Physical device: Use your computer's IP or deployed URL

### Development Cycle
1. **Code** in Android Studio
2. **Run** on emulator or physical device
3. **Test** with real notifications
4. **Debug** using Android Studio tools
5. **Iterate** based on results

### Testing Strategy
- **Unit Tests**: Business logic, ViewModels
- **Integration Tests**: API calls, database operations
- **UI Tests**: Critical user flows
- **Manual Testing**: Real notifications on physical device

---

## Questions Before We Start

### 1. Backend Deployment
- **Where is your backend running?**
  - Local development server?
  - Deployed (Railway, Render, Cloud Run)?
  - Need to deploy it first?

### 2. Development Device
- **Physical Android device** or **emulator**?
- Physical device is better for testing notifications
- Emulator works but requires more setup

### 3. Android Studio
- **Do you have Android Studio installed?**
- If not, we'll need to install it first

### 4. API Endpoint
- **What URL will the app use?**
  - Local: `http://10.0.2.2:8000` (emulator)
  - Physical device: `http://YOUR_IP:8000`
  - Deployed: Your production URL

### 5. Architecture Preferences
- **MVVM** (recommended) or prefer different pattern?
- **Hilt** for dependency injection (recommended) or manual DI?

### 6. UI Framework
- **Jetpack Compose** (modern, recommended) or **XML Views** (traditional)?
- Compose is easier and more modern
- XML is more traditional

---

## Recommended Approach

### For MVP (Fastest Path)
1. **XML Views** (faster to get started)
2. **MVVM** with manual dependency injection (simpler)
3. **Physical device** for testing (more reliable for notifications)
4. **Local backend** initially (easier debugging)

### For Modern/Production
1. **Jetpack Compose** (better long-term)
2. **MVVM** with Hilt (cleaner architecture)
3. **Both emulator and device** testing
4. **Deployed backend** (production-like)

---

## Next Steps

1. **Answer the questions above** so I can tailor the approach
2. **Set up Android Studio** if needed
3. **Create project structure** with proper architecture
4. **Start with Phase 2** - Foundation

---

## What I'll Create

Once you answer the questions, I'll create:
1. **Detailed Phase 2 plan** (like Phase 1 plan)
2. **Project structure** with all files
3. **Step-by-step issues** for incremental development
4. **Code templates** for key components
5. **Testing strategy** for each component

---

**Ready?** Let me know your preferences and we'll get started! 🚀
