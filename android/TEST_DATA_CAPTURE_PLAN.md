# Test Data Capture Plan

**Objective**: Create a process for capturing real-world notifications from a personal device to build a high-quality test dataset for evaluating the reasoning engine.

### Phase 1: Data Export Feature

**Goal**: Add a temporary feature to the debug version of the app to export notifications.

3.  **Implement JSON Export:**
    *   The exporter will create a single JSON file containing an array of all notification objects.
    *   Each JSON object must include the `userClassification` field. This manually assigned category will serve as the "ground truth" for model evaluation.
    *   **NEW:** Each JSON object must also include the `isActionable` boolean flag.
    *   The AI's original classification should be stored as `aiClassification`.
    *   Other fields to include: `title`, `body`, `appName`, `packageName`, `timestamp`, `reasoning`.

_(The rest of this document remains unchanged)_
