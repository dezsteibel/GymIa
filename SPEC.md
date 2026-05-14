# GymAI — Project Specification for Claude Code

> **Instructions for Claude Code:** This file is the single source of truth for this project.
> Read it entirely before writing any code. Follow every decision listed here without suggesting alternatives unless explicitly asked.

---

## Project Summary

Android native app (Kotlin) for gym workout tracking, integrated with the Anthropic Claude API.
The AI generates complete periodized training cycles based on the user's workout history.
This is a **personal-use app** — no authentication, no backend, no multi-user support.

---

## User Profile

- Advanced lifter (3+ years)
- Goal: strength + hypertrophy (mixed)
- Training style: periodized cycles
- No personal trainer — plans own workouts
- Familiar with Strong / Hevy apps

---

## Tech Stack — Follow Exactly

| Layer | Technology |
|---|---|
| Language | Kotlin |
| UI | Jetpack Compose |
| Architecture | MVVM + Repository Pattern |
| Dependency Injection | Hilt |
| Local Database | Room (SQLite) |
| Networking | Retrofit + OkHttp |
| Serialization | Kotlin Serialization (kotlinx.serialization) |
| AI | Anthropic API — model `claude-sonnet-4-20250514` |
| Async | Kotlin Coroutines + StateFlow |
| Min SDK | 26 (Android 8.0) |
| Build | Gradle with Kotlin DSL (build.gradle.kts) |

**Do not use:** ViewBinding, XML layouts, LiveData, RxJava, or any UI framework other than Compose.

---

## Gradle Dependencies

Add these to `app/build.gradle.kts`:

```kotlin
// Compose BOM
implementation(platform("androidx.compose:compose-bom:2024.02.00"))
implementation("androidx.compose.ui:ui")
implementation("androidx.compose.ui:ui-tooling-preview")
implementation("androidx.compose.material3:material3")
implementation("androidx.activity:activity-compose:1.8.2")

// Navigation
implementation("androidx.navigation:navigation-compose:2.7.6")

// ViewModel
implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.7.0")
implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.7.0")

// Hilt
implementation("com.google.dagger:hilt-android:2.50")
kapt("com.google.dagger:hilt-compiler:2.50")
implementation("androidx.hilt:hilt-navigation-compose:1.1.0")

// Room
implementation("androidx.room:room-runtime:2.6.1")
implementation("androidx.room:room-ktx:2.6.1")
kapt("androidx.room:room-compiler:2.6.1")

// Retrofit + OkHttp
implementation("com.squareup.retrofit2:retrofit:2.9.0")
implementation("com.squareup.okhttp3:okhttp:4.12.0")
implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")

// Kotlin Serialization
implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.2")
implementation("com.jakewharton.retrofit:retrofit2-kotlinx-serialization-converter:1.0.0")

// Coroutines
implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")

// Charts (for progress screen)
implementation("com.patrykandpatrick.vico:compose-m3:1.13.1")
```

Add to `app/build.gradle.kts` plugins block:
```kotlin
id("com.google.dagger.hilt.android")
id("androidx.room") // optional, for schema export
kotlin("kapt")
kotlin("plugin.serialization")
```

Add to `local.properties` (never commit this):
```
ANTHROPIC_API_KEY=sk-ant-YOUR_KEY_HERE
```

Read the key in `build.gradle.kts`:
```kotlin
val localProperties = java.util.Properties().apply {
    load(rootProject.file("local.properties").inputStream())
}
android {
    buildFeatures { buildConfig = true }
    defaultConfig {
        buildConfigField("String", "ANTHROPIC_API_KEY", "\"${localProperties["ANTHROPIC_API_KEY"]}\"")
    }
}
```

---

## Project Folder Structure

Create exactly this package structure under `com.gymia`:

