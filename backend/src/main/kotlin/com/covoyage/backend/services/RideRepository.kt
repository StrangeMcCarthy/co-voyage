package com.covoyage.backend.services

import com.covoyage.backend.config.DatabaseConfig
import com.covoyage.backend.models.Ride
import com.covoyage.backend.models.RideFilter
import com.covoyage.backend.models.RideStatus
import com.mongodb.client.model.Filters
import com.mongodb.client.model.Filters.*
import com.mongodb.client.model.Sorts
import com.mongodb.client.model.Updates
import com.mongodb.kotlin.client.coroutine.MongoCollection
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.toList
import org.bson.conversions.Bson

class RideRepository {
    private val collection: MongoCollection<Ride> = 
        DatabaseConfig.database.getCollection("rides")
    
    suspend fun create(ride: Ride): Ride {
        collection.insertOne(ride)
        return ride
    }
    
    suspend fun findById(id: String): Ride? {
        return collection.find(eq("_id", id)).firstOrNull()
    }
    
    suspend fun search(
        filter: RideFilter,
        page: Int = 1,
        pageSize: Int = 20
    ): Pair<List<Ride>, Long> {
        val filters = mutableListOf<Bson>(eq("status", RideStatus.SCHEDULED.name))
        
        filter.departingTown?.let { filters.add(eq("departingTown", it)) }
        filter.destination?.let { filters.add(eq("destination", it)) }
        filter.date?.let { filters.add(eq("departureDate", it)) }
        filter.minSeats?.let { filters.add(gte("availableSeats", it)) }
        filter.maxPrice?.let { filters.add(lte("pricePerSeat", it)) }
        filter.minRating?.let { filters.add(gte("driverRating", it)) }
        
        val combinedFilter = and(filters)
        val skip = (page - 1) * pageSize
        
        val rides = collection.find(combinedFilter)
            .sort(Sorts.ascending("departureDate", "departureTime"))
            .skip(skip)
            .limit(pageSize)
            .toList()
        
        val total = collection.countDocuments(combinedFilter)
        
        return Pair(rides, total)
    }
    
    suspend fun findByDriverId(driverId: String): List<Ride> {
        return collection.find(eq("driverId", driverId))
            .sort(Sorts.descending("createdAt"))
            .toList()
    }
    
    suspend fun update(id: String, updates: Map<String, Any>): Boolean {
        val updateDoc = Updates.combine(
            updates.map { (key, value) -> Updates.set(key, value) }
        )
        val result = collection.updateOne(
            eq("_id", id),
            Updates.combine(updateDoc, Updates.set("updatedAt", System.currentTimeMillis()))
        )
        return result.modifiedCount > 0
    }
    
    suspend fun updateAvailableSeats(id: String, seatsBooked: Int): Boolean {
        val ride = findById(id) ?: return false
        val newAvailableSeats = ride.availableSeats - seatsBooked
        
        if (newAvailableSeats < 0) return false
        
        return update(id, mapOf("availableSeats" to newAvailableSeats))
    }
    
    suspend fun updateStatus(id: String, status: RideStatus, reason: String? = null): Boolean {
        val updates = mutableMapOf<String, Any>("status" to status.name)
        
        when (status) {
            RideStatus.IN_PROGRESS -> updates["startedAt"] = System.currentTimeMillis()
            RideStatus.COMPLETED -> updates["completedAt"] = System.currentTimeMillis()
            RideStatus.CANCELLED -> {
                updates["cancelledAt"] = System.currentTimeMillis()
                reason?.let { updates["cancellationReason"] = it }
            }
            else -> {}
        }
        
        return update(id, updates)
    }
    
    suspend fun delete(id: String): Boolean {
        val result = collection.deleteOne(eq("_id", id))
        return result.deletedCount > 0
    }
}
