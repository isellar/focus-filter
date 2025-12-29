# Quick Start Guide

**Focus Filter** - Get started with Android development

---

## Prerequisites

### Required
- **Android Studio** (latest version)
  - Download from [developer.android.com/studio](https://developer.android.com/studio)
- **Android Device** (for testing)
  - Physical device recommended (better for notifications)
  - Android 8.0+ (API 26+) for basic features
  - **Pixel 8/9 or S24+** for AICore (Gemini Nano) support

### Optional (for full features)
- **Google Cloud Account** (for Awareness API)
- **Backend API** (for fallback reasoning)
  - See [backend/README.md](./backend/README.md) for setup

---

## Android Development Setup

### 1. Clone Repository

```bash
git clone <repository-url>
cd focus-filter
```

### 2. Open in Android Studio

```bash
cd android
# Open Android Studio
# File → Open → Select android/ directory
```

### 3. Configure Project

**Min SDK**: 26 (Android 8.0)  
**Target SDK**: 34 (Android 14)  
**Language**: Kotlin

### 4. Add Dependencies

The project will need these dependencies (see `PROTOTYPE_PLAN.md` for full list):

**Core**:
- AICore (Gemini Nano)
- AppSearch
- Awareness API
- Health Connect (optional)
- Retrofit (for backend fallback)
- Room (fallback storage)
- Coroutines, Flow, WorkManager
- Material Design Components
- Hilt (dependency injection)

### 5. Set Up AICore

1. **Check Device Support**:
   - AICore is available on Pixel 8/9 and S24+
   - App will fallback to backend API on other devices

2. **Add AICore Dependency**:
   ```kotlin
   // In build.gradle.kts
   dependencies {
       implementation("com.google.ai.client.generativeai:generativeai:0.2.2")
   }
   ```

3. **Check Availability**:
   ```kotlin
   val aicoreAvailable = AICore.isAvailable(context)
   ```

### 6. Configure Permissions

**AndroidManifest.xml**:
```xml
<!-- Notification access (system permission) -->
<service
    android:name=".service.FocusFilterNotificationService"
    android:permission="android.permission.BIND_NOTIFICATION_LISTENER_SERVICE">
    <intent-filter>
        <action android:name="android.service.notification.NotificationListenerService" />
    </intent-filter>
</service>

<!-- Awareness API -->
<uses-permission android:name="com.google.android.gms.permission.ACTIVITY_RECOGNITION" />

<!-- Health Connect (optional) -->
<uses-permission android:name="android.permission.health.READ_STEPS" />
```

### 7. Enable Notification Access

**Important**: Users must manually enable notification access in system settings.

**In your app**:
1. Check if access is granted
2. Show dialog with instructions
3. Deep link to notification access settings:
   ```kotlin
   val intent = Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS)
   startActivity(intent)
   ```

---

## Backend Setup (Optional)

The backend serves as an optional fallback for devices without AICore or for complex reasoning.

### Quick Setup

```bash
cd backend
docker-compose up
```

**See [backend/README.md](./backend/README.md) for detailed setup.**

### Configure in Android App

**Settings**:
- Backend API endpoint: `http://YOUR_IP:8000` (local) or deployed URL
- API key: Store securely in Android Keystore

---

## Development Workflow

### 1. Start Development

1. Open project in Android Studio
2. Connect Android device or start emulator
3. Run app
4. Grant notification access (manually in system settings)

### 2. Test Notification Interception

1. Send test notification to device
2. Check logs for interception
3. Verify classification works
4. Check agent summary notification

### 3. Iterate

- Make changes
- Run tests
- Debug using Android Studio tools
- Test on physical device (recommended)

---

## Testing Strategy

### Unit Tests
- Business logic
- ViewModels
- Repository pattern

### Integration Tests
- AICore integration
- AppSearch queries
- API calls (backend fallback)

### UI Tests
- Critical user flows
- Notification access setup
- Settings configuration

### Manual Testing
- **Physical device recommended** for notification testing
- Test with real notifications
- Verify agent summary updates
- Test context providers

---

## Key Files to Know

### Project Structure
```
android/
├── app/
│   ├── src/main/java/com/focusfilter/
│   │   ├── service/
│   │   │   └── FocusFilterNotificationService.kt
│   │   ├── reasoning/
│   │   │   └── OnDeviceReasoningEngine.kt
│   │   ├── context/
│   │   │   └── ContextProvider.kt
│   │   ├── data/
│   │   │   └── repository/
│   │   └── ui/
│   │       └── MainActivity.kt
│   └── build.gradle.kts
```

### Important Files
- **MainActivity.kt**: Main UI, status display
- **FocusFilterNotificationService.kt**: Notification interception
- **OnDeviceReasoningEngine.kt**: Gemini Nano integration
- **ContextProvider.kt**: Awareness API, Health Connect, Calendar
- **build.gradle.kts**: Dependencies and configuration

---

## Common Issues

### AICore Not Available
- **Solution**: App automatically falls back to backend API
- **Check**: Device compatibility (Pixel 8/9, S24+)
- **Workaround**: Use backend API for all devices

### Notification Access Not Working
- **Solution**: User must manually enable in system settings
- **Check**: Deep link to settings works
- **Verify**: Check `NotificationListenerService` is bound

### Context Providers Not Working
- **Solution**: Check permissions are granted
- **Check**: Awareness API key is configured
- **Verify**: Health Connect is installed (if using)

### Backend API Connection Failed
- **Solution**: Check backend is running
- **Check**: Network connectivity
- **Verify**: API endpoint and key are correct

---

## Next Steps

1. **Read [PROTOTYPE_PLAN.md](./PROTOTYPE_PLAN.md)** for detailed development plan
2. **Read [ARCHITECTURE.md](./ARCHITECTURE.md)** for architecture details
3. **Start Phase 2**: Android Foundation
4. **Follow the plan**: Step-by-step implementation

---

## Resources

- [Android NotificationListenerService](https://developer.android.com/reference/android/service/notification/NotificationListenerService)
- [AICore Documentation](https://ai.google.dev/edge/ai-core)
- [Google Awareness API](https://developers.google.com/awareness)
- [AppSearch Documentation](https://developer.android.com/guide/topics/search/app-search)
- [Backend README](./backend/README.md)

---

**Ready to start?** Open the project in Android Studio and begin Phase 2! 🚀