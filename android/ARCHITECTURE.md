# Focus Filter - Architecture Documentation

**Last Updated**: 2025-12-28

---

## Table of Contents

1. [System Overview](#system-overview)
2. [Agentic Loop Architecture](#agentic-loop-architecture)
3. [Component Details](#component-details)
4. [Data Flow](#data-flow)
5. [Technology Choices](#technology-choices)
6. [Design Decisions](#design-decisions)

---

## System Overview

Focus Filter is an **agentic notification management system** that uses a multi-agent AI approach to intelligently filter, prioritize, and act on Android notifications.

### Core Principles

1. **On-Device First**: Primary reasoning happens on-device for privacy, speed, and cost
2. **Context-Aware**: Decisions consider user's activity, weather, calendar, and health
3. **Agentic Actions**: System takes actions, not just filters
4. **Non-Intrusive**: Shadow Shade strategy - don't replace system UI
5. **Privacy-Focused**: Data stays on device by default

---

## Agentic Loop Architecture

### High-Level Flow

```
┌─────────────────────────────────────────────────────────────┐
│                    Android Device                            │
│                                                              │
│  1. INPUT INTERCEPTOR                                        │
│     NotificationListenerService                              │
│     ↓                                                        │
│  2. CONTEXT PROVIDER                                         │
│     Awareness API + Health Connect + Calendar                │
│     ↓                                                        │
│  3. REASONING ENGINE                                         │
│     Gemini Nano (on-device) → Backend API (fallback)         │
│     ↓                                                        │
│  4. EFFECTOR                                                 │
│     NotificationAssistantService + ZenMode + App Actions     │
│     ↓                                                        │
│  5. MEMORY & STORAGE                                         │
│     AppSearch (vector search) + Room (fallback)              │
│     ↓                                                        │
│  6. SHADOW SHADE (UI)                                        │
│     Agent Summary Notification                               │
└──────────────────────────────────────────────────────────────┘
```

### Detailed Component Flow

```
Notification Arrives
    ↓
NotificationListenerService.onNotificationPosted()
    ↓
Extract Notification Data (title, body, app, etc.)
    ↓
Gather Context:
    - Awareness API (activity, weather, location)
    - Health Connect (workout, sleep)
    - Calendar Provider (meetings)
    ↓
Build Context-Aware Prompt
    ↓
Reasoning Engine:
    ├─ Try Gemini Nano (on-device)
    │   └─ Success → Classification Result
    └─ Fallback to Backend API
        └─ Classification Result
    ↓
Execute Action Based on Classification:
    ├─ URGENT → Promote via NotificationAssistantService
    ├─ IRRELEVANT → Suppress (already cancelled)
    └─ LESS_URGENT → Store in AppSearch
    ↓
Update Agent Summary Notification
    ↓
Store in Memory (AppSearch)
```

---

## Component Details

### 1. Input Interceptor

**Component**: `NotificationListenerService`

**Responsibilities**:
- Intercept all incoming notifications
- Extract notification data:
  - Title, body, app name, package
  - Icon, timestamp
  - Quick reply actions
- Cancel original notification (to prevent duplicates)

**Implementation**:
```kotlin
class FocusFilterNotificationService : NotificationListenerService() {
    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        // Extract notification data
        // Cancel original
        // Pass to processor
    }
}
```

**Permissions Required**:
- `BIND_NOTIFICATION_LISTENER_SERVICE` (system permission)
- User must manually enable in system settings

---

### 2. Context Provider

**Components**:
- **Awareness API**: Activity, weather, location, headphone state
- **Health Connect**: Workout status, sleep window
- **Calendar Provider**: Current/upcoming meetings

**Responsibilities**:
- Gather real-time context about user's state
- Provide context to reasoning engine
- Update context as user state changes

**Implementation**:
```kotlin
class ContextProvider {
    suspend fun getActivity(): ActivityType
    suspend fun getWeather(): WeatherContext
    suspend fun getLocation(): LocationContext
    suspend fun getHealth(): HealthContext
    suspend fun getCalendar(): CalendarContext
    
    fun buildContextString(): String {
        // Combine all context into prompt string
    }
}
```

**Context Example**:
```
Activity: Walking
Weather: Raining
Location: Home
Health: Just finished workout
Calendar: Meeting in 10 minutes
```

---

### 3. Reasoning Engine

**Primary**: Gemini Nano (AICore) - On-device  
**Fallback**: Backend API (Gemini 2.0 Flash) - Cloud

**Responsibilities**:
- Classify notifications (URGENT/IRRELEVANT/LESS_URGENT)
- Consider context in classification
- Provide confidence score
- Provide reasoning explanation

**Implementation**:
```kotlin
class ReasoningEngine {
    suspend fun classify(
        notification: Notification,
        context: Context
    ): ClassificationResult {
        // Try Gemini Nano first
        return try {
            aicore.classify(buildPrompt(notification, context))
        } catch (e: AICoreUnavailableException) {
            // Fallback to backend API
            backendApi.classify(notification, context)
        }
    }
}
```

**Prompt Template**:
```
Given context:
- Activity: {activity}
- Weather: {weather}
- Calendar: {calendar}
- Health: {health}

Should I alert the user about this notification:
- Title: {title}
- Body: {body}
- App: {app}

Classify as: URGENT, IRRELEVANT, or LESS_URGENT
Provide confidence (0.0-1.0) and reasoning.
```

---

### 4. Effector (Actions)

**Components**:
- **NotificationAssistantService**: Re-rank, smart replies
- **ZenMode APIs**: DND control
- **App Actions**: Deep-linking

**Responsibilities**:
- Execute actions based on classification
- Promote urgent notifications
- Suppress irrelevant notifications
- Store less urgent notifications
- Generate smart replies

**Implementation**:
```kotlin
class ActionExecutor {
    fun executeUrgent(notification: Notification) {
        // Use NotificationAssistantService to promote
        // Generate smart reply via Gemini Nano
        // High priority, sound, vibration
    }
    
    fun executeIrrelevant(notification: Notification) {
        // Suppress via NotificationAssistantService
        // Don't re-display
    }
    
    fun executeLessUrgent(notification: Notification) {
        // Store in AppSearch
        // Extract memory
        // Queue for daily summary
    }
}
```

---

### 5. Memory & Storage

**Primary**: AppSearch (vector search, semantic search)  
**Fallback**: Room (simple queries)

**Responsibilities**:
- Store notification history
- Store extracted memories
- Enable semantic search
- Pattern detection

**Implementation**:
```kotlin
class MemoryRepository {
    suspend fun store(notification: Notification, classification: Classification) {
        // Store in AppSearch with vector embeddings
        // Index for full-text search
    }
    
    suspend fun search(query: String): List<Notification> {
        // Semantic search via AppSearch
        // Vector similarity search
    }
}
```

**AppSearch Schema**:
```kotlin
data class NotificationDocument(
    val id: String,
    val title: String,
    val body: String,
    val appName: String,
    val timestamp: Long,
    val classification: String,
    val embedding: FloatArray, // Vector embedding
    val metadata: Map<String, String>
)
```

---

### 6. Shadow Shade (UI)

**Component**: Agent Summary Notification

**Responsibilities**:
- Show summary of agent actions
- Non-intrusive display
- Expandable for details
- Stay at top of notification shade

**Implementation**:
```kotlin
class AgentSummaryManager {
    fun updateSummary(
        silencedCount: Int,
        highlightedCount: Int,
        storedCount: Int
    ) {
        // Update persistent notification
        // Show counts and recent actions
    }
}
```

**Display Example**:
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

## Data Flow

### Notification Processing Flow

```
1. Notification Arrives
   └─> NotificationListenerService.onNotificationPosted()

2. Extract Data
   └─> Notification(title, body, app, package, timestamp)

3. Gather Context (Parallel)
   ├─> Awareness API → Activity, Weather, Location
   ├─> Health Connect → Workout, Sleep
   └─> Calendar Provider → Meetings, Events

4. Build Prompt
   └─> Combine notification + context → Prompt string

5. Reasoning
   ├─> Try Gemini Nano (on-device)
   │   └─> Success → ClassificationResult
   └─> Fallback to Backend API
       └─> ClassificationResult

6. Execute Action
   ├─> URGENT → Promote + Smart Reply
   ├─> IRRELEVANT → Suppress
   └─> LESS_URGENT → Store + Extract Memory

7. Update UI
   └─> Agent Summary Notification

8. Store in Memory
   └─> AppSearch (with embeddings)
```

### Memory Retrieval Flow

```
1. User Searches
   └─> "Find notifications about meetings"

2. Query Processing
   ├─> Semantic search (vector similarity)
   └─> Full-text search (keywords)

3. AppSearch Query
   └─> Returns matching notifications

4. Display Results
   └─> RecyclerView with search results
```

---

## Technology Choices

### On-Device AI: Gemini Nano (AICore)

**Why**:
- Privacy (data stays on device)
- Speed (no network latency)
- Cost (no API charges)
- Offline capability

**When to Use**:
- Primary reasoning engine
- Classification tasks
- Smart reply generation

**Limitations**:
- Only available on Pixel 8/9, S24+
- Smaller model (less complex reasoning)
- Limited context window

### Fallback: Backend API (Gemini 2.0 Flash)

**Why**:
- More powerful reasoning
- Available on all devices
- Complex memory extraction

**When to Use**:
- AICore unavailable
- Complex reasoning needed
- Testing and development

### Storage: AppSearch

**Why**:
- Vector embeddings for semantic search
- Full-text search
- Pattern detection
- Native Android solution

**When to Use**:
- Notification history
- Memory storage
- Semantic search queries

### Fallback Storage: Room

**Why**:
- Simple queries
- User preferences
- Reliable fallback

**When to Use**:
- Simple data storage
- AppSearch unavailable
- User preferences

---

## Design Decisions

### 1. On-Device First

**Decision**: Primary reasoning on-device, cloud as fallback

**Rationale**:
- Privacy is paramount
- Speed improves UX
- Cost savings
- Works offline

**Trade-offs**:
- Requires AICore-capable device
- Smaller model (less complex reasoning)
- Need fallback strategy

### 2. Shadow Shade Strategy

**Decision**: Don't replace system UI, provide agent summary

**Rationale**:
- Non-intrusive
- User stays in control
- Clear visibility of agent decisions
- Better UX

**Trade-offs**:
- Less control over notification display
- Relies on NotificationAssistantService
- May not work on all Android versions

### 3. Context-Aware Decisions

**Decision**: Consider activity, weather, calendar, health

**Rationale**:
- More intelligent filtering
- Better user experience
- Reduces false positives

**Trade-offs**:
- More permissions needed
- More complex implementation
- Privacy concerns (user can disable)

### 4. AppSearch for Memory

**Decision**: Use AppSearch for vector/semantic search

**Rationale**:
- Better search capabilities
- Pattern detection
- Native Android solution

**Trade-offs**:
- More complex setup
- Requires embeddings
- Room as fallback needed

### 5. Multi-Agent Architecture

**Decision**: Separate agents for classification, action, memory

**Rationale**:
- Clear separation of concerns
- Easier to test and maintain
- Follows agentic principles

**Trade-offs**:
- More components to manage
- Potential performance overhead
- More complex architecture

---

## Security & Privacy

### Data Privacy

- **On-Device Processing**: Primary reasoning on-device
- **No Data Collection**: Notifications processed locally
- **Optional Cloud**: Backend API is optional fallback
- **User Control**: All context providers are optional

### Security

- **Secure Storage**: API keys in Android Keystore
- **Encryption**: Sensitive data encrypted in AppSearch
- **HTTPS Only**: All API calls use HTTPS
- **Permission Model**: Minimal permissions, user control

---

## Performance Considerations

### Optimization Strategies

1. **Batch Processing**: Process multiple notifications together
2. **Caching**: Cache classification results for similar notifications
3. **Lazy Loading**: Load notification history on demand
4. **Background Processing**: Use WorkManager for reliable background tasks
5. **Battery Optimization**: Efficient background processing, constraints

### Bottlenecks

1. **AICore Inference**: May be slow on some devices
2. **AppSearch Queries**: Vector search can be expensive
3. **Context Gathering**: Multiple API calls may be slow
4. **Network Calls**: Backend fallback adds latency

### Mitigation

1. **Async Processing**: Use Coroutines for non-blocking operations
2. **Caching**: Cache context and classification results
3. **Fallback Strategy**: Graceful degradation when services unavailable
4. **User Feedback**: Show loading states, progress indicators

---

## Future Enhancements

### Potential Improvements

1. **Advanced Pattern Detection**: Cross-notification pattern learning
2. **User Preference Learning**: Learn from user actions
3. **Multi-Device Sync**: Sync preferences across devices
4. **Advanced Memory Extraction**: More sophisticated memory consolidation
5. **Custom Actions**: User-defined actions for specific notifications
6. **Widget Support**: Home screen widget for quick stats
7. **Voice Commands**: Voice control for agent actions

---

**Status**: Architecture defined, implementation in progress
