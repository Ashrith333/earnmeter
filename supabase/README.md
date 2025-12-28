# Supabase Setup

## Quick Start

1. Go to your Supabase Dashboard: https://supabase.com/dashboard/project/jfdcguuegggerpvvgxdc

2. Navigate to **SQL Editor**

3. Run the `schema.sql` file to create all tables

4. Enable Phone Auth:
   - Go to **Authentication** > **Providers**
   - Enable **Phone** provider
   - Configure your SMS provider (Twilio, Vonage, etc.)

## Database Connection Strings

**Direct Connection:**
```
postgresql://postgres:Ashashash333@db.jfdcguuegggerpvvgxdc.supabase.co:5432/postgres
```

**Transaction Pooler (Recommended for production):**
```
postgresql://postgres.jfdcguuegggerpvvgxdc:Ashashash333@aws-1-ap-south-1.pooler.supabase.com:6543/postgres
```

## Getting Your Anon Key

1. Go to **Settings** > **API**
2. Copy the `anon` key (public)
3. Add it to `app/build.gradle.kts`:

```kotlin
buildConfigField("String", "SUPABASE_ANON_KEY", "\"YOUR_ANON_KEY\"")
```

## Tables Created

| Table | Purpose |
|-------|---------|
| `users` | User profiles |
| `user_settings` | User preferences and ride thresholds |
| `rides` | All ride notifications logged |
| `ride_analytics` | Daily aggregated statistics |
| `admin_suggested_ranges` | City-wise suggested ranges (V2) |
| `supported_apps` | Ride app package names for filtering |
| `app_config` | Remote configuration |

## Row Level Security

All tables have RLS enabled. Users can only access their own data.

## Triggers

- `on_auth_user_created`: Automatically creates user profile and default settings when a new user signs up

## Default Data

The schema inserts:
- 3 supported apps (Uber, Ola, Rapido)
- Default app configuration for feature names

## SMS Provider Setup

For OTP authentication, configure one of:
- **Twilio** (Recommended)
- **Vonage**
- **MessageBird**

In Supabase Dashboard:
1. **Authentication** > **Providers** > **Phone**
2. Enable the provider
3. Add your SMS provider credentials

