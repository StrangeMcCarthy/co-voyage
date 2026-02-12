package com.covoyage.backend.services

import com.covoyage.backend.config.DatabaseConfig
import com.covoyage.backend.models.Rating
import com.mongodb.client.model.Filters
import com.mongodb.kotlin.client.coroutine.MongoCollection
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.toList
import org.bson.types.ObjectId

class RatingRepository {
    private val collection: MongoCollection<Rating>
        get() = DatabaseConfig.database.getCollection<Rating>("ratings")

    suspend fun create(rating: Rating): Rating {
        collection.insertOne(rating)
        return rating
    }

    suspend fun findById(id: String): Rating? {
        return collection.find(Filters.eq("_id", ObjectId(id))).firstOrNull()
    }

    suspend fun findByBookingId(bookingId: String): List<Rating> {
        return collection.find(Filters.eq("bookingId", bookingId)).toList()
    }

    suspend fun findByReviewedUserId(userId: String): List<Rating> {
        return collection.find(Filters.eq("reviewedUserId", userId)).toList()
    }

    suspend fun findByReviewerUserId(userId: String): List<Rating> {
        return collection.find(Filters.eq("reviewerUserId", userId)).toList()
    }

    suspend fun existsByBookingAndReviewer(bookingId: String, reviewerUserId: String): Boolean {
        return collection.find(
            Filters.and(
                Filters.eq("bookingId", bookingId),
                Filters.eq("reviewerUserId", reviewerUserId)
            )
        ).firstOrNull() != null
    }
}
