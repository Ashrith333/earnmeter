# Build & Run Guide for Earn Meter

## Prerequisites

1. **Android Studio** (Arctic Fox 2020.3.1 or later)
   - Download from: https://developer.android.com/studio

2. **JDK 17** (bundled with Android Studio)

3. **Android Device or Emulator**
   - Minimum: Android 8.0 (API 26)
   - Recommended: Android 12+ for best overlay support

---

## Step 1: Set Up Supabase

### 1.1 Get Your Anon Key

1. Go to Supabase Dashboard: https://supabase.com/dashboard/project/jfdcguuegggerpvvgxdc
2. Navigate to **Settings** → **API**
3. Copy the `anon` key (the public one)

### 1.2 Update Build Config

Open `app/build.gradle.kts` and replace line 28:

```kotlin
buildConfigField("String", "SUPABASE_ANON_KEY", "\"YOUR_ANON_KEY_HERE\"")
```

With your actual key:

```kotlin
buildConfigField("String", "SUPABASE_ANON_KEY", "\"eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...\"")
```

### 1.3 Set Up Database

1. Go to Supabase Dashboard → **SQL Editor**
2. Click "New Query"
3. Copy and paste the contents of `supabase/schema.sql`
4. Click "Run"

### 1.4 Enable Phone Authentication

1. Go to **Authentication** → **Providers**
2. Find **Phone** and enable it
3. Configure SMS provider (e.g., Twilio):
   - Account SID
   - Auth Token
   - Messaging Service SID or Phone Number

---

## Step 2: Build the App

### Option A: Using Android Studio (Recommended)

1. Open Android Studio
2. File → Open → Select the `earn meter` folder
3. Wait for Gradle sync to complete
4. Click the green "Run" button (▶️) or press `Shift+F10`

### Option B: Using Command Line

```bash
# Navigate to project
cd "/Users/ash/Desktop/Newapp/earn meter"

# Make gradlew executable (first time only)
chmod +x gradlew

# Build debug APK
./gradlew assembleDebug

# The APK will be at:
# app/build/outputs/apk/standard/debug/app-standard-debug.apk
```

---

## Step 3: Install on Device

### Option A: Via Android Studio
- Just click "Run" with device connected

### Option B: Via ADB

```bash
# Connect your device via USB (enable USB debugging)
adb devices

# Install the APK
adb install app/build/outputs/apk/standard/debug/app-standard-debug.apk
```

### Option C: Manual Install
- Transfer the APK to your phone
- Open it and allow installation from unknown sources

---

## Step 4: First Run Setup

When you first open the app:

### 4.1 Phone Login
1. Enter your phone number (10 digits)
2. Tap "Continue"
3. If new user: Create password → Receive OTP → Verify
4. If existing user: Enter password → Login

### 4.2 Grant Permissions

The app will guide you through 3 permissions:

1. **Notification Access**
   - Tap "Grant" → Redirects to Android settings
   - Find "Earn Meter" → Enable toggle

2. **Display Over Apps**
   - Tap "Grant" → Redirects to Android settings
   - Find "Earn Meter" → Enable toggle

3. **Battery Optimization**
   - Tap "Grant" → Dialog appears
   - Select "Allow" or "Don't optimize"

### 4.3 Ready to Use!
- App will now monitor ride notifications
- Configure your thresholds in Settings → Track Profits

---

## Testing the App

### Test Without Ride Apps

Since you may not have ride notifications, you can test the overlay:

1. Open the app
2. Go to Settings → Overlay Settings
3. Use the "Preview" section to see how overlays look

### Simulate a Notification (Advanced)

Create a test notification using ADB:

```bash
# This simulates an Uber-like notification
adb shell am broadcast -a android.intent.action.SEND \
  -n com.earnmeter.app/.receiver.TestReceiver \
  --es "test_notification" "New trip request ₹250 8.5 km 15 min"
```

*(Note: Would need a TestReceiver to be added for this to work)*

---

## Project Architecture

