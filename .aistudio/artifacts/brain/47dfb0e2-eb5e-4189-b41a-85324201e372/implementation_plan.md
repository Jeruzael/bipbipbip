# WakeQuest – Challenge-Based Alarm Clock

WakeQuest transforms morning wake-ups by replacing passive snooze buttons with active cognitive challenges (finger drawing path-tracing and dynamic fruit catching), combined with a mandatory 4-sequence alarm cadence (+0m, +5m, +10m, +15m) that ensures users never slip back into deep sleep.

---

## User Review & Critical Decisions

> [!IMPORTANT]
> The following product decisions were confirmed during clarification:
> - **Visual Style & Theme**: Modern Energetic Sunrise with a rich dark night background (`#111218`), vibrant sunrise orange accents (`#FF6F00`, `#FF8A00`), solar amber accents (`#FFB300`), and clean high-contrast text.
> - **Alarm Audio & Haptics**: Continuous high-priority alarm audio streaming paired with synchronized haptic pulse vibration that persists throughout the challenge until the user successfully completes the task.
> - **Practice Mode**: An interactive "Test & Practice Challenge" mode accessible directly from the home screen and alarm editor, allowing users to practice the drawing templates and fruit game anytime without waiting for an alarm.
> - **Sequence Invariance**: As specified in the PRD, every created alarm strictly schedules four occurrences at 5-minute intervals (+0m, +5m, +10m, +15m) without the option to disable the sequence. Dismissing one occurrence preserves all subsequent scheduled reminders.

---

## 1. Overview & Core Concept

- **What It Does**: WakeQuest is an offline, privacy-first Android alarm clock built with Kotlin and Jetpack Compose. When an alarm triggers, it presents either an interactive **Drawing Challenge** (tracing Star, House, or Dinosaur within an 85% path tolerance) or a fast-paced **Fruit Catching Game** (tapping 10 target fruits falling from the top among distractors). The alarm only silences upon verifiable completion.
- **Target Audience**: Heavy sleepers, chronic snoozers, students, and professionals who struggle with unconscious alarm dismissal.
- **Key Value**: Guarantees physical and mental alertness before the alarm can be turned off, backed by an unyielding 4-stage sequential reminder safeguard.

---

## 2. User Experience & Visual Design

### 2.1 Key User Flows

1. **Dashboard & Sequence Overview (Home Screen)**:
   - Displays real-time digital clock header with energetic sunrise greeting.
   - List of active and inactive alarms. Each alarm card displays the primary time (e.g., `06:30 AM`), the automatic 4-sequence timeline pills (`6:30 → 6:35 → 6:40 → 6:45`), active repeat days, challenge badge (e.g., 🎨 *Dinosaur Trace* or 🍇 *Catch Grapes*), and an enable toggle.
   - Quick action to "Practice Challenges" or tap "+" to create an alarm.
   - Alarm cards can be expanded to view or edit, or swiped/tapped to delete.

2. **Add / Edit Alarm Screen**:
   - Time picker (12-hour AM/PM dial and spinners).
   - Live **Sequential Preview**: Immediately displays the 4 calculated alarm times (+0m, +5m, +10m, +15m) with a clear explanation that sequential alarms are mandatory and automatic.
   - Repeat days selector (Sun through Sat chips).
   - Challenge Picker:
     - **Drawing Challenge**: Choose template (*Star [Easy]*, *House [Medium]*, *Dinosaur [Hard]*).
     - **Fruit Catching Game**: Choose target fruit (*Apple*, *Banana*, *Grapes*, *Orange*, *Strawberry*, *Watermelon*).
   - "Test Challenge Now" preview button to verify the challenge before saving.
   - Save button commits to Room DB and registers `AlarmManager` exact alarms.

