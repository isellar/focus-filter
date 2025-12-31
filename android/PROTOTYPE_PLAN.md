# Focus Filter - Complete Prototype Plan

**Last Updated**: 2025-12-30
**Status**: Backend Complete ✅ | Android In Progress 🚧

---

## 📋 Table of Contents
_(No changes)_

---

## Development Phases

### Phase 2: Android App Foundation (Week 2-3)

**Goal**: Create basic Android app with notification listener and on-device reasoning

#### Tasks

5. **Storage Setup**
   - [x] Set up Room as a fallback/initial storage solution
   - [x] Create `NotificationRepository` pattern
   - [ ] **NEW:** Add a `userClassification` field to `NotificationEntity` to store manual categorization.
   - [ ] **NEW:** Add an `isActionable` boolean field to `NotificationEntity` to track if a notification contains a potential action.
   - [ ] Set up AppSearch for primary storage with vector embeddings

6. **Basic UI**
   - [x] Create `MainActivity` and `SettingsActivity`
   - [x] Implement UI for notification access and permission status
   - [x] Display list of stored notifications from Room
   - [x] Add "Passthrough Mode" toggle and "Export Data" feature
   - [x] Add Dark/Light/System theme selection
   - [ ] **NEW:** Update notification history items to display the resolved app name (and eventually icon).

---

### Phase 4: UI & User Experience (Week 6)

**Goal**: Build user interface for viewing filtered notifications and managing settings

#### Tasks

1. **Notification History Screen**
   - [ ] Enhance the notification list in `MainActivity`.
   - [ ] **NEW:** Add UI controls (e.g., buttons, chips) to each notification item to allow manual categorization into **Urgent, Informational, Background, or Irrelevant**.
   - [ ] **NEW:** Add a UI control (e.g., checkbox, switch) to each notification item to manually mark it as **actionable**.
   - [ ] The selected category and actionable status should be saved back to the database.

---

## Implementation Details

### Classification Categories

The classification system uses a four-tier model:

- **Urgent**: An immediate and important notification. The agent should surface this to the user right away.
- **Informational**: A notification that is useful but not time-sensitive. The agent should surface this at the next opportune moment.
- **Background**: A notification that should be recorded for long-term memory or pattern detection but does not need to be surfaced to the user directly.
- **Irrelevant**: A notification that can be safely discarded and does not need to be stored.

### **NEW: Actionability**

- A notification is considered **actionable** if it contains information that could be used to perform a task (e.g., creating a calendar event, setting a reminder, navigating to a location).
- This will be tracked as a boolean flag in the database and will be a key feature for the agent's future decision-making.

_(The rest of this document remains largely unchanged)_