```
┌─────────────────────────────────────────────────────────────┐
│                     PRESENTATION LAYER                       │
│  ┌─────────┐ ┌─────────┐ ┌─────────┐ ┌─────────┐           │
│  │  Auth   │ │  Home   │ │Settings │ │ Rides   │           │
│  │ Screens │ │ Screen  │ │ Screens │ │ Screen  │           │
│  └────┬────┘ └────┬────┘ └────┬────┘ └────┬────┘           │
│       │           │           │           │                  │
│  ┌────▼────┐ ┌────▼────┐ ┌────▼────┐ ┌────▼────┐           │
│  │  Auth   │ │  Home   │ │Settings │ │ Rides   │           │
│  │ViewModel│ │ViewModel│ │ViewModel│ │ViewModel│           │
│  └────┬────┘ └────┬────┘ └────┬────┘ └────┬────┘           │
└───────│───────────│───────────│───────────│─────────────────┘
        │           │           │           │
┌───────▼───────────▼───────────▼───────────▼─────────────────┐
│                      DOMAIN LAYER                            │
│  ┌──────────────────┐  ┌──────────────────┐                 │
│  │ NotificationParser│  │  RideClassifier  │  ← Pure Logic  │
│  │  (parses text)    │  │  (classifies)    │                 │
│  └────────┬─────────┘  └────────┬─────────┘                 │
│           │                      │                           │
│  ┌────────▼──────────────────────▼─────────┐                 │
│  │    ProcessRideNotificationUseCase       │  ← Orchestrates │
│  │    (coordinates parsing → classify)     │                 │
│  └────────┬─────────────────────┬──────────┘                 │
│           │                      │                           │
│  ┌────────▼─────────┐  ┌────────▼─────────┐                 │
│  │  OverlayManager  │  │   Repositories   │                 │
│  │  (shows popup)   │  │  (data access)   │                 │
│  └──────────────────┘  └────────┬─────────┘                 │
└─────────────────────────────────│───────────────────────────┘
                                  │
┌─────────────────────────────────▼───────────────────────────┐
│                       DATA LAYER                             │
│  ┌─────────────────┐        ┌─────────────────┐             │
│  │  SupabaseClient │        │   Room Database │             │
│  │  (remote sync)  │        │  (local cache)  │             │
│  └────────┬────────┘        └────────┬────────┘             │
│           │                          │                       │
│           ▼                          ▼                       │
│  ┌─────────────────────────────────────────────┐            │
│  │              PostgreSQL / SQLite             │            │
│  └─────────────────────────────────────────────┘            │
└─────────────────────────────────────────────────────────────┘
```

---

## Separation of Concerns

Each component is **independent** and **testable**:

| Component | Responsibility | Can Be Tested With |
|-----------|---------------|-------------------|
| `NotificationParser` | Extract data from notification text | Unit tests (no Android) |
| `RideClassifier` | Classify rides as good/bad | Unit tests (no Android) |
| `ProcessRideNotificationUseCase` | Orchestrate the flow | Unit tests with mocks |
| `OverlayManager` | Display overlay | Instrumented tests |
| `AuthRepository` | Handle authentication | Unit tests with mocks |
| `RideRepository` | Manage ride data | Unit tests with mocks |
| `SettingsRepository` | Manage user settings | Unit tests with mocks |

### Why This Matters

✅ **Working on classification doesn't break parsing**
- `RideClassifier` has no knowledge of how data is parsed
- You can change classification logic without touching parser

✅ **Working on overlay doesn't break data storage**
- `OverlayManager` just displays what it's given
- You can redesign the overlay without touching database code

✅ **Working on UI doesn't break services**
- ViewModels call repositories/use cases
- Services also call the same repositories/use cases
- Both stay in sync automatically

---

## Common Issues

### Gradle Sync Failed
```bash
# Clean and rebuild
./gradlew clean
./gradlew build
```

### Notification Access Not Working
- Make sure the app is listed in Settings → Apps → Special Access → Notification Access
- Toggle off and on again

### Overlay Not Showing
- Check Settings → Apps → Special Access → Display over other apps
- Some phones (Xiaomi, MIUI) have additional settings

### App Killed in Background
- Go to phone Settings → Battery → Earn Meter → Unrestricted
- Disable battery saver for this app

---

## Release Build

For production release:

```bash
# Create release keystore (first time)
keytool -genkey -v -keystore earn-meter-release.keystore \
  -alias earn-meter -keyalg RSA -keysize 2048 -validity 10000

# Create signing config in app/build.gradle.kts
# Then build release
./gradlew assembleRelease
```

---

## Need Help?

1. Check the logs: `adb logcat | grep -i earnmeter`
2. Check Supabase logs in Dashboard → Database → Logs
3. Open an issue on GitHub with:
   - Device model and Android version
   - Steps to reproduce
   - Relevant logs

