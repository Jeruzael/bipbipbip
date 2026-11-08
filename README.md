# WakeQuest ⏰🎮

**WakeQuest** is a challenge-based wake-up alarm clock Android application engineered to eliminate chronic oversleeping and morning snooze habits. Rather than allowing one-tap dismissals, WakeQuest forces users to engage their motor coordination and cognitive faculties through interactive challenges before silencing the alarm, backed by a mandatory sequential reminder system.

---

## 🌟 Key Features

### 1. Mandatory 4-Sequence Alarms (+0m, +5m, +10m, +15m)
- Every alarm automatically generates a non-negotiable **4-stage sequential reminder chain** at 5-minute intervals (Base Time, +5 min, +10 min, +15 min).
- Prevents falling back asleep after completing a single challenge; the subsequent reminders ensure full alertness.
- Clear visual timeline badges display the upcoming reminders across all active alarm cards.

### 2. Interactive Wake-Up Challenges
To turn off the alarm sound and dismiss the screen, users must complete their chosen challenge:
- **🎨 Drawing / Tracing Challenge**:
  - Requires tracing an animated silhouette template with smooth finger dragging.
  - Generates dense coordinate checkpoint paths and requires **at least 85% coverage** to unlock.
  - Three distinct templates and difficulty tiers:
    - **Star** (*Easy*)
    - **House** (*Medium*)
    - **Dinosaur (T-Rex)** (*Hard*)
  - Includes real-time progress indicators, glowing stroke feedback, and a reset button.
- **🍎 Fruit Catching Arcade Challenge**:
  - High-tempo arcade game where different fruits fall down the screen with physics simulation.
  - Users must identify and tap **10 designated target fruits** (Apple, Banana, Grapes, Orange, Strawberry, or Watermelon) while ignoring distractors.
  - Dynamic falling speed and touch hit-box detection.

### 3. Practice Arena / Sandbox Mode
- Test and practice any drawing template or fruit-catching minigame at any time directly from the top header or the alarm creation screen without setting off real alarms.

### 4. Robust Alarm & System Engine
- **`AlarmManager.setExactAndAllowWhileIdle`**: Guarantees alarm execution even when the Android device enters aggressive Doze power-saving states.
- **`AlarmRingingService` (Foreground Service)**: Plays looping system alarm audio via `AudioAttributes.USAGE_ALARM` paired with intense vibration patterns.
- **Lockscreen & Wakeup Override**: Uses `showWhenLocked`, `turnScreenOn`, and full-screen intent notifications to wake up sleeping devices and display over the lockscreen.
- **Boot & Time Resilience (`BootReceiver`)**: Automatically reschedules all enabled alarms following device reboots (`BOOT_COMPLETED`), app updates (`MY_PACKAGE_REPLACED`), manual clock changes, or timezone updates.
- **Local Persistence**: Built with Android Jetpack Room SQLite (`wakequest_alarms.db`) and Kotlin Coroutines/Flow for reactive UI state.

---

## 🛠️ Technology Stack & Architecture

- **Language**: Kotlin 2.2+
- **UI Framework**: Jetpack Compose with Material 3 design system
- **Build System**: Gradle 9.3+ with Android Gradle Plugin 9.1+ (Kotlin DSL `.gradle.kts`)
- **Architecture**: MVVM (Model-View-ViewModel) + Repository Pattern
- **Database**: Android Jetpack Room 2.7.0
- **Asynchronous**: Kotlin Coroutines & `StateFlow`
- **Minimum SDK**: Android 7.0 (API Level 24)
- **Target SDK**: Android 16 / VanillaIceCream (API Level 36)
- **Design Tokens**: Dark futuristic UI with Warm Sunrise Amber & Solar Orange accents

---

## 📋 Prerequisites & Setup

Ensure your development environment meets the following requirements:
1. **JDK 17 or higher**: Android Gradle Plugin 9.x requires JDK 17+ (e.g., Eclipse Adoptium Temurin 17 or OpenJDK 17/21). Set your `JAVA_HOME` environment variable accordingly.
2. **Android SDK**: Android API 36 / 36.1 platform installed via Android SDK Manager.
3. **Android Studio**: Android Studio (Ladybug, Meerkat, Koala, or newer recommended).
4. **Android Device or Emulator**:
   - A physical device running Android 7.0 (API 24) or newer with USB or Wireless debugging enabled, **OR**
   - An Android Virtual Device (AVD) running Google Play system images.
5. **Debug Keystore (`debug.keystore`)**:
   - WakeQuest includes a local `debug.keystore` in the project root (matching AI Studio conventions with password `android`).
   - If cloning fresh without it, copy your system debug keystore:
     ```powershell
     Copy-Item "$env:USERPROFILE\.android\debug.keystore" -Destination "debug.keystore"
     ```
     Or generate one via `keytool`:
     ```powershell
     keytool -genkey -v -keystore debug.keystore -storepass android -alias androiddebugkey -keypass android -keyalg RSA -keysize 2048 -validity 10000 -dname "CN=Android Debug,O=Android,C=US"
     ```

---

## 🚀 How to Run the Project

### Option A: Using Android Studio (Recommended)

1. **Clone / Open Project**:
   - Open Android Studio.
   - Click **File > Open...** and select the root directory of this repository (`bipbipbip`).
2. **Gradle Sync**:
   - Android Studio will detect the Gradle files and initiate a Gradle sync automatically.
   - Wait until dependency indexing and Gradle sync complete successfully.
3. **Select Target Device**:
   - In the top toolbar, select your connected physical device (via USB/Wi-Fi ADB) or an Android Virtual Device (AVD).
4. **Run**:
   - Click the green **Run 'app'** button (or press `Shift + F10`).
   - The app will compile, install onto the device, and launch `MainActivity`.

---

### Option B: Using the Command Line (Gradle Wrapper)

WakeQuest includes the Gradle Wrapper (`gradlew` / `gradlew.bat`), enabling standalone terminal builds.

#### 1. Configure Android SDK Path
If `ANDROID_HOME` or `ANDROID_SDK_ROOT` is not configured in your system environment, create a `local.properties` file in the project root:

**On Windows:**
```properties
sdk.dir=C\:\\Users\\<YourUsername>\\AppData\\Local\\Android\\Sdk
```

**On macOS / Linux:**
```properties
sdk.dir=/Users/<YourUsername>/Library/Android/sdk
```

#### 2. Check Connected Devices
Ensure your physical phone or emulator is recognized by ADB:
```bash
adb devices
```

#### 3. Build the Debug APK
- **Windows (PowerShell / Command Prompt)**:
  ```powershell
  .\gradlew.bat assembleDebug
  ```
- **macOS / Linux**:
  ```bash
  ./gradlew assembleDebug
  ```
*Output APK location:* `app/build/outputs/apk/debug/app-debug.apk`

#### 4. Install & Launch on Device
To automatically build, install, and launch the app:

**Install:**
- Windows:
  ```powershell
  .\gradlew.bat installDebug
  ```
- macOS / Linux:
  ```bash
  ./gradlew installDebug
  ```

**Launch App via ADB:**
```bash
adb shell am start -n com.aistudio.wakequest.kqywmx/com.example.MainActivity
```

---

### Option C: Wireless Debugging via ADB CLI 📶

If you develop using a terminal and connect to your Android device via Wireless Debugging (Wi-Fi), follow this dedicated guide.

#### 1. Pairing & Connecting (If needed)
If your wireless connection ever drops or after restarting Wi-Fi:
1. Enable **Developer Options > Wireless debugging** on your phone.
2. Tap **Pair device with pairing code**.
3. In terminal, pair and connect:
   ```powershell
   # Pair with IP and port shown on the pairing popup
   adb pair <DEVICE_IP>:<PAIRING_PORT> <PAIRING_CODE>

   # Connect with IP and port shown on the main Wireless debugging screen
   adb connect <DEVICE_IP>:<CONNECT_PORT>
   ```

#### 2. Verify Connected Devices & Avoid Multi-Device Ambiguity
Run:
```powershell
adb devices -l
```
Example output:
```text
List of devices attached
192.168.1.185:41235     device product:husky model:Pixel_8_Pro device:husky transport_id:3
adb-9A8B7C6D5E4F3210-xyzABC._adb-tls-connect._tcp device product:husky model:Pixel_8_Pro device:husky transport_id:2
```

