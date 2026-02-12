package com.covoyage.backend.config

import com.mongodb.kotlin.client.coroutine.MongoClient
import com.mongodb.kotlin.client.coroutine.MongoDatabase
import io.github.cdimascio.dotenv.dotenv

object DatabaseConfig {
    private val dotenv = dotenv {
        ignoreIfMissing = true
    }
    
    private val mongoUri = dotenv["MONGODB_URI"] ?: "mongodb://localhost:27017"
    private val databaseName = dotenv["MONGODB_DATABASE"] ?: "covoyage_db"
    
    val mongoClient: MongoClient by lazy {
        MongoClient.create(mongoUri)
    }
    
    val database: MongoDatabase by lazy {
        mongoClient.getDatabase(databaseName)
    }
    
    suspend fun init() {
        try {
            // Test connection
            database.listCollectionNames().first()
            println("✅ MongoDB connection established successfully")
            
            // Create indexes
            createIndexes()
        } catch (e: Exception) {
            println("❌ Failed to connect to MongoDB: ${e.message}")
            throw e
        }
    }
    
    private suspend fun createIndexes() {
        // User indexes
        val usersCollection = database.getCollection<org.bson.Document>("users")
        usersCollection.createIndex(org.bson.Document("email", 1), 
            com.mongodb.client.model.IndexOptions().unique(true))
        usersCollection.createIndex(org.bson.Document("phoneNumber", 1))
        usersCollection.createIndex(org.bson.Document("role", 1))
        
        // Ride indexes
        val ridesCollection = database.getCollection<org.bson.Document>("rides")
        ridesCollection.createIndex(org.bson.Document("driverId", 1))
        ridesCollection.createIndex(org.bson.Document("departingTown", 1))
        ridesCollection.createIndex(org.bson.Document("destination", 1))
        ridesCollection.createIndex(org.bson.Document("departureDate", 1))
        ridesCollection.createIndex(org.bson.Document("status", 1))
        
        // Booking indexes
        val bookingsCollection = database.getCollection<org.bson.Document>("bookings")
        bookingsCollection.createIndex(org.bson.Document("rideId", 1))
        bookingsCollection.createIndex(org.bson.Document("passengerId", 1))
        bookingsCollection.createIndex(org.bson.Document("driverId", 1))
        bookingsCollection.createIndex(org.bson.Document("status", 1))
        
        // Payment indexes
        val paymentsCollection = database.getCollection<org.bson.Document>("payments")
        paymentsCollection.createIndex(org.bson.Document("bookingId", 1))
        paymentsCollection.createIndex(org.bson.Document("userId", 1))
        paymentsCollection.createIndex(org.bson.Document("flutterwaveTransactionId", 1), 
            com.mongodb.client.model.IndexOptions().unique(true).sparse(true))
        
        println("✅ Database indexes created")
    }
    
    fun close() {
        mongoClient.close()
    }
}