```
com.gymia/
├── GymAiApp.kt                  ← @HiltAndroidApp Application class
├── MainActivity.kt              ← Single activity, setContent with NavGraph
│
├── di/
│   ├── AppModule.kt             ← Provides Retrofit, OkHttpClient, AnthropicApi
│   └── DatabaseModule.kt       ← Provides AppDatabase, all DAOs
│
├── data/
│   ├── local/
│   │   ├── AppDatabase.kt      ← RoomDatabase with all entities
│   │   ├── ExerciseDao.kt
│   │   ├── WorkoutDao.kt
│   │   ├── SessionDao.kt
│   │   └── CardioDao.kt
│   ├── model/
│   │   ├── Exercise.kt         ← @Entity
│   │   ├── WorkoutPlan.kt      ← @Entity
│   │   ├── WorkoutDay.kt       ← @Entity (day within a plan)
│   │   ├── WorkoutSession.kt   ← @Entity (a completed training session)
│   │   ├── SetRecord.kt        ← @Entity (individual set within a session)
│   │   └── CardioRecord.kt     ← @Entity
│   ├── remote/
│   │   ├── AnthropicApi.kt     ← Retrofit interface
│   │   ├── dto/
│   │   │   ├── AiRequest.kt    ← Request body for /v1/messages
│   │   │   └── AiResponse.kt   ← Response from Anthropic API
│   └── repository/
│       ├── WorkoutRepository.kt
│       └── AiRepository.kt
│
├── domain/
│   ├── model/
│   │   └── WorkoutCycle.kt     ← Domain model for AI-generated cycle
│   └── usecase/
│       ├── LogSessionUseCase.kt
│       ├── GetProgressUseCase.kt
│       └── GenerateCycleUseCase.kt
│
└── ui/
    ├── theme/
    │   ├── Theme.kt
    │   ├── Color.kt
    │   └── Type.kt
    ├── navigation/
    │   └── NavGraph.kt          ← All routes defined here
    ├── components/              ← Shared Composables (RestTimer, SetRow, etc.)
    ├── workout/
    │   ├── WorkoutScreen.kt
    │   ├── WorkoutViewModel.kt
    │   └── WorkoutUiState.kt
    ├── history/
    │   ├── HistoryScreen.kt
    │   └── HistoryViewModel.kt
    ├── progress/
    │   ├── ProgressScreen.kt
    │   └── ProgressViewModel.kt
    ├── cardio/
    │   ├── CardioScreen.kt
    │   └── CardioViewModel.kt
    └── ai/
        ├── AiCycleScreen.kt
        └── AiCycleViewModel.kt
```

---

## Data Models

### Room Entities

```kotlin
// Exercise.kt
@Entity(tableName = "exercises")
data class Exercise(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val muscleGroup: String,
    val equipmentType: String // "barbell" | "dumbbell" | "machine" | "bodyweight" | "cable"
)

// WorkoutPlan.kt
@Entity(tableName = "workout_plans")
data class WorkoutPlan(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val createdAt: Long = System.currentTimeMillis(),
    val source: String = "manual" // "manual" | "ai_generated"
)

// WorkoutDay.kt
@Entity(tableName = "workout_days")
data class WorkoutDay(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val planId: Long,
    val label: String,          // e.g. "A — Push", "B — Pull"
    val order: Int
)

// WorkoutSession.kt
@Entity(tableName = "workout_sessions")
data class WorkoutSession(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val planId: Long? = null,
    val dayId: Long? = null,
    val date: Long = System.currentTimeMillis(),
    val durationMinutes: Int = 0,
    val notes: String? = null
)

// SetRecord.kt
@Entity(tableName = "set_records")
data class SetRecord(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val sessionId: Long,
    val exerciseId: Long,
    val setNumber: Int,
    val reps: Int,
    val loadKg: Float,
    val completed: Boolean = true
)

// CardioRecord.kt
@Entity(tableName = "cardio_records")
data class CardioRecord(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val date: Long = System.currentTimeMillis(),
    val activityType: String,   // "run" | "bike" | "row" | "other"
    val durationMinutes: Int,
    val distanceKm: Float? = null,
    val notes: String? = null
)
```

---

## Anthropic API Integration

### Endpoint

```
POST https://api.anthropic.com/v1/messages
```

### Required Headers

```
x-api-key: BuildConfig.ANTHROPIC_API_KEY
anthropic-version: 2023-06-01
content-type: application/json
```

### Request DTO

```kotlin
@Serializable
data class AiRequest(
    val model: String = "claude-sonnet-4-20250514",
    val max_tokens: Int = 4096,
    val system: String,
    val messages: List<AiMessage>
)

@Serializable
data class AiMessage(
    val role: String, // "user"
    val content: String
)
```

