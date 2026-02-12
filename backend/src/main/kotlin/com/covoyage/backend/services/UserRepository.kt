package com.covoyage.backend.services

import com.covoyage.backend.config.DatabaseConfig
import com.covoyage.backend.models.User
import com.covoyage.backend.models.UserRole
import com.covoyage.backend.models.UserStatus
import com.mongodb.client.model.Filters
import com.mongodb.client.model.Updates
import com.mongodb.kotlin.client.coroutine.MongoCollection
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.toList
import org.bson.types.ObjectId

class UserRepository {
    private val collection: MongoCollection<User> = 
        DatabaseConfig.database.getCollection("users")
    
    suspend fun create(user: User): User {
        collection.insertOne(user)
        return user
    }
    
    suspend fun findById(id: String): User? {
        return collection.find(Filters.eq("_id", id)).firstOrNull()
    }
    
    suspend fun findByEmail(email: String): User? {
        return collection.find(Filters.eq("email", email)).firstOrNull()
    }
    
    suspend fun findByPhoneNumber(phoneNumber: String): User? {
        return collection.find(Filters.eq("phoneNumber", phoneNumber)).firstOrNull()
    }
    
    suspend fun update(id: String, updates: Map<String, Any>): Boolean {
        val updateDoc = Updates.combine(
            updates.map { (key, value) -> Updates.set(key, value) }
        )
        val result = collection.updateOne(
            Filters.eq("_id", id),
            Updates.combine(updateDoc, Updates.set("updatedAt", System.currentTimeMillis()))
        )
        return result.modifiedCount > 0
    }
    
    suspend fun updateRefreshToken(id: String, refreshToken: String?): Boolean {
        val result = collection.updateOne(
            Filters.eq("_id", id),
            Updates.combine(
                Updates.set("refreshToken", refreshToken),
                Updates.set("updatedAt", System.currentTimeMillis())
            )
        )
        return result.modifiedCount > 0
    }
    
    suspend fun updateLastLogin(id: String): Boolean {
        val result = collection.updateOne(
            Filters.eq("_id", id),
            Updates.combine(
                Updates.set("lastLoginAt", System.currentTimeMillis()),
                Updates.set("updatedAt", System.currentTimeMillis())
            )
        )
        return result.modifiedCount > 0
    }
    
    suspend fun updateStatus(id: String, status: UserStatus): Boolean {
        return update(id, mapOf("status" to status))
    }
    
    suspend fun updateRating(id: String, newRating: Double, increment: Boolean = true): Boolean {
        val user = findById(id) ?: return false
        
        val totalRatings = if (increment) user.totalRatings + 1 else user.totalRatings
        val averageRating = ((user.averageRating * user.totalRatings) + newRating) / totalRatings
        
        return update(id, mapOf(
            "averageRating" to averageRating,
            "totalRatings" to totalRatings
        ))
    }
    
    suspend fun incrementTotalRides(id: String): Boolean {
        val user = findById(id) ?: return false
        return update(id, mapOf("totalRides" to user.totalRides + 1))
    }
    
    suspend fun findByRole(role: UserRole): List<User> {
        return collection.find(Filters.eq("role", role.name)).toList()
    }
    
    suspend fun delete(id: String): Boolean {
        val result = collection.deleteOne(Filters.eq("_id", id))
        return result.deletedCount > 0
    }
}
