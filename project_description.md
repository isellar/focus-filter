# **Focus Filter — Intelligent Notification Filtering Agent**

---

## 🔍 **Problem Statement**

Modern smartphone users are overwhelmed by a constant stream of notifications from apps, services, and systems. These alerts vary drastically in importance—while some require immediate attention, many are low-value distractions or informational noise. In many cases, a single application will have multiple categories of notification with no way to distinguish between them without the user reading and digesting the information themselves.

This overload negatively impacts productivity, focus, and mental clarity.

**The problem:**  
There is no intelligent, personalized system that automatically *interprets*, *filters*, and *acts* on notifications based on a user's priorities.

**Why it matters:**  
- The average person receives 40–100+ notifications per day  
- Most are irrelevant in the moment  
- Even tiny interruptions break workflow and attention cycles  
- Users need an automated agent that works as a cognitive shield, surfacing what matters now, storing what matters later, and blocking what doesn't

Focus Filter addresses this gap by creating an autonomous multi-agent system that routes notifications intelligently and reduces distraction.

---

## 🤖 **Why Agents?**

Agents are uniquely suited to solve this problem because they can:

**1. Perceive**  
They ingest raw notifications, inspect content, and understand the underlying intent using natural-language reasoning.

**2. Decide**  
Agents classify each notification as urgent, irrelevant, or informational through multi-step reasoning, few-shot examples, and user preferences.

**3. Act**  
Unlike a simple classifier, agents can trigger tools, suppress output, store knowledge, summarize long-term patterns, and escalate emergencies.

This ties directly into agentic loops:
- **Get the Mission:** The notification is the mission  
- **Think It Through:** The model reasons about urgency and context  
- **Take Action:** The agent passes, blocks, or stores it  
- **Observe & Learn:** Less urgent notifications enrich long-term memory and improve behavior over time

Agents provide both *autonomy* and *adaptability*, making them an ideal solution for personalized notification management.

---

## 🏗️ **Architecture Overview**

```
Notification Input
    ↓
[Classification Agent]
    → Analyzes with few-shot examples
    → Checks user preferences
    → Classifies: URGENT / IRRELEVANT / LESS_URGENT
    ↓
[Action Agent]
    → Receives classification result
    → Executes: display_urgent / block / save_memory
    ↓
[Memory Agent] (if LESS_URGENT)
    → Extracts key facts
    → Stores with deduplication
    → Consolidates similar memories
    ↓
[Observability & Evaluation]
    → Logs all steps
    → Captures complete traces
    → Updates metrics
    → LLM-as-judge validation
```

### **Core Components**

**1. Multi-Agent System (Sequential Agents)**
- **Classification Agent**: Analyzes notifications and determines urgency category using few-shot examples and user preferences
- **Action Agent**: Executes appropriate actions based on classification (display, block, or save to memory)
- **Memory Agent**: Handles memory extraction, consolidation, and retrieval for less urgent notifications

**2. Custom Tools**
- `display_urgent_notification()`: Shows urgent notifications immediately
- `block_notification()`: Suppresses irrelevant notifications
- `save_notification_memory()`: Stores less urgent notifications as searchable memories
- `retrieve_user_preferences()`: Accesses learned user preferences for better decision-making

**3. Sessions & Memory**
- **Session Management**: Uses `InMemorySessionService` for conversation context and state management
- **Long-Term Memory**: Stores notification facts with deduplication and consolidation
- **User Preferences**: Learns and stores user preferences (always urgent apps, always blocked apps, etc.)
- **Context Compaction**: Efficiently manages context size by selecting most relevant memories
- **Pattern Learning**: Automatically learns user preferences from classification history

**4. Context Engineering**
- **Few-Shot Examples**: Provides 6 example classifications to guide the agent
- **Dynamic Context Assembly**: Builds context based on notification characteristics and user preferences
- **User Preference Injection**: Injects user preferences into agent context automatically
- **Optimized Instructions**: Generates optimized system instructions with few-shot examples embedded
- **Context Compaction**: Selects most relevant memories for efficient context building