### Response DTO

```kotlin
@Serializable
data class AiResponse(
    val content: List<AiContent>
)

@Serializable
data class AiContent(
    val type: String,
    val text: String
)
```

### System Prompt for Cycle Generation

Use this exact system prompt in `AiRepository`:

```
You are an expert strength and conditioning coach specializing in periodized training for advanced lifters.
The user trains for strength and hypertrophy (mixed). They are advanced (3+ years).
They plan their own workouts without a personal trainer.

When asked to generate a training cycle, analyze the provided workout history and respond ONLY with a valid JSON object.
Do not include markdown, code blocks, or any explanation outside the JSON.

The JSON must follow this exact structure:
{
  "cycle_name": "string",
  "duration_weeks": number,
  "general_notes": "string",
  "days": [
    {
      "day_label": "string",
      "exercises": [
        {
          "name": "string",
          "sets": number,
          "reps_target": "string",
          "load_suggestion_kg": number,
          "progression_note": "string"
        }
      ]
    }
  ]
}
```

### User Message Template

```kotlin
fun buildUserMessage(history: List<WorkoutSession>): String {
    return """
        Here is my recent workout history:
        ${Json.encodeToString(history)}
        
        Please generate my next periodized training cycle based on this data.
        Consider my performance trends, stagnation points, and progression opportunities.
        Respond only with the JSON object as specified.
    """.trimIndent()
}
```

---

## Navigation Routes

```kotlin
sealed class Screen(val route: String) {
    object Workout  : Screen("workout")
    object History  : Screen("history")
    object Progress : Screen("progress")
    object Cardio   : Screen("cardio")
    object AiCycle  : Screen("ai_cycle")
}
```

Bottom navigation with 5 tabs: Workout, History, Progress, Cardio, AI.

---

## UI Guidelines

- Use **Material 3** components only
- **Dark theme** supported (use `dynamicColorScheme` if API >= 31, fallback to custom dark theme)
- Interface must be **fast** — minimum taps to log a set
- No decorative elements — functional UI only
- The active workout screen is the most important screen: optimize for speed of data entry
- Use `LazyColumn` for all lists
- All ViewModels expose `StateFlow<UiState>` — no LiveData
- Handle loading / error / success states in every UiState

---

## Development Phases

Build in this exact order. Do not skip phases.

### Phase 1 — Foundation (build this first)
1. Project setup: Hilt, Room, Compose, Retrofit all configured and compiling
2. All Room entities and DAOs
3. AppDatabase with all tables
4. All DI modules (AppModule, DatabaseModule)
5. Basic NavGraph with bottom bar and placeholder screens

### Phase 2 — Workout Recording
1. Exercise CRUD (create, list, search)
2. WorkoutPlan creation with days and exercises
3. WorkoutSession execution screen (the main screen):
   - List exercises for the day
   - Log sets inline (reps + load input, confirm button)
   - Optional rest timer
   - Save and close session
4. History screen: list of past sessions with summary

### Phase 3 — Progress & Cardio
1. Progress screen: chart of max load per exercise over time
2. Weekly volume chart
3. PR detection per exercise
4. Cardio recording screen and history

### Phase 4 — AI Integration
1. AiRepository: serialize history, call Anthropic API, parse JSON response
2. GenerateCycleUseCase: orchestrate the full flow
3. AiCycleScreen: show generated cycle, allow accept/edit/reject
4. Save accepted cycle as a new WorkoutPlan (source = "ai_generated")

---

## Key Rules — Always Follow

1. **Never access the Anthropic API during a workout session** — only on explicit user request in the AI tab
2. **Offline-first** — every feature works without internet except AI cycle generation
3. **All network calls in repositories** — ViewModels never call Retrofit directly
4. **All DB calls via Use Cases** — ViewModels call Use Cases, not repositories directly
5. **Every ViewModel function must handle exceptions** — wrap in try/catch, expose error state
6. **The API key lives only in `local.properties` and `BuildConfig`** — never hardcode it anywhere
7. **Use `kotlinx.serialization`** throughout — not Gson, not Moshi

---

## .gitignore additions required

Make sure these are in `.gitignore`:
```
local.properties
*.jks
```

---

*This spec is complete. Start with Phase 1.*
