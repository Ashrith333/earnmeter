package com.earnmeter.app.data.remote

import com.earnmeter.app.domain.model.AdminSuggestedRanges
import com.earnmeter.app.domain.model.AppConfig
import com.earnmeter.app.domain.model.Ride
import com.earnmeter.app.domain.model.RideAnalytics
import com.earnmeter.app.domain.model.SupportedApp
import com.earnmeter.app.domain.model.User
import com.earnmeter.app.domain.model.UserSettings
import io.github.jan.supabase.SupabaseClient as SupabaseClientLib
import io.github.jan.supabase.gotrue.Auth
import io.github.jan.supabase.gotrue.providers.Phone
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.postgrest.query.Columns
import io.github.jan.supabase.postgrest.query.Order
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SupabaseClient @Inject constructor(
    private val client: SupabaseClientLib,
    private val auth: Auth,
    private val postgrest: Postgrest
) {
    // ==========================================
    // AUTH OPERATIONS
    // ==========================================

    suspend fun signUpWithPhone(phoneNumber: String, password: String): Result<Unit> {
        return try {
            auth.signUpWith(Phone) {
                this.phoneNumber = phoneNumber
                this.password = password
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun signInWithPhone(phoneNumber: String, password: String): Result<Unit> {
        return try {
            auth.signInWith(Phone) {
                this.phoneNumber = phoneNumber
                this.password = password
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun verifyOtp(phoneNumber: String, otp: String): Result<Unit> {
        return try {
            auth.verifyPhoneOtp(
                type = io.github.jan.supabase.gotrue.OtpType.Phone.SMS,
                phone = phoneNumber,
                token = otp
            )
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun resendOtp(phoneNumber: String): Result<Unit> {
        return try {
            auth.resendPhone(
                type = io.github.jan.supabase.gotrue.OtpType.Phone.SMS,
                phoneNumber = phoneNumber
            )
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun resetPassword(phoneNumber: String): Result<Unit> {
        return try {
            auth.resetPasswordForEmail(phoneNumber) // Will use phone
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updatePassword(newPassword: String): Result<Unit> {
        return try {
            auth.updateUser {
                password = newPassword
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun signOut(): Result<Unit> {
        return try {
            auth.signOut()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun getCurrentUserId(): String? = auth.currentUserOrNull()?.id

    fun isLoggedIn(): Boolean = auth.currentUserOrNull() != null

    // ==========================================
    // USER OPERATIONS
    // ==========================================

    suspend fun getUser(userId: String): Result<User?> {
        return try {
            val user = postgrest.from("users")
                .select {
                    filter { eq("id", userId) }
                }
                .decodeSingleOrNull<User>()
            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateUser(user: User): Result<Unit> {
        return try {
            postgrest.from("users")
                .update(user) {
                    filter { eq("id", user.id) }
                }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun checkUserExists(phoneNumber: String): Result<Boolean> {
        return try {
            val user = postgrest.from("users")
                .select {
                    filter { eq("phone_number", phoneNumber) }
                }
                .decodeSingleOrNull<User>()
            Result.success(user != null)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ==========================================
    // USER SETTINGS OPERATIONS
    // ==========================================

    suspend fun getUserSettings(userId: String): Result<UserSettings?> {
        return try {
            val settings = postgrest.from("user_settings")
                .select {
                    filter { eq("user_id", userId) }
                }
                .decodeSingleOrNull<UserSettings>()
            Result.success(settings)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateUserSettings(settings: UserSettings): Result<Unit> {
        return try {
            postgrest.from("user_settings")
                .upsert(settings)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ==========================================
    // RIDE OPERATIONS
    // ==========================================

    suspend fun insertRide(ride: Ride): Result<Ride> {
        return try {
            val inserted = postgrest.from("rides")
                .insert(ride) {
                    select()
                }
                .decodeSingle<Ride>()
            Result.success(inserted)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateRide(ride: Ride): Result<Unit> {
        return try {
            postgrest.from("rides")
                .update(ride) {
                    filter { eq("id", ride.id!!) }
                }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getRides(
        userId: String,
        limit: Int = 50,
        offset: Int = 0
    ): Result<List<Ride>> {
        return try {
            val rides = postgrest.from("rides")
                .select {
                    filter { eq("user_id", userId) }
                    order("created_at", Order.DESCENDING)
                    limit(limit.toLong())
                    range(offset.toLong(), (offset + limit - 1).toLong())
                }
                .decodeList<Ride>()
            Result.success(rides)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getRidesByDate(
        userId: String,
        startDate: String,
        endDate: String
    ): Result<List<Ride>> {
        return try {
            val rides = postgrest.from("rides")
                .select {
                    filter {
                        eq("user_id", userId)
                        gte("created_at", startDate)
                        lte("created_at", endDate)
                    }
                    order("created_at", Order.DESCENDING)
                }
                .decodeList<Ride>()
            Result.success(rides)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ==========================================
    // ANALYTICS OPERATIONS
    // ==========================================

    suspend fun getAnalytics(userId: String, date: String): Result<RideAnalytics?> {
        return try {
            val analytics = postgrest.from("ride_analytics")
                .select {
                    filter {
                        eq("user_id", userId)
                        eq("date", date)
                    }
                }
                .decodeSingleOrNull<RideAnalytics>()
            Result.success(analytics)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun upsertAnalytics(analytics: RideAnalytics): Result<Unit> {
        return try {
            postgrest.from("ride_analytics")
                .upsert(analytics)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getAnalyticsRange(
        userId: String,
        startDate: String,
        endDate: String
    ): Result<List<RideAnalytics>> {
        return try {
            val analytics = postgrest.from("ride_analytics")
                .select {
                    filter {
                        eq("user_id", userId)
                        gte("date", startDate)
                        lte("date", endDate)
                    }
                    order("date", Order.DESCENDING)
                }
                .decodeList<RideAnalytics>()
            Result.success(analytics)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ==========================================
    // ADMIN/CONFIG OPERATIONS
    // ==========================================

    suspend fun getSuggestedRanges(city: String): Result<AdminSuggestedRanges?> {
        return try {
            val ranges = postgrest.from("admin_suggested_ranges")
                .select {
                    filter {
                        eq("city", city)
                        eq("is_active", true)
                    }
                }
                .decodeSingleOrNull<AdminSuggestedRanges>()
            Result.success(ranges)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getSupportedApps(): Result<List<SupportedApp>> {
        return try {
            val apps = postgrest.from("supported_apps")
                .select {
                    filter { eq("is_active", true) }
                }
                .decodeList<SupportedApp>()
            Result.success(apps)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getAppConfig(): Result<List<AppConfig>> {
        return try {
            val config = postgrest.from("app_config")
                .select {
                    filter { eq("is_active", true) }
                }
                .decodeList<AppConfig>()
            Result.success(config)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

