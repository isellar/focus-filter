# Focus Filter

An intelligent, agentic notification management system for Android that uses on-device AI to filter, prioritize, and act on notifications based on context.

---

## 🎯 Overview

Focus Filter is an Android app that intelligently manages your notifications using a multi-agent AI system. It observes your context (activity, weather, calendar, health), reasons about notification importance using on-device Gemini Nano, and takes actions (promote urgent, silence irrelevant, store less urgent).

### Key Features

- **On-Device Intelligence**: Uses Gemini Nano (AICore) for private, fast, offline-capable reasoning
- **Context-Aware**: Considers your activity, weather, calendar, and health status
- **Agentic Actions**: Re-ranks notifications, generates smart replies, controls DND
- **Shadow Shade Strategy**: Non-intrusive agent summary notification
- **Semantic Search**: AppSearch-powered memory and pattern detection
- **Privacy-First**: On-device processing by default, cloud fallback optional

---

## 🏗️ Architecture

### Agentic Loop

```
┌─────────────────────────────────────────────────────────────┐
│                    Android Device                            │
│                                                              │
│  INPUT INTERCEPTOR                                           │
│  └─ NotificationListenerService (intercepts all)          │
│                                                              │
│  CONTEXT PROVIDER                                            │
│  └─ Awareness API (activity, weather, location)            │
│  └─ Health Connect (well-being context)                     │
│  └─ Calendar Provider (meetings, events)                   │
│                                                              │
│  REASONING ENGINE                                            │
│  └─ Primary: Gemini Nano (on-device via AICore)            │
│  └─ Fallback: Backend API (cloud Gemini 2.0 Flash)          │
│                                                              │
│  EFFECTOR (Actions)                                          │
│  └─ NotificationAssistantService (re-rank, smart replies)   │
│  └─ ZenMode APIs (DND control)                               │
│  └─ App Actions (deep-link to Calendar, etc.)                │
│                                                              │
│  MEMORY & STORAGE                                            │
│  └─ AppSearch (vector search, semantic search)              │
│  └─ Room (simple queries, fallback)                         │
│                                                              │
│  SHADOW SHADE (UI)                                           │
│  └─ Agent Summary notification (non-intrusive)              │
└──────────────────────────────────────────────────────────────┘
                        │
                        │ (Optional Fallback)
                        ▼
        ┌───────────────────────────────┐
        │   Backend API Service         │
        │   (Python FastAPI)            │
        │   - Cloud Gemini 2.0 Flash    │
        │   - Used when Nano unavailable│
        └───────────────────────────────┘
```

### Components

1. **Input Interceptor**: `NotificationListenerService` intercepts all notifications
2. **Context Provider**: Gathers activity, weather, calendar, health context
3. **Reasoning Engine**: On-device Gemini Nano classifies notifications (URGENT/IRRELEVANT/LESS_URGENT)
4. **Effector**: Takes actions (promote, silence, store, smart replies)
5. **Memory**: AppSearch for semantic search and pattern detection
6. **Shadow Shade**: Agent summary notification showing actions taken

---

## 📁 Project Structure

```
focus-filter/
├── backend/                    # ✅ COMPLETE - Optional fallback API
│   ├── app/                    # FastAPI application
│   ├── focus_filter/           # Agent logic
│   ├── tests/                  # Test suite
│   └── .devcontainer/          # DevContainer setup
│
├── android/                    # 🚧 IN PROGRESS - Android app
│   ├── app/
│   │   └── src/main/java/com/focusfilter/
│   │       ├── service/        # NotificationListenerService
│   │       ├── reasoning/      # On-device reasoning engine
│   │       ├── context/        # Context providers
│   │       ├── data/           # AppSearch, Room, API
│   │       └── ui/             # Activities, Fragments
│   └── build.gradle.kts
│
├── kaggle/                     # Original Kaggle demo
│   └── ...                     # Reference implementation
│
└── notes/                      # Planning and documentation
    ├── PHASE1_PLAN.md          # Backend development plan
    ├── ANDROID_PROTOTYPE_PLAN.md # Android development plan
    └── ...
```

---

## 🚀 Getting Started

### Prerequisites

**For Android Development:**
- Android Studio (latest version)
- Android device with:
  - Android 8.0+ (API 26+) for basic features
  - Pixel 8/9 or S24+ for AICore (Gemini Nano) support
- Google Cloud account (for Awareness API, optional)

