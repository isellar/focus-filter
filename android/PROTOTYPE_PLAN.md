# Focus Filter - Complete Prototype Plan

**Last Updated**: 2025-12-31
**Status**: Backend Complete ✅ | Android In Progress 🚧

---

## 📋 Table of Contents
_(No changes)_

---

## Development Phases

### Phase 2: Android App Foundation (Week 2-3)

**Goal**: Create basic Android app with notification listener and on-device reasoning

#### Tasks

2.  **NotificationListenerService**
    *   [x] Create `FocusFilterNotificationService`
    *   [x] Request notification access permission via UI
    *   [x] Implement `onNotificationPosted()` to extract base data
    *   [x] Cancel original notification (Toggleable via Passthrough Mode)
    *   [ ] Resolve user-facing application name and icon from package name.
    *   [ ] **NEW:** Identify system notifications (e.g., using `Notification.FLAG_ONGOING_EVENT` or checking the package name for `android`) and store this status.

5.  **Storage Setup**
    *   [x] Set up Room as a fallback/initial storage solution
    *   [x] Create `NotificationRepository` pattern
    *   [ ] Add a `userClassification` field to `NotificationEntity` to store manual categorization.
    *   [ ] Add an `isActionable` boolean field to `NotificationEntity`.
    *   [ ] **NEW:** Add an `isSystemNotification` boolean field to `NotificationEntity`.
    *   [ ] Set up AppSearch for primary storage with vector embeddings

---

### Phase 4: UI & User Experience (Week 6)

**Goal**: Build user interface for viewing filtered notifications and managing settings

#### Tasks

1.  **Notification History Screen**
    *   [ ] Enhance the notification list in `MainActivity`.
    *   [ ] Add UI controls for manual categorization (Urgent, Informational, etc.).
    *   [ ] Add a UI control to manually mark a notification as actionable.
    *   [ ] **NEW:** Update the UI for notification history items. If a notification is marked as a system notification, the manual classification and actionability controls should be disabled and visually grayed out.

---

## Implementation Details

### **NEW: System Notification Handling**

To ensure the app is safe and reliable, system-critical notifications will be handled with special care:

*   **Detection:** The `NotificationListenerService` will check for flags like `Notification.FLAG_ONGOING_EVENT` or if the notification originates from a core system package (e.g., `android`, `com.android.systemui`).
*   **Automatic Classification:** Any notification flagged as a system notification will be automatically treated as **Urgent** by the reasoning engine, bypassing any complex AI analysis.
*   **No Suppression:** System notifications will never be suppressed or delayed. In Passthrough Mode they appear normally, and in Filtering Mode they will be immediately re-posted as Urgent.
*   **User Interface:** The manual categorization controls in the UI will be disabled for these items to prevent accidental mislabeling in the dataset.

_(The rest of this document remains largely unchanged)_
