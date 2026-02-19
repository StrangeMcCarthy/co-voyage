package covoyage.server.routes

import covoyage.server.service.DashboardService
import io.ktor.http.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

/**
 * Dashboard REST endpoints for the admin web UI.
 */
fun Route.dashboardRoutes(dashboardService: DashboardService) {

    route("/dashboard") {

        /** Overview stats for header cards */
        get("/stats") {
            val stats = dashboardService.getOverviewStats()
            call.respond(HttpStatusCode.OK, stats)
        }

        /** Recent payments for transactions table */
        get("/payments") {
            val limit = call.parameters["limit"]?.toIntOrNull() ?: 50
            val payments = dashboardService.getRecentPayments(limit)
            call.respond(HttpStatusCode.OK, payments)
        }

        /** Revenue breakdown by payment method */
        get("/revenue") {
            val revenue = dashboardService.getRevenueByMethod()
            call.respond(HttpStatusCode.OK, revenue)
        }

        /** All journeys */
        get("/journeys") {
            val journeys = dashboardService.getAllJourneys()
            call.respond(HttpStatusCode.OK, journeys)
        }

        /** All bookings */
        get("/bookings") {
            val bookings = dashboardService.getAllBookings()
            call.respond(HttpStatusCode.OK, bookings)
        }
    }
}
