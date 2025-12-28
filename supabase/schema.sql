-- Earn Meter Database Schema for Supabase
-- Run this in Supabase SQL Editor to set up the database

-- Enable necessary extensions
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- ============================================
-- USERS TABLE
-- ============================================
CREATE TABLE IF NOT EXISTS users (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    phone_number VARCHAR(20) UNIQUE NOT NULL,
    full_name VARCHAR(255),
    email VARCHAR(255),
    city VARCHAR(100),
    state VARCHAR(100),
    country VARCHAR(100) DEFAULT 'India',
    profile_image_url TEXT,
    is_active BOOLEAN DEFAULT true,
    created_at TIMESTAMPTZ DEFAULT NOW(),
    updated_at TIMESTAMPTZ DEFAULT NOW()
);

-- ============================================
-- USER SETTINGS TABLE
-- ============================================
CREATE TABLE IF NOT EXISTS user_settings (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID REFERENCES users(id) ON DELETE CASCADE UNIQUE,
    
    -- Earnings per KM thresholds (in INR)
    good_earnings_per_km DECIMAL(10, 2) DEFAULT 15.0,
    avg_earnings_per_km DECIMAL(10, 2) DEFAULT 10.0,
    bad_earnings_per_km DECIMAL(10, 2) DEFAULT 5.0,
    
    -- Earnings per hour thresholds (in INR)
    good_earnings_per_hour DECIMAL(10, 2) DEFAULT 300.0,
    avg_earnings_per_hour DECIMAL(10, 2) DEFAULT 200.0,
    bad_earnings_per_hour DECIMAL(10, 2) DEFAULT 100.0,
    
    -- User rating thresholds
    good_rating DECIMAL(3, 2) DEFAULT 4.5,
    avg_rating DECIMAL(3, 2) DEFAULT 4.0,
    bad_rating DECIMAL(3, 2) DEFAULT 3.5,
    
    -- Overlay settings
    overlay_font_size INTEGER DEFAULT 14,
    overlay_position VARCHAR(20) DEFAULT 'TOP_RIGHT',
    overlay_duration_ms INTEGER DEFAULT 5000,
    overlay_opacity DECIMAL(3, 2) DEFAULT 0.9,
    
    -- Feature toggles
    smart_assist_enabled BOOLEAN DEFAULT true,
    track_profits_enabled BOOLEAN DEFAULT true,
    auto_suggest_ranges BOOLEAN DEFAULT false,
    
    created_at TIMESTAMPTZ DEFAULT NOW(),
    updated_at TIMESTAMPTZ DEFAULT NOW()
);

-- ============================================
-- RIDES TABLE
-- ============================================
CREATE TABLE IF NOT EXISTS rides (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID REFERENCES users(id) ON DELETE CASCADE,
    
    -- Source app
    source_app VARCHAR(50) NOT NULL,
    
    -- Ride details
    pickup_location TEXT,
    dropoff_location TEXT,
    distance_km DECIMAL(10, 2),
    estimated_duration_mins INTEGER,
    
    -- Earnings
    fare_amount DECIMAL(10, 2) NOT NULL,
    surge_multiplier DECIMAL(5, 2),
    tip_amount DECIMAL(10, 2),
    
    -- Calculated metrics
    earnings_per_km DECIMAL(10, 2),
    earnings_per_hour DECIMAL(10, 2),
    
    -- User rating
    rider_rating DECIMAL(3, 2),
    
    -- Decision
    action VARCHAR(20) DEFAULT 'pending',
    action_timestamp TIMESTAMPTZ,
    
    -- Classification
    classification VARCHAR(20) DEFAULT 'unknown',
    
    -- Timestamps
    notification_received_at TIMESTAMPTZ NOT NULL,
    created_at TIMESTAMPTZ DEFAULT NOW(),
    
    -- Raw data for debugging
    raw_notification_data TEXT
);

-- Create index for faster queries
CREATE INDEX IF NOT EXISTS idx_rides_user_id ON rides(user_id);
CREATE INDEX IF NOT EXISTS idx_rides_created_at ON rides(created_at DESC);
CREATE INDEX IF NOT EXISTS idx_rides_action ON rides(action);
CREATE INDEX IF NOT EXISTS idx_rides_source_app ON rides(source_app);

