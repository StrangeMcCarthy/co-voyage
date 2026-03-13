package covoyage.server.routes

import covoyage.server.service.MatchingService
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

/**
 * Routes for route matching and following.
 */
fun Route.matchingRoutes(matchingService: MatchingService) {
    route("/matching") {
        
        /** Follow a route for alerts */
        post("/follow") {
            val body = call.receive<Map<String, String>>()
            val userId = body["userId"] ?: return@post call.respond(HttpStatusCode.BadRequest, "Missing userId")
            val departureCity = body["departureCity"] ?: return@post call.respond(HttpStatusCode.BadRequest, "Missing departureCity")
            val arrivalCity = body["arrivalCity"] ?: return@post call.respond(HttpStatusCode.BadRequest, "Missing arrivalCity")
            
            matchingService.followRoute(userId, departureCity, arrivalCity)
            call.respond(HttpStatusCode.OK, mapOf("success" to true, "message" to "Now following $departureCity -> $arrivalCity"))
        }
    }
}
