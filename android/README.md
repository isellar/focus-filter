# Focus Filter - Android App

Intelligent, agentic notification management system for Android.

---

## 🚀 Quick Start

### Prerequisites
- Android Studio (latest version)
- Android device with Android 8.0+ (API 26+)
- For AICore (Gemini Nano): Pixel 8/9 or S24+

### Setup

1. **Open in Android Studio**
   ```bash
   # Open Android Studio
   # File → Open → Select android/ directory
   ```

2. **Sync Gradle**
   - Android Studio will automatically sync Gradle
   - Wait for dependencies to download

3. **Build Project**
   - Build → Make Project
   - Fix any dependency issues if they arise

4. **Run on Device**
   - Connect Android device or start emulator
   - Run → Run 'app'
   - Grant notification access when prompted

---

## 📁 Project Structure

```
android/
├── app/
│   ├── src/main/java/com/focusfilter/
│   │   ├── models/              # Data models
│   │   ├── reasoning/           # Reasoning engines
│   │   ├── service/             # Notification services
│   │   ├── context/             # Context providers
│   │   ├── action/              # Action executors
│   │   ├── processor/           # Notification processor
│   │   ├── data/                # Data layer (API, storage)
│   │   ├── ui/                  # UI components
│   │   └── di/                  # Dependency injection
│   └── build.gradle.kts        # App dependencies
├── build.gradle.kts             # Project-level config
└── settings.gradle.kts          # Project settings
```

---

## 🔧 Key Components

### Services
- **FocusFilterNotificationService**: Intercepts all notifications
- **FocusFilterNotificationAssistantService**: Re-ranks notifications, smart replies

### Reasoning
- **OnDeviceReasoningEngine**: Primary - uses Gemini Nano (AICore)
- **BackendReasoningEngine**: Fallback - uses backend API

### Context Providers
- **AwarenessContextProvider**: Activity, weather, location
- **HealthContextProvider**: Workout, sleep (optional)
- **CalendarContextProvider**: Meetings, events

### Actions
- **ActionExecutor**: Executes actions based on classification
- **FocusFilterNotificationManager**: Manages notification display

---

## 📋 Next Steps

1. **Test Build**: Open in Android Studio and build
2. **Fix Dependencies**: Resolve any Gradle issues
3. **Test on Device**: Run and test notification interception
4. **Implement Missing Features**:
   - AppSearch integration
   - Agent summary notification
   - Settings screen
   - Storage implementation

---

## 🐛 Known Issues / TODOs

- AICore initialization needs proper SDK integration
- Awareness API calls need proper implementation
- AppSearch setup not yet implemented
- Room database setup not yet implemented
- Agent summary notification not yet implemented
- Settings screen is placeholder

---

## 📚 Documentation

- See [PROTOTYPE_PLAN.md](../PROTOTYPE_PLAN.md) for full development plan
- See [ARCHITECTURE.md](../ARCHITECTURE.md) for architecture details
- See [QUICK_START.md](../QUICK_START.md) for getting started guide

---

**Status**: 🚧 Foundation code complete, ready for Android Studio build and testing
