# Test Data Capture Plan

**Objective**: Create a process for capturing real-world notifications from a personal device to build a high-quality test dataset for evaluating the reasoning engine.

### Phase 1: Data Export Feature

**Goal**: Add a temporary feature to the debug version of the app to export notifications.

1.  **Add "Export" Button to Settings:**
    *   Create a new button in the `SettingsActivity`.
    *   This button will trigger the export process.

2.  **Create an `NotificationExporter` Class:**
    *   This class will be responsible for querying the Room database for all stored notifications.
    *   It will convert the list of `NotificationEntity` objects into a structured JSON format.

3.  **Implement JSON Export:**
    *   The exporter will create a single JSON file containing an array of all notification objects.
    *   Each JSON object will include all relevant fields: `title`, `body`, `appName`, `packageName`, `timestamp`, `classification`, `reasoning`.

4.  **Save & Share the File:**
    *   Use Android's `FileProvider` and `ACTION_SEND` intent to allow sharing the generated JSON file.
    *   This will let you easily save the file to Google Drive, send it via email, or transfer it to a computer.

### Phase 2: Data Import & Test Harness

**Goal**: Create a mechanism to use the exported data for automated testing.

1.  **Create a Test Asset Folder:**
    *   Place the exported `notifications.json` file in the `app/src/test/assets` directory.

2.  **Build a Test `FakeReasoningEngine`:**
    *   Create a test implementation of the `ReasoningEngine` interface.
    *   This fake engine will not use AI but will instead return pre-defined classifications for known notifications from the test dataset.

3.  **Write Unit/Integration Tests:**
    *   Create tests that load the JSON dataset.
    *   For each notification in the dataset, the test will pass it through the `NotificationProcessor`.
    *   The test will verify that the processor correctly interacts with the system based on the expected outcome.

### Phase 3: (Future) Bulk Classification & Evaluation

**Goal**: Create a script to evaluate the performance of the AI model against the dataset.

1.  **Create a Standalone Evaluation Script:**
    *   This could be a Python or Kotlin script that runs on a development machine.
    *   The script will load the `notifications.json` dataset.

2.  **Iterate and Classify:**
    *   For each notification in the file, the script will send it to the backend API's classification endpoint.

3.  **Compare and Report:**
    *   The script will compare the API's classification result with the original classification (if available).
    *   It will generate a report showing accuracy, misclassifications, and other relevant metrics. This will be key for tuning the model's prompts and behavior.