**For Backend (Optional):**
- Docker & Docker Compose
- Python 3.11+ (if running locally)
- Google API key (for Gemini 2.0 Flash)

### Quick Start

1. **Clone the repository**
   ```bash
   git clone <repository-url>
   cd focus-filter
   ```

2. **Backend Setup** (Optional - for fallback)
   ```bash
   cd backend
   # Use DevContainer or Docker Compose
   docker-compose up
   # Or see backend/README.md for details
   ```

3. **Android Setup**
   ```bash
   cd android
   # Open in Android Studio
   # See PROTOTYPE_PLAN.md for detailed setup
   ```

---

## 🛠️ Technology Stack

### Android App
- **Language**: Kotlin
- **Min SDK**: 26 (Android 8.0)
- **Target SDK**: 34 (Android 14)
- **Architecture**: MVVM with Repository pattern
- **On-Device AI**: AICore (Gemini Nano)
- **Storage**: AppSearch (vector search), Room (fallback)
- **Context**: Awareness API, Health Connect, Calendar Provider
- **Actions**: NotificationAssistantService, ZenMode APIs, App Actions

### Backend API (Optional Fallback)
- **Language**: Python 3.11+
- **Framework**: FastAPI
- **LLM**: Gemini 2.0 Flash (via Google API)
- **Database**: SQLite (dev) / PostgreSQL (prod)
- **Containerization**: Docker, DevContainers

---

## 📋 Development Status

### ✅ Phase 1: Backend API (Complete)
- [x] DevContainer & Docker setup
- [x] Multi-agent system (Classification, Action, Memory)
- [x] FastAPI endpoints (`/classify`, `/process`)
- [x] Database integration
- [x] Authentication & error handling
- [x] Tests & validation

### 🚧 Phase 2: Android Foundation (In Progress)
- [ ] Android Studio project setup
- [ ] NotificationListenerService
- [ ] AICore integration (Gemini Nano)
- [ ] Context providers (Awareness API, Health Connect)
- [ ] AppSearch setup
- [ ] Basic UI

### 📅 Phase 3: Agent Integration (Planned)
- [ ] Full agentic loop
- [ ] NotificationAssistantService
- [ ] ZenMode APIs
- [ ] App Actions
- [ ] Agent summary notification

### 📅 Phase 4: UI & UX (Planned)
- [ ] Notification history
- [ ] Memory viewer with search
- [ ] Settings & preferences
- [ ] Statistics dashboard

### 📅 Phase 5: Polish (Planned)
- [ ] Performance optimization
- [ ] Error handling
- [ ] Security & privacy
- [ ] Testing & documentation

---

## 🎯 Core Concepts

### Classification Categories
- **URGENT**: Important notifications that need immediate attention
- **IRRELEVANT**: Spam, ads, or unimportant notifications
- **LESS_URGENT**: Notifications that can wait (stored for later)

### Context-Aware Decisions
The agent considers:
- **Activity**: Walking, driving, still
- **Weather**: Rain, snow, etc.
- **Calendar**: Current/upcoming meetings
- **Health**: Workout status, sleep window
- **Location**: Home, work, etc.

### Shadow Shade Strategy
Instead of replacing the system notification UI, Focus Filter:
1. Lets notifications come in normally
2. Processes them in the background
3. Provides a single "Agent Summary" notification showing actions taken

---

## 📚 Documentation

- **[PROTOTYPE_PLAN.md](./PROTOTYPE_PLAN.md)**: Complete development plan with phases and tasks
- **[ARCHITECTURE.md](./ARCHITECTURE.md)**: Detailed architecture documentation
- **[backend/README.md](./backend/README.md)**: Backend setup and API documentation
- **[notes/](./notes/)**: Planning documents and reference materials

---

## 🔒 Privacy & Security

- **On-Device Processing**: Primary reasoning happens on-device (Gemini Nano)
- **No Data Collection**: Notifications processed locally
- **Optional Cloud**: Backend API is optional fallback only
- **Secure Storage**: API keys stored in Android Keystore
- **User Control**: All context providers are optional

---

## 🤝 Contributing

This is a personal project, but contributions and feedback are welcome!

---

## 📄 License

[Add your license here]

---

## 🙏 Acknowledgments

- Google ADK (Agent Developer Kit)
- Gemini Nano (AICore) for on-device AI
- Kaggle Agentic Intensive course for inspiration

---

**Status**: 🚧 Active Development - Backend complete, Android app in progress