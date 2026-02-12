package com.covoyage.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class Rating(
    val id: String,
    val bookingId: String,
    val reviewerId: String,
    val reviewedUserId: String,
    val rating: Int, // 1-5 stars
    val comment: String? = null,
    val categories: RatingCategories,
    val createdAt: Long
)

@Serializable
data class RatingCategories(
    val punctuality: Int? = null,
    val cleanliness: Int? = null,
    val communication: Int? = null,
    val safety: Int? = null,
    val comfort: Int? = null
)

@Serializable
data class CreateRatingRequest(
    val bookingId: String,
    val reviewedUserId: String,
    val rating: Int,
    val comment: String? = null,
    val categories: RatingCategories
)

@Serializable
data class UserRatingSummary(
    val userId: String,
    val averageRating: Double,
    val totalRatings: Int,
    val ratingDistribution: Map<Int, Int>, // star -> count
    val categoryAverages: RatingCategories,
    val recentReviews: List<Rating>
)
