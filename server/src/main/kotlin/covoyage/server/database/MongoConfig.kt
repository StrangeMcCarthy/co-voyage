package covoyage.server.database

import com.mongodb.kotlin.client.coroutine.MongoClient
import com.mongodb.kotlin.client.coroutine.MongoDatabase

/**
 * MongoDB configuration and connection.
 */
class MongoConfig {
    private val connectionString = System.getenv("MONGO_URI") ?: "mongodb://localhost:27017"
    private val databaseName = System.getenv("MONGO_DB") ?: "covoyage"

    private val client: MongoClient by lazy { MongoClient.create(connectionString) }

    val database: MongoDatabase by lazy { client.getDatabase(databaseName) }
}