-- ============================================
-- RIDE ANALYTICS (Daily Summary)
-- ============================================
CREATE TABLE IF NOT EXISTS ride_analytics (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID REFERENCES users(id) ON DELETE CASCADE,
    date DATE NOT NULL,
    
    total_rides_received INTEGER DEFAULT 0,
    rides_accepted INTEGER DEFAULT 0,
    rides_rejected INTEGER DEFAULT 0,
    rides_missed INTEGER DEFAULT 0,
    
    total_earnings DECIMAL(10, 2) DEFAULT 0,
    total_distance_km DECIMAL(10, 2) DEFAULT 0,
    total_duration_mins INTEGER DEFAULT 0,
    
    avg_earnings_per_km DECIMAL(10, 2) DEFAULT 0,
    avg_earnings_per_hour DECIMAL(10, 2) DEFAULT 0,
    
    good_rides_count INTEGER DEFAULT 0,
    average_rides_count INTEGER DEFAULT 0,
    bad_rides_count INTEGER DEFAULT 0,
    
    created_at TIMESTAMPTZ DEFAULT NOW(),
    updated_at TIMESTAMPTZ DEFAULT NOW(),
    
    UNIQUE(user_id, date)
);

CREATE INDEX IF NOT EXISTS idx_analytics_user_date ON ride_analytics(user_id, date DESC);

-- ============================================
-- ADMIN SUGGESTED RANGES (V2 Feature)
-- ============================================
CREATE TABLE IF NOT EXISTS admin_suggested_ranges (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    city VARCHAR(100) NOT NULL,
    state VARCHAR(100),
    country VARCHAR(100) DEFAULT 'India',
    
    -- Suggested earnings per KM
    suggested_good_per_km DECIMAL(10, 2) NOT NULL,
    suggested_avg_per_km DECIMAL(10, 2) NOT NULL,
    suggested_bad_per_km DECIMAL(10, 2) NOT NULL,
    
    -- Suggested earnings per hour
    suggested_good_per_hour DECIMAL(10, 2) NOT NULL,
    suggested_avg_per_hour DECIMAL(10, 2) NOT NULL,
    suggested_bad_per_hour DECIMAL(10, 2) NOT NULL,
    
    -- Suggested rating thresholds
    suggested_good_rating DECIMAL(3, 2) NOT NULL,
    suggested_avg_rating DECIMAL(3, 2) NOT NULL,
    suggested_bad_rating DECIMAL(3, 2) NOT NULL,
    
    is_active BOOLEAN DEFAULT true,
    updated_at TIMESTAMPTZ DEFAULT NOW(),
    updated_by UUID REFERENCES users(id)
);

CREATE INDEX IF NOT EXISTS idx_suggested_ranges_city ON admin_suggested_ranges(city, state);

-- ============================================
-- SUPPORTED APPS TABLE
-- ============================================
CREATE TABLE IF NOT EXISTS supported_apps (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    package_name VARCHAR(255) UNIQUE NOT NULL,
    app_name VARCHAR(100) NOT NULL,
    notification_patterns TEXT[] NOT NULL,
    is_active BOOLEAN DEFAULT true,
    created_at TIMESTAMPTZ DEFAULT NOW(),
    updated_at TIMESTAMPTZ DEFAULT NOW()
);

-- Insert default supported apps
INSERT INTO supported_apps (package_name, app_name, notification_patterns) VALUES
    ('com.ubercab.driver', 'Uber Driver', ARRAY['New trip request', 'Trip nearby', '₹']),
    ('com.olacabs.oladriver', 'Ola Driver', ARRAY['New ride', 'Booking', '₹']),
    ('com.rapido.rider', 'Rapido Captain', ARRAY['New order', 'Trip request', '₹'])
ON CONFLICT (package_name) DO NOTHING;

-- ============================================
-- APP CONFIG TABLE
-- ============================================
CREATE TABLE IF NOT EXISTS app_config (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    config_key VARCHAR(100) UNIQUE NOT NULL,
    config_value TEXT NOT NULL,
    description TEXT,
    is_active BOOLEAN DEFAULT true,
    updated_at TIMESTAMPTZ DEFAULT NOW()
);