> [!WARNING]
> When Wireless Debugging is enabled, ADB frequently detects both the **IP:Port** address and the **mDNS name** (e.g. `adb-XXXX._adb-tls-connect._tcp`). Running plain `adb` or Gradle commands without targeting will fail with `error: more than one device/emulator`.

To fix this, set the target serial once in your active terminal session:
- **PowerShell (Windows)**:
  ```powershell
  $env:ANDROID_SERIAL="192.168.1.185:41235"
  ```
- **Command Prompt (cmd.exe)**:
  ```cmd
  set ANDROID_SERIAL=192.168.1.185:41235
  ```
- **Bash / Zsh (macOS / Linux)**:
  ```bash
  export ANDROID_SERIAL="192.168.1.185:41235"
  ```

#### 3. One-Liner: Build, Install, & Launch via CLI
Run this single command in PowerShell to build, install, and immediately launch the app on your wireless phone:
```powershell
.\gradlew.bat assembleDebug; adb -s $env:ANDROID_SERIAL install -r app\build\outputs\apk\debug\app-debug.apk; adb -s $env:ANDROID_SERIAL shell am start -n com.aistudio.wakequest.kqywmx/com.example.MainActivity
```

Or using Gradle's direct install task:
```powershell
.\gradlew.bat installDebug
adb -s $env:ANDROID_SERIAL shell am start -n com.aistudio.wakequest.kqywmx/com.example.MainActivity
```

#### 4. Grant Required Permissions via ADB CLI (Skip UI Taps)
You can automatically grant all required system permissions directly from your command line without tapping on the device screen:

```powershell
# 1. Grant Notification permission (Android 13+)
adb -s $env:ANDROID_SERIAL shell pm grant com.aistudio.wakequest.kqywmx android.permission.POST_NOTIFICATIONS

# 2. Allow Exact Alarms (Android 12+)
adb -s $env:ANDROID_SERIAL shell appops set com.aistudio.wakequest.kqywmx SCHEDULE_EXACT_ALARM allow

# 3. Whitelist from Doze / Battery Optimization (Crucial for background alarms)
adb -s $env:ANDROID_SERIAL shell dumpsys deviceidle whitelist +com.aistudio.wakequest.kqywmx
```

#### 5. Essential ADB CLI Debugging Commands
- **Stream Colored App Logs (Filtered)**:
  ```powershell
  adb -s $env:ANDROID_SERIAL logcat -v color -s AlarmScheduler AlarmReceiver AlarmRingingService WakeQuest
  ```
- **Simulate Immediate Alarm Ringing (Tests challenge UI, sound & vibration)**:
  ```powershell
  adb -s $env:ANDROID_SERIAL shell am broadcast -a com.example.wakequest.ALARM_TRIGGER -p com.aistudio.wakequest.kqywmx --el extra_alarm_id 1 --ei extra_sequence_index 0
  ```
- **Simulate Device Reboot (Tests BootReceiver alarm restoration)**:
  ```powershell
  adb -s $env:ANDROID_SERIAL shell am broadcast -a android.intent.action.BOOT_COMPLETED -p com.aistudio.wakequest.kqywmx
  ```
- **Check if WakeQuest Process is Running**:
  ```powershell
  adb -s $env:ANDROID_SERIAL shell pidof com.aistudio.wakequest.kqywmx
  ```
- **Force Stop App**:
  ```powershell
  adb -s $env:ANDROID_SERIAL shell am force-stop com.aistudio.wakequest.kqywmx
  ```
- **Uninstall App Completely**:
  ```powershell
  adb -s $env:ANDROID_SERIAL uninstall com.aistudio.wakequest.kqywmx
  ```

---

## 🧪 Running Tests

### Unit Tests
Execute the local unit test suite (testing sequence calculations, checkpoint generation, and day formatting):

- **Windows**:
  ```powershell
  .\gradlew.bat test
  ```
- **macOS / Linux**:
  ```bash
  ./gradlew test
  ```
