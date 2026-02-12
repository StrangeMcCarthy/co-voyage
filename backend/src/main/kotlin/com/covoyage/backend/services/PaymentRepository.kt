package com.covoyage.backend.services

import com.covoyage.backend.config.DatabaseConfig
import com.covoyage.backend.models.*
import com.mongodb.client.model.Filters
import com.mongodb.client.model.Updates
import com.mongodb.kotlin.client.coroutine.MongoCollection
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.toList
import org.bson.types.ObjectId

class PaymentRepository {
    private val collection: MongoCollection<Payment>
        get() = DatabaseConfig.database.getCollection<Payment>("payments")

    suspend fun create(payment: Payment): Payment {
        collection.insertOne(payment)
        return payment
    }

    suspend fun findById(id: String): Payment? {
        return collection.find(Filters.eq("_id", ObjectId(id))).firstOrNull()
    }

    suspend fun findByBookingId(bookingId: String): Payment? {
        return collection.find(Filters.eq("bookingId", bookingId)).firstOrNull()
    }

    suspend fun findByTransactionRef(ref: String): Payment? {
        return collection.find(Filters.eq("transactionRef", ref)).firstOrNull()
    }

    suspend fun findByUserId(userId: String): List<Payment> {
        return collection.find(Filters.eq("userId", userId)).toList()
    }

    suspend fun updateStatus(id: String, status: PaymentStatus): Boolean {
        val result = collection.updateOne(
            Filters.eq("_id", ObjectId(id)),
            Updates.combine(
                Updates.set("status", status.name),
                Updates.currentDate("updatedAt")
            )
        )
        return result.modifiedCount > 0
    }

    suspend fun updateFlutterwaveDetails(id: String, flutterwaveId: String, status: PaymentStatus): Boolean {
        val result = collection.updateOne(
            Filters.eq("_id", ObjectId(id)),
            Updates.combine(
                Updates.set("flutterwaveTransactionId", flutterwaveId),
                Updates.set("status", status.name),
                Updates.currentDate("updatedAt")
            )
        )
        return result.modifiedCount > 0
    }

    suspend fun delete(id: String): Boolean {
        val result = collection.deleteOne(Filters.eq("_id", ObjectId(id)))
        return result.deletedCount > 0
    }
}