3. **Active Alarm Ringing & Challenge Experience**:
   - Full-screen wake lock with continuous audio playback (using alarm audio stream) and rhythmic vibration.
   - **Drawing Challenge Canvas**:
     - Displays the vector outline of the selected template as a faint guide.
     - Smooth finger stroke rendering with glowing neon sunrise trails.
     - Real-time progress bar computing path coverage percentage (must reach ≥ 85%).
     - "Clear / Restart" button if the user gets stuck.
     - On reaching ≥ 85%, instant audio silence, celebration burst, and dismiss transition.
   - **Fruit Catching Game Arena**:
     - Shows the target fruit badge prominently at the top with a score tracker (e.g., `0 / 10`).
     - Animated falling fruit physics spawning at varied horizontal positions and increasing velocities.
     - Tapping the target fruit produces visual pop effects, haptic tick, and increments score. Tapping distractor fruits or missing fruits has no penalty but does not increment score.
     - Once 10/10 target fruits are tapped, alarm silences immediately.

4. **Dismissal Summary Screen**:
   - Displays "Challenge Complete! Alarm Dismissed" with victory badge.
   - Shows remaining upcoming sequential alarm times (e.g. "Next reminder at 6:35 AM").
   - "Got It" button returns user cleanly to the home screen.

### 2.2 Visual Identity & Styling

- **Theme Palette**:
  - `Background`: Deep Night Obsidian (`#0F1016`)
  - `Surface / Card`: Dark Slate Glass (`#181922` with `#232532` borders)
  - `Primary / Accent`: Solar Orange (`#FF6F00`) and Warm Amber (`#FFA000`)
  - `Secondary Accent`: Electric Sunrise Coral (`#FF5252`) and Golden Glow (`#FFD54F`)
  - `Text High Contrast`: Crisp White (`#FFFFFF`) and Light Silver (`#E0E0E6`)
  - `Text Muted`: Slate Gray (`#8E90A6`)
- **Typography**: Clean, geometric modern typography with bold clock numerals and crisp challenge instructions.
- **Component Styling**: Rounded M3 cards (20dp corner radius), illuminated action buttons, clear visual pill indicators for sequential alarms.

---

## 3. Key Product Decisions & Trade-Offs

- **Decision 1: Local Canvas Vector Path Validation vs. AI/Cloud Services**:
  - *Chosen Approach*: Pre-defined normalized vector path definitions for Star, House, and Dinosaur. Path segments are subdivided into discrete sampling checkpoints. As user touches move across the canvas, checkpoints within a proximity threshold (e.g., 28dp) are marked covered. Real-time coverage ratio = `covered_points / total_points`.
  - *Why*: 100% offline, zero latency, highly reliable, completely free, and works without network connectivity even in airplane mode.
- **Decision 2: 4-Alarm Sequential Generation Architecture**:
  - *Chosen Approach*: When an alarm entity is stored (e.g. ID `42` at `07:00`), the scheduler automatically creates 4 distinct exact intent alarms with IDs `42_0` (07:00), `42_1` (07:05), `42_2` (07:10), and `42_3` (07:15).
  - *Why*: Alarms are scheduled natively in the Android OS. If the user dismisses `42_0`, the subsequent alarms `42_1`, `42_2`, and `42_3` remain natively registered in `AlarmManager` without needing background daemon polling.
- **Decision 3: Single Active Session Guard**:
  - *Chosen Approach*: If an alarm occurrence triggers while a challenge from an earlier occurrence is still in progress, `AlarmReceiver` forwards to the active session without stacking duplicate activities or resetting the user's current drawing/fruit progress.
- **Decision 4: Local Room Database Persistence**:
  - *Chosen Approach*: Fully local Room DB with Kotlin Flows, storing alarm configurations, challenge configurations, and active states. Survives app termination and device reboot via `BOOT_COMPLETED` BroadcastReceiver.

---

## 4. Technical Architecture & Data Strategy