*Test reports will be generated at:* `app/build/reports/tests/testDebugUnitTest/index.html`

### Instrumented Tests
To run UI and instrumentation tests on a connected device:
```bash
.\gradlew.bat connectedAndroidTest
```

---

## ⚙️ Device Permissions & Setup Tips

For optimal reliability on modern Android versions (Android 12, 13, 14, 15+):

1. **Alarms & Reminders Permission (Android 12+)**:
   - Go to **Settings > Apps > Special App Access > Alarms & Reminders > WakeQuest**.
   - Ensure **Allow setting alarms** is toggled **ON**.
2. **Notification Permission (Android 13+)**:
   - On first launch, grant the notification prompt to allow high-priority foreground alert notifications.
3. **Battery Optimization / Background Autostart**:
   - On OEM devices (Xiaomi/HyperOS, Samsung OneUI, OnePlus, Huawei), set WakeQuest battery usage to **Unrestricted** / **Don't Optimize** to prevent OEM process killers from cancelling background alarm broadcasts.
4. **Display over other apps / Full-Screen Intents**:
   - Ensure WakeQuest has permission to show over the lock screen.

---

## 📂 Project Directory Structure

```text
bipbipbip/
├── app/
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/example/
│   │   │   │   ├── alarm/                  # Alarm scheduling, ringing service & receivers
│   │   │   │   │   ├── AlarmAlertActivity.kt      # Full-screen lockscreen challenge screen
│   │   │   │   │   ├── AlarmReceiver.kt           # Broadcast receiver triggering ringing
│   │   │   │   │   ├── AlarmRingingService.kt     # Foreground service for sound & vibration
│   │   │   │   │   ├── AlarmScheduler.kt          # 4-sequence exact alarm scheduling
│   │   │   │   │   └── BootReceiver.kt            # Re-registers alarms on device reboot
│   │   │   │   ├── challenge/              # Interactive challenges
│   │   │   │   │   ├── drawing/            # Tracing challenge & checkpoint math
│   │   │   │   │   └── fruit/              # Falling fruit arcade game
│   │   │   │   ├── data/                   # Room DB, Entity & DAO repositories
│   │   │   │   ├── model/                  # Data classes, Enums & Time helpers
│   │   │   │   ├── ui/                     # Jetpack Compose UI screens & dialogs
│   │   │   │   │   ├── AlarmEditDialog.kt         # Create & edit alarms
│   │   │   │   │   ├── AlarmViewModel.kt          # Alarm state management
│   │   │   │   │   ├── HomeScreen.kt              # Main alarm dashboard & live clock
│   │   │   │   │   ├── PracticeHubDialog.kt       # Standalone practice arena
│   │   │   │   │   └── theme/                     # Typography, colors & dark theme
│   │   │   │   ├── MainActivity.kt         # Entry activity & permission handling
│   │   │   │   └── WakeQuestApplication.kt # Notification channels & DB initialization
│   │   │   ├── res/                        # Drawables, mipmaps, strings, and XML
│   │   │   └── AndroidManifest.xml         # Permissions & component declarations
│   │   └── test/                           # JUnit unit test suite
│   └── build.gradle.kts                    # App-level build script & dependencies
├── gradle/
│   ├── libs.versions.toml                  # Version catalog for dependencies
│   └── wrapper/                            # Gradle wrapper binaries & configuration
├── .env.example                            # Template for API keys / secrets
├── build.gradle.kts                        # Root project build script
├── gradlew & gradlew.bat                   # Gradle wrapper scripts
└── settings.gradle.kts                     # Project & repository configurations
```

---

## 🔑 Environment Secrets (`.env`)

WakeQuest utilizes the Google Maps / Android Secrets Gradle Plugin configured via `.env`:
- Copy `.env.example` to `.env` if using features requiring external API tokens (such as Gemini API):
  ```bash
  cp .env.example .env
  ```
- If Gemini API is not actively utilized, leaving the default `.env.example` is sufficient for full local offline alarm functionality.

---

## 📄 License

This project is licensed under the **GNU Affero General Public License v3.0 or later (AGPL-3.0-or-later)**. See the [LICENSE](LICENSE) file for the full license text.
