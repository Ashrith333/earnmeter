package com.earnmeter.app.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import com.earnmeter.app.BuildConfig
import com.earnmeter.app.data.local.EarnMeterDatabase
import com.earnmeter.app.data.local.dao.RideDao
import com.earnmeter.app.data.local.dao.UserSettingsDao
import com.earnmeter.app.data.remote.SupabaseClient
import com.earnmeter.app.data.repository.AuthRepository
import com.earnmeter.app.data.repository.AuthRepositoryImpl
import com.earnmeter.app.data.repository.RideRepository
import com.earnmeter.app.data.repository.RideRepositoryImpl
import com.earnmeter.app.data.repository.SettingsRepository
import com.earnmeter.app.data.repository.SettingsRepositoryImpl
import com.earnmeter.app.domain.usecase.classification.RideClassifier
import com.earnmeter.app.domain.usecase.notification.NotificationParser
import com.earnmeter.app.domain.usecase.overlay.OverlayManager
import com.earnmeter.app.domain.usecase.ride.ProcessRideNotificationUseCase
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import io.github.jan.supabase.SupabaseClient as SupabaseClientLib
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.gotrue.Auth
import io.github.jan.supabase.gotrue.auth
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.realtime.Realtime
import io.github.jan.supabase.storage.Storage
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "earn_meter_prefs")

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideSupabaseClient(): SupabaseClientLib {
        return createSupabaseClient(
            supabaseUrl = BuildConfig.SUPABASE_URL,
            supabaseKey = BuildConfig.SUPABASE_ANON_KEY
        ) {
            install(Auth) {
                // Phone auth configuration
            }
            install(Postgrest)
            install(Realtime)
            install(Storage)
        }
    }

    @Provides
    @Singleton
    fun provideSupabaseAuth(client: SupabaseClientLib): Auth = client.auth

    @Provides
    @Singleton
    fun provideSupabasePostgrest(client: SupabaseClientLib): Postgrest = client.postgrest

    @Provides
    @Singleton
    fun provideCustomSupabaseClient(
        client: SupabaseClientLib,
        auth: Auth,
        postgrest: Postgrest
    ): SupabaseClient = SupabaseClient(client, auth, postgrest)

    @Provides
    @Singleton
    fun provideDataStore(@ApplicationContext context: Context): DataStore<Preferences> {
        return context.dataStore
    }

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): EarnMeterDatabase {
        return EarnMeterDatabase.getInstance(context)
    }

    @Provides
    @Singleton
    fun provideRideDao(database: EarnMeterDatabase): RideDao = database.rideDao()

    @Provides
    @Singleton
    fun provideUserSettingsDao(database: EarnMeterDatabase): UserSettingsDao = database.userSettingsDao()

    // =====================================================
    // USE CASES - Business logic components
    // Each is independent and can be tested separately
    // =====================================================

    @Provides
    @Singleton
    fun provideNotificationParser(): NotificationParser = NotificationParser()

    @Provides
    @Singleton
    fun provideRideClassifier(): RideClassifier = RideClassifier()

    @Provides
    @Singleton
    fun provideOverlayManager(
        @ApplicationContext context: Context
    ): OverlayManager = OverlayManager(context)

    @Provides
    @Singleton
    fun provideProcessRideNotificationUseCase(
        notificationParser: NotificationParser,
        rideClassifier: RideClassifier,
        rideRepository: RideRepository,
        settingsRepository: SettingsRepository
    ): ProcessRideNotificationUseCase = ProcessRideNotificationUseCase(
        notificationParser = notificationParser,
        rideClassifier = rideClassifier,
        rideRepository = rideRepository,
        settingsRepository = settingsRepository
    )
}

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindAuthRepository(impl: AuthRepositoryImpl): AuthRepository

    @Binds
    @Singleton
    abstract fun bindRideRepository(impl: RideRepositoryImpl): RideRepository

    @Binds
    @Singleton
    abstract fun bindSettingsRepository(impl: SettingsRepositoryImpl): SettingsRepository
}

