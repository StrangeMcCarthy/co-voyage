package covoyage.server.routes

import covoyage.server.service.RideRequestService
import io.ktor.http.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.bson.Document

/**
 * REST endpoints for passenger ride requests.
 */
fun Route.rideRequestRoutes(rideRequestService: RideRequestService) {

    route("/ride-requests") {

        /** Create a new ride request (passenger posts) */
        post {
            val body = call.receive<Map<String, Any>>()
            val doc = Document(body)
            val response = rideRequestService.createRideRequest(doc)
            call.respond(
                if (response.success) HttpStatusCode.Created else HttpStatusCode.BadRequest,
                response,
            )
        }

        /** Get all open ride requests (public feed) */
        get {
            val requests = rideRequestService.getAllOpenRequests()
            call.respond(HttpStatusCode.OK, requests.map { it.toMap() })
        }

        /** Get ride requests by passenger */
        get("/passenger/{passengerId}") {
            val passengerId = call.parameters["passengerId"]
                ?: return@get call.respond(HttpStatusCode.BadRequest, "Missing passengerId")
            val requests = rideRequestService.getRequestsByPassenger(passengerId)
            call.respond(HttpStatusCode.OK, requests.map { it.toMap() })
        }

        /** Close a ride request */
        put("/{id}/close") {
            val requestId = call.parameters["id"]
                ?: return@put call.respond(HttpStatusCode.BadRequest, "Missing request ID")
            val body = call.receive<Map<String, String>>()
            val passengerId = body["passengerId"]
                ?: return@put call.respond(HttpStatusCode.BadRequest, "Missing passengerId")
            val response = rideRequestService.closeRequest(requestId, passengerId)
            call.respond(
                if (response.success) HttpStatusCode.OK else HttpStatusCode.BadRequest,
                response,
            )
        }
    }
}
