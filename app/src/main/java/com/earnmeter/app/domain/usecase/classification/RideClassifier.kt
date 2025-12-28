package com.earnmeter.app.domain.usecase.classification

import com.earnmeter.app.domain.model.RideClassification
import com.earnmeter.app.domain.model.UserSettings
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Classifies rides based on user-defined thresholds.
 * Pure logic - no Android or database dependencies.
 * Can be unit tested independently.
 */
@Singleton
class RideClassifier @Inject constructor() {

    /**
     * Classify a ride based on multiple criteria
     */
    fun classify(
        earningsPerKm: Double?,
        earningsPerHour: Double?,
        riderRating: Double?,
        settings: UserSettings
    ): RideClassification {
        val scores = mutableListOf<ClassificationScore>()
        
        // Score based on earnings per km
        earningsPerKm?.let { epk ->
            scores.add(classifyMetric(epk, settings.goodEarningsPerKm, settings.avgEarningsPerKm, settings.badEarningsPerKm))
        }
        
        // Score based on earnings per hour
        earningsPerHour?.let { eph ->
            scores.add(classifyMetric(eph, settings.goodEarningsPerHour, settings.avgEarningsPerHour, settings.badEarningsPerHour))
        }
        
        // Score based on rating
        riderRating?.let { rating ->
            scores.add(classifyMetric(rating, settings.goodRating, settings.avgRating, settings.badRating))
        }
        
        return aggregateScores(scores)
    }

    /**
     * Classify a single metric against thresholds
     */
    private fun classifyMetric(
        value: Double,
        goodThreshold: Double,
        avgThreshold: Double,
        badThreshold: Double
    ): ClassificationScore {
        return when {
            value >= goodThreshold -> ClassificationScore.GOOD
            value >= avgThreshold -> ClassificationScore.AVERAGE
            value >= badThreshold -> ClassificationScore.BELOW_AVERAGE
            else -> ClassificationScore.BAD
        }
    }

    /**
     * Aggregate multiple scores into a final classification
     */
    private fun aggregateScores(scores: List<ClassificationScore>): RideClassification {
        if (scores.isEmpty()) return RideClassification.UNKNOWN
        
        val goodCount = scores.count { it == ClassificationScore.GOOD }
        val badCount = scores.count { it == ClassificationScore.BAD || it == ClassificationScore.BELOW_AVERAGE }
        val total = scores.size
        
        return when {
            // Majority good
            goodCount >= (total + 1) / 2 -> RideClassification.GOOD
            // Majority bad
            badCount >= (total + 1) / 2 -> RideClassification.BAD
            // Mixed
            goodCount > badCount -> RideClassification.GOOD
            badCount > goodCount -> RideClassification.BAD
            else -> RideClassification.AVERAGE
        }
    }

    /**
     * Get a detailed breakdown of classification
     */
    fun getClassificationDetails(
        earningsPerKm: Double?,
        earningsPerHour: Double?,
        riderRating: Double?,
        settings: UserSettings
    ): ClassificationDetails {
        return ClassificationDetails(
            epkClassification = earningsPerKm?.let { 
                classifyMetric(it, settings.goodEarningsPerKm, settings.avgEarningsPerKm, settings.badEarningsPerKm).toClassification()
            },
            ephClassification = earningsPerHour?.let {
                classifyMetric(it, settings.goodEarningsPerHour, settings.avgEarningsPerHour, settings.badEarningsPerHour).toClassification()
            },
            ratingClassification = riderRating?.let {
                classifyMetric(it, settings.goodRating, settings.avgRating, settings.badRating).toClassification()
            },
            overallClassification = classify(earningsPerKm, earningsPerHour, riderRating, settings)
        )
    }
}

private enum class ClassificationScore {
    GOOD, AVERAGE, BELOW_AVERAGE, BAD;
    
    fun toClassification(): RideClassification = when (this) {
        GOOD -> RideClassification.GOOD
        AVERAGE -> RideClassification.AVERAGE
        BELOW_AVERAGE, BAD -> RideClassification.BAD
    }
}

data class ClassificationDetails(
    val epkClassification: RideClassification?,
    val ephClassification: RideClassification?,
    val ratingClassification: RideClassification?,
    val overallClassification: RideClassification
)