-- Insert default config
INSERT INTO app_config (config_key, config_value, description) VALUES
    ('feature_smart_assist_name', 'Smart Assist', 'Display name for Smart Assist feature'),
    ('feature_track_profits_name', 'Track Profits', 'Display name for Track Profits feature'),
    ('min_app_version', '1.0.0', 'Minimum supported app version'),
    ('force_update', 'false', 'Force users to update the app')
ON CONFLICT (config_key) DO NOTHING;

-- ============================================
-- ROW LEVEL SECURITY (RLS) POLICIES
-- ============================================

-- Enable RLS on all tables
ALTER TABLE users ENABLE ROW LEVEL SECURITY;
ALTER TABLE user_settings ENABLE ROW LEVEL SECURITY;
ALTER TABLE rides ENABLE ROW LEVEL SECURITY;
ALTER TABLE ride_analytics ENABLE ROW LEVEL SECURITY;

-- Users can only see their own data
CREATE POLICY "Users can view own profile" ON users
    FOR SELECT USING (auth.uid()::text = id::text);

CREATE POLICY "Users can update own profile" ON users
    FOR UPDATE USING (auth.uid()::text = id::text);

-- User settings policies
CREATE POLICY "Users can view own settings" ON user_settings
    FOR SELECT USING (auth.uid()::text = user_id::text);

CREATE POLICY "Users can insert own settings" ON user_settings
    FOR INSERT WITH CHECK (auth.uid()::text = user_id::text);

CREATE POLICY "Users can update own settings" ON user_settings
    FOR UPDATE USING (auth.uid()::text = user_id::text);

-- Rides policies
CREATE POLICY "Users can view own rides" ON rides
    FOR SELECT USING (auth.uid()::text = user_id::text);

CREATE POLICY "Users can insert own rides" ON rides
    FOR INSERT WITH CHECK (auth.uid()::text = user_id::text);

CREATE POLICY "Users can update own rides" ON rides
    FOR UPDATE USING (auth.uid()::text = user_id::text);

-- Analytics policies
CREATE POLICY "Users can view own analytics" ON ride_analytics
    FOR SELECT USING (auth.uid()::text = user_id::text);

CREATE POLICY "Users can insert own analytics" ON ride_analytics
    FOR INSERT WITH CHECK (auth.uid()::text = user_id::text);

CREATE POLICY "Users can update own analytics" ON ride_analytics
    FOR UPDATE USING (auth.uid()::text = user_id::text);

-- Public read access for config tables
ALTER TABLE admin_suggested_ranges ENABLE ROW LEVEL SECURITY;
ALTER TABLE supported_apps ENABLE ROW LEVEL SECURITY;
ALTER TABLE app_config ENABLE ROW LEVEL SECURITY;

CREATE POLICY "Anyone can read suggested ranges" ON admin_suggested_ranges
    FOR SELECT USING (true);

CREATE POLICY "Anyone can read supported apps" ON supported_apps
    FOR SELECT USING (true);

CREATE POLICY "Anyone can read app config" ON app_config
    FOR SELECT USING (true);

-- ============================================
-- FUNCTIONS & TRIGGERS
-- ============================================

-- Function to update updated_at timestamp
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = NOW();
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- Triggers for updated_at
CREATE TRIGGER update_users_updated_at
    BEFORE UPDATE ON users
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_user_settings_updated_at
    BEFORE UPDATE ON user_settings
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_ride_analytics_updated_at
    BEFORE UPDATE ON ride_analytics
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

-- Function to create user profile on signup
CREATE OR REPLACE FUNCTION handle_new_user()
RETURNS TRIGGER AS $$
BEGIN
    INSERT INTO users (id, phone_number)
    VALUES (NEW.id, NEW.phone);
    
    INSERT INTO user_settings (user_id)
    VALUES (NEW.id);
    
    RETURN NEW;
END;
$$ LANGUAGE plpgsql SECURITY DEFINER;

-- Trigger to create profile on auth signup
CREATE TRIGGER on_auth_user_created
    AFTER INSERT ON auth.users
    FOR EACH ROW EXECUTE FUNCTION handle_new_user();