```
┌─────────────────────────────────────────────────────────────┐
│                       WakeQuest UI                          │
│  ┌─────────────────┐  ┌──────────────────┐  ┌─────────────┐ │
│  │   HomeScreen    │  │  AlarmEditScreen │  │ PracticeHub │ │
│  └────────┬────────┘  └────────┬─────────┘  └──────┬──────┘ │
│           │                    │                   │        │
│           └────────────────────┼───────────────────┘        │
│                                ▼                            │
│                       AlarmViewModel                        │
│                                │                            │
│                                ▼                            │
│                       AlarmRepository                       │
│                                │                            │
│                                ▼                            │
│                     Room Database (AppDb)                   │
└────────────────────────────────┬────────────────────────────┘
                                 │
                                 ▼
┌─────────────────────────────────────────────────────────────┐
│                 Alarm Scheduling & Execution                │
│  ┌───────────────────────────────────────────────────────┐  │
│  │ AlarmScheduler (Android AlarmManager Exact Alarms)    │  │
│  │  -> Schedules 4 sequential triggers (+0m,+5m,+10m,+15m)│  │
│  └──────────────────────────┬────────────────────────────┘  │
│                             │                               │
│                             ▼                               │
│  ┌───────────────────────────────────────────────────────┐  │
│  │ AlarmReceiver (BroadcastReceiver)                     │  │
│  │  -> BootReceiver (reschedules on device restart)      │  │
│  └──────────────────────────┬────────────────────────────┘  │
│                             │ (Intent with Alarm ID)        │
│                             ▼                               │
│  ┌───────────────────────────────────────────────────────┐  │
│  │ AlarmAlertActivity (Full-screen, wake-lock, audio/vib) │  │
│  │  ┌───────────────────────┐   ┌────────────────────────┐  │
│  │  │ DrawingChallengeCanvas│   │ FruitCatchingGameView  │  │
│  │  │ (Star/House/Dinosaur) │   │ (6 fruits, target 10)  │  │
│  │  └───────────────────────┘   └────────────────────────┘  │
│  │                             │                            │
│  │                             ▼ (On Complete)              │
│  │                     Stop Audio & Dismiss                 │
│  └───────────────────────────────────────────────────────┘  │
└─────────────────────────────────────────────────────────────┘
```

### 4.1 Data Models & Database Schema

```kotlin
// Alarm Entity
@Entity(tableName = "alarms")
data class AlarmEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val hour: Int,                // 0 - 23
    val minute: Int,              // 0 - 59
    val isEnabled: Boolean = true,
    val daysOfWeek: Int = 0,      // Bitmask or 7-day comma string
    val challengeType: ChallengeType, // DRAWING or FRUIT_GAME
    val drawingTemplate: DrawingTemplate = DrawingTemplate.DINOSAUR, // STAR, HOUSE, DINOSAUR
    val targetFruit: FruitType = FruitType.GRAPES, // APPLE, BANANA, GRAPES, ORANGE, STRAWBERRY, WATERMELON
    val label: String = "Wake Up!"
)
```

### 4.2 Drawing Challenge Mathematics

- Templates contain ordered and connected normalized lines/beziers normalized from `[0..1, 0..1]`.
- Upon canvas layout measurement, coordinates are mapped to pixel bounds.
- Target path points are discretized into 150-250 checkpoint nodes.
- When touch gestures move across canvas, nodes within radius `R = 32dp` are flagged as covered.
- Progress = `(coveredNodes.size / totalNodes.toDouble()) * 100%`. Target threshold: 85%.

### 4.3 Fruit Catching Game Loop

- Spawner runs on 60 FPS Compose `LaunchedEffect(Unit)` or ticker.
- Spawns falling fruit tokens with randomized X position, randomized fall speed (300dp/s to 650dp/s), and randomized fruit types.
- Target fruit matches the alarm configuration (e.g. Grapes).
- On pointer tap inside bounding circle of fruit:
  - If matches target fruit: burst particles, sound feedback, score += 1.
  - If wrong fruit: slight wobble or miss indicator, no score.
- On reaching score = 10, triggers completion event.

### 4.4 Permissions & Android Manifest Requirements

- `android.permission.SCHEDULE_EXACT_ALARM`
- `android.permission.USE_EXACT_ALARM`
- `android.permission.RECEIVE_BOOT_COMPLETED`
- `android.permission.WAKE_LOCK`
- `android.permission.VIBRATE`
- `android.permission.POST_NOTIFICATIONS`
- `android.permission.USE_FULL_SCREEN_INTENT`
