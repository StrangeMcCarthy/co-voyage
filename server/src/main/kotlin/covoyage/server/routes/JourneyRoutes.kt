package covoyage.server.routes

import covoyage.server.service.JourneyService
import io.ktor.http.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.bson.Document

/**
 * REST endpoints for journey management and trip lifecycle.
 */
fun Route.journeyRoutes(journeyService: JourneyService) {

    route("/journeys") {

        /** Create a new journey (driver posts a trip) */
        post {
            val body = call.receive<Map<String, Any>>()
            val doc = Document(body)
            val response = journeyService.createJourney(doc)
            call.respond(
                if (response.success) HttpStatusCode.Created else HttpStatusCode.BadRequest,
                response,
            )
        }

        /** Get all scheduled journeys (public feed) */
        get {
            val journeys = journeyService.getAllJourneys()
            call.respond(HttpStatusCode.OK, journeys.map { it.toMap() })
        }

        /** Get journeys by driver */
        get("/driver/{driverId}") {
            val driverId = call.parameters["driverId"]
                ?: return@get call.respond(HttpStatusCode.BadRequest, "Missing driverId")
            val journeys = journeyService.getJourneysByDriver(driverId)
            call.respond(HttpStatusCode.OK, journeys.map { it.toMap() })
        }

        /** Edit a journey */
        put("/{id}") {
            val journeyId = call.parameters["id"]
                ?: return@put call.respond(HttpStatusCode.BadRequest, "Missing journey ID")
            val body = call.receive<Map<String, Any>>()
            val driverId = body["driverId"] as? String
                ?: return@put call.respond(HttpStatusCode.BadRequest, "Missing driverId")
            val updates = Document(body)
            val response = journeyService.editJourney(journeyId, driverId, updates)
            call.respond(
                if (response.success) HttpStatusCode.OK else HttpStatusCode.BadRequest,
                response,
            )
        }

        /** Cancel/delete a journey */
        delete("/{id}") {
            val journeyId = call.parameters["id"]
                ?: return@delete call.respond(HttpStatusCode.BadRequest, "Missing journey ID")
            val driverId = call.request.queryParameters["driverId"]
                ?: return@delete call.respond(HttpStatusCode.BadRequest, "Missing driverId")
            val response = journeyService.cancelJourney(journeyId, driverId)
            call.respond(
                if (response.success) HttpStatusCode.OK else HttpStatusCode.BadRequest,
                response,
            )
        }

        /** Start a trip: SCHEDULED → IN_PROGRESS */
        post("/{id}/start") {
            val journeyId = call.parameters["id"]
                ?: return@post call.respond(HttpStatusCode.BadRequest, "Missing journey ID")
            val body = call.receive<Map<String, String>>()
            val driverId = body["driverId"]
                ?: return@post call.respond(HttpStatusCode.BadRequest, "Missing driverId")
            val response = journeyService.startTrip(journeyId, driverId)
            call.respond(
                if (response.success) HttpStatusCode.OK else HttpStatusCode.BadRequest,
                response,
            )
        }

        /** Complete a trip: IN_PROGRESS → COMPLETED (auto-releases escrow) */
        post("/{id}/complete") {
            val journeyId = call.parameters["id"]
                ?: return@post call.respond(HttpStatusCode.BadRequest, "Missing journey ID")
            val body = call.receive<Map<String, String>>()
            val driverId = body["driverId"]
                ?: return@post call.respond(HttpStatusCode.BadRequest, "Missing driverId")
            val response = journeyService.completeTrip(journeyId, driverId)
            call.respond(
                if (response.success) HttpStatusCode.OK else HttpStatusCode.BadRequest,
                response,
            )
        }

        /** Get driver payout history */
        get("/payouts/{driverId}") {
            val driverId = call.parameters["driverId"]
                ?: return@get call.respond(HttpStatusCode.BadRequest, "Missing driverId")
            val summary = journeyService.getDriverPayouts(driverId)
            call.respond(HttpStatusCode.OK, summary)
        }
    }
}
