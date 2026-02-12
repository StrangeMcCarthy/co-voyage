package com.covoyage.backend.services

import com.covoyage.backend.config.DatabaseConfig
import com.covoyage.backend.models.*
import com.mongodb.client.model.Filters
import com.mongodb.client.model.Updates
import com.mongodb.kotlin.client.coroutine.MongoCollection
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.toList
import org.bson.types.ObjectId

class BookingRepository {
    private val collection: MongoCollection<Booking>
        get() = DatabaseConfig.database.getCollection<Booking>("bookings")

    suspend fun create(booking: Booking): Booking {
        collection.insertOne(booking)
        return booking
    }

    suspend fun findById(id: String): Booking? {
        return collection.find(Filters.eq("_id", ObjectId(id))).firstOrNull()
    }

    suspend fun findByRideId(rideId: String): List<Booking> {
        return collection.find(Filters.eq("rideId", rideId)).toList()
    }

    suspend fun findByPassengerId(passengerId: String): List<Booking> {
        return collection.find(Filters.eq("passengerId", passengerId)).toList()
    }

    suspend fun findByDriverId(driverId: String): List<Booking> {
        return collection.find(Filters.eq("driverId", driverId)).toList()
    }

    suspend fun updateStatus(id: String, status: BookingStatus): Boolean {
        val result = collection.updateOne(
            Filters.eq("_id", ObjectId(id)),
            Updates.combine(
                Updates.set("status", status.name),
                Updates.currentDate("updatedAt")
            )
        )
        return result.modifiedCount > 0
    }

    suspend fun updatePaymentStatus(id: String, paymentStatus: String, paymentId: String): Boolean {
        val result = collection.updateOne(
            Filters.eq("_id", ObjectId(id)),
            Updates.combine(
                Updates.set("paymentStatus", paymentStatus),
                Updates.set("paymentId", paymentId),
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
