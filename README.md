# Earn Meter

A production-ready Android app for gig drivers to maximize ride earnings by analyzing notifications from ride-sharing apps like Uber, Ola, and Rapido.

## 📱 Features

### Smart Assist (Configurable name)
- **Notification Monitoring**: Reads ride notifications from supported apps
- **Real-time Analysis**: Calculates earnings per KM, earnings per hour
- **Ride Classification**: Automatically categorizes rides as Good, Average, or Bad
- **Floating Overlay**: Shows ride quality at a glance with customizable popup

### Track Profits (Configurable name)
- **Custom Thresholds**: Set your own ranges for what constitutes a good, average, or bad ride
- **Earnings per KM**: Define your acceptable rates
- **Earnings per Hour**: Set hourly earning expectations
- **User Ratings**: Filter based on rider ratings
- **Admin Suggestions**: (V2) Get recommended ranges based on your city

### Ride Logging
- **Complete History**: All ride notifications are logged
- **Accept/Reject Tracking**: See which rides you accepted or rejected
- **Analytics**: Daily, weekly, monthly earnings summaries
- **Offline Support**: Data syncs when online

### Customizable Overlay
- **Font Size**: Adjust text size for visibility
- **Position**: Choose overlay position (corners or center)
- **Duration**: Control how long the popup stays visible
- **Opacity**: Adjust transparency

## 🏗️ Architecture

```
com.earnmeter.app/
├── data/
│   ├── local/           # Room database, DAOs, entities
│   ├── remote/          # Supabase client
│   └── repository/      # Repository implementations
├── di/                  # Hilt dependency injection modules
├── domain/
│   └── model/           # Domain models
├── presentation/
│   ├── auth/            # Login, OTP screens
│   ├── home/            # Main dashboard
│   ├── onboarding/      # Permissions setup
│   ├── profile/         # User profile
│   ├── rides/           # Ride history
│   ├── settings/        # App settings
│   ├── navigation/      # Navigation setup
│   └── theme/           # Material 3 theming
├── receiver/            # Boot receiver
└── service/             # Notification listener, Overlay service
```

### Tech Stack
- **Language**: Kotlin 1.9+
- **UI**: Jetpack Compose with Material 3
- **Architecture**: MVVM with Clean Architecture
- **DI**: Hilt
- **Database**: 
  - Remote: Supabase (PostgreSQL)
  - Local: Room (offline caching)
- **Networking**: Ktor (for Supabase SDK)
- **Async**: Kotlin Coroutines & Flow
- **Background**: Foreground Service + WorkManager

## 📋 Requirements

- **Minimum SDK**: 26 (Android 8.0 Oreo)
- **Target SDK**: 34 (Android 14)
- **Compile SDK**: 34

## 🔐 Permissions

| Permission | Purpose |
|------------|---------|
| `BIND_NOTIFICATION_LISTENER_SERVICE` | Read ride notifications |
| `SYSTEM_ALERT_WINDOW` | Display floating overlay |
| `REQUEST_IGNORE_BATTERY_OPTIMIZATIONS` | Run reliably in background |
| `FOREGROUND_SERVICE` | Keep service running |
| `INTERNET` | Sync with Supabase |
| `RECEIVE_BOOT_COMPLETED` | Restart service after reboot |

## 🚀 Setup

### 1. Clone & Open
```bash
cd earn-meter
```
Open in Android Studio Arctic Fox or later.

### 2. Configure Supabase

1. Go to [Supabase Dashboard](https://supabase.com/dashboard)
2. Get your project URL and anon key from Settings > API
3. Update `app/build.gradle.kts`:

```kotlin
buildConfigField("String", "SUPABASE_URL", "\"https://YOUR_PROJECT.supabase.co\"")
buildConfigField("String", "SUPABASE_ANON_KEY", "\"YOUR_ANON_KEY\"")
```

### 3. Set Up Database

Run the SQL schema in Supabase SQL Editor:
```bash
# Contents in supabase/schema.sql
```

### 4. Enable Phone Auth

In Supabase Dashboard:
1. Go to Authentication > Providers
2. Enable Phone provider
3. Configure SMS provider (Twilio recommended)

### 5. Build & Run
```bash
./gradlew assembleDebug
```

## ⚙️ Configuration

### Feature Names
Feature names are configurable via BuildConfig:

```kotlin
// app/build.gradle.kts
buildConfigField("String", "FEATURE_SMART_ASSIST_NAME", "\"Smart Assist\"")
buildConfigField("String", "FEATURE_TRACK_PROFITS_NAME", "\"Track Profits\"")
```

Or via remote config in Supabase `app_config` table.

### Product Flavors
Two flavors available:
- `standard`: Default feature names
- `custom`: For white-label versions

## 📊 Database Schema

### Tables
- `users` - User profiles
- `user_settings` - User preferences and thresholds
- `rides` - All ride notifications
- `ride_analytics` - Daily aggregated stats
- `admin_suggested_ranges` - City-wise suggested ranges (V2)
- `supported_apps` - Ride app package names
- `app_config` - Remote configuration

### Row Level Security
All user data is protected with RLS policies ensuring users can only access their own data.

## 🔄 Offline Support

The app uses a hybrid approach:
1. **Room Database**: Caches all data locally
2. **Sync on Connect**: Unsynced rides are uploaded when online
3. **Optimistic Updates**: Changes apply immediately locally

## 🎨 Theming

Uses Material 3 with a custom emerald/money-themed color palette:
- Primary: Emerald Green (#00C853)
- Good rides: Green (#2E7D32)
- Average rides: Orange (#F57C00)
- Bad rides: Red (#C62828)

## 📝 Adding New Ride Apps

1. Add to `supported_apps` table in Supabase:
```sql
INSERT INTO supported_apps (package_name, app_name, notification_patterns)
VALUES ('com.new.app', 'New App', ARRAY['pattern1', 'pattern2']);
```

2. The app will automatically fetch and use new patterns on next sync.

## 🐛 Debugging

### Notification Parser
Raw notification data is stored in `raw_notification_data` column for debugging parsing issues.

### Logs
```bash
adb logcat | grep "RideNotificationListener"
```

## 📦 Release

1. Update version in `app/build.gradle.kts`
2. Build release APK:
```bash
./gradlew assembleRelease
```
3. Sign with your keystore

## 🤝 Contributing

1. Fork the repository
2. Create feature branch
3. Submit pull request

## 📄 License

MIT License - see LICENSE file

## 🆘 Support

For issues or questions, open a GitHub issue.

---

Built with ❤️ for gig drivers
