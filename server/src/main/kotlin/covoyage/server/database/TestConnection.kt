package covoyage.server.database

import com.mongodb.kotlin.client.coroutine.MongoClient
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import kotlin.time.Duration.Companion.seconds

fun main() {
    // URL Encoded password to handle special characters: & -> %26, + -> %2B, < -> %3C, > -> %3E
    val uri = "mongodb+srv://Cluster86192:%3C9DFR%3Er3V6Z%26,wW%2B%3E@cluster86192.8ask4yy.mongodb.net/?appName=Cluster86192"
    println("Attempting to connect to MongoDB Atlas with encoded credentials...")
    runBlocking {
        try {
            withTimeout(30.seconds) {
                val client = MongoClient.create(uri)
                val database = client.getDatabase("admin")
                val command = org.bson.Document("ping", 1)
                database.runCommand(command)
                println("SUCCESS: Connected to MongoDB Atlas!")
                client.close()
            }
        } catch (e: Exception) {
            println("FAILURE: Could not connect to MongoDB Atlas.")
            println("Error: ${e.message}")
            e.printStackTrace()
        }
    }
}