**5. Observability**
- **Structured Logging**: Logs all agent actions with timestamps and context
- **Trace Capture**: Captures complete traces of notification processing sessions
- **Metrics Collection**: Tracks classification distribution, action distribution, and performance metrics
- **Error Tracking**: Logs and tracks errors throughout the system
- **Performance Monitoring**: Measures agent response times and system performance

**6. Agent Evaluation**
- **Golden Test Suite**: 8 pre-labeled test cases with expected classifications and actions
- **LLM-as-Judge**: Uses an LLM to evaluate agent performance against expected results
- **Comprehensive Metrics**: Calculates classification accuracy, action correctness, and memory quality
- **Automated Reporting**: Generates detailed evaluation reports with scores and reasoning

---

## 🎥 **How Focus Filter Works**

**Example 1: Urgent Security Alert**

1. **Input:** *"Your bank flagged suspicious activity on your account. Please verify immediately."*

2. **Classification Agent:**
   - Recognizes security alert from banking app
   - Checks user preferences (banking apps may be marked as always urgent)
   - Classifies as **URGENT**

3. **Action Agent:**
   - Calls `display_urgent_notification()`
   - Immediately displays to user with prominent formatting

**Example 2: Irrelevant Social Media Noise**

1. **Input:** *"3 new people liked your photo."*

2. **Classification Agent:**
   - Identifies as social media notification
   - Recognizes low-value content
   - Classifies as **IRRELEVANT**

3. **Action Agent:**
   - Calls `block_notification()`
   - Suppresses without showing to user

**Example 3: Less Urgent Project Update**

1. **Input:** *"Your project deadline has moved to Tuesday."*

2. **Classification Agent:**
   - Identifies as project update
   - Determines important but not immediately urgent
   - Classifies as **LESS_URGENT**
   - Extracts key fact: "Project deadline moved to Tuesday"

3. **Action Agent:**
   - Calls `save_notification_memory()` with extracted fact
   - Stores in memory for later reference

4. **Memory Agent:**
   - Checks for similar memories (deduplication)
   - Consolidates if duplicate found
   - Updates memory store

---

## 🛠️ **Technologies & Key Concepts Demonstrated**

**Technologies:**
- **Google Agent Developer Kit (ADK)** for agent orchestration
- **Gemini 2.0 Flash** for LLM reasoning and classification
- **InMemorySessionService** for session and state management
- **Python** for implementation

**Key Concepts (6 of 6 demonstrated):**

1. **Multi-Agent System**: Sequential coordination of three specialized agents (Classification → Action → Memory)
2. **Custom Tools**: Four custom tools for notification management and preference retrieval
3. **Sessions & Memory**: 
   - Session management with `InMemorySessionService`
   - Long-term memory with consolidation and deduplication
   - Context compaction for efficient retrieval
4. **Context Engineering**: Few-shot examples, dynamic context assembly, user preference injection, optimized instructions
5. **Observability**: Comprehensive logging, tracing, and metrics collection
6. **Agent Evaluation**: LLM-as-judge methodology with golden test suite and automated reporting

---

## 📊 **Evaluation Results**

The system was evaluated using an LLM-as-judge framework with 8 test cases covering all three classification categories:

- **Classification Accuracy**: 100% (8/8 correct)
- **Action Accuracy**: 100% (8/8 correct)
- **Memory Extraction Quality**: 100% average score
- **Overall Performance**: 100% average score

All test cases passed with perfect scores, demonstrating the system's ability to correctly classify and act on notifications across different categories.

---

## ⏳ **Future Enhancements**

**1. Real Device Integration**
- Android notification listeners
- iOS push-forwarding via app extension

**2. Advanced Personalization**
- Automatic preference learning from user feedback
- Time-based filtering rules (e.g., "summaries only after work hours")

**3. Dashboard & Analytics**
- Daily notification summaries
- Memory graph visualization
- Classification breakdown analytics

**4. Vector Database Integration**
- Semantic memory search
- Improved memory retrieval and relevance

**5. Multi-Agent Coordination**
- Parallel processing for multiple notifications
- Specialized agents for different notification types

---

# 🎯 **Focus Filter combines multi-agent reasoning, intelligent memory management, and comprehensive observability to create a smarter, calmer notification experience.**
