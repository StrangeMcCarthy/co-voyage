package covoyage.server.routes

import covoyage.server.model.AuthLoginRequest
import covoyage.server.model.AuthRegisterRequest
import covoyage.server.model.ChangePasswordRequest
import covoyage.server.service.AuthService
import io.ktor.http.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

/**
 * Authentication endpoints.
 *
 * POST /api/auth/register        — create account (password BCrypt-hashed)
 * POST /api/auth/login           — verify credentials
 * PUT  /api/auth/change-password — change password (requires old + new)
 */
fun Route.authRoutes(authService: AuthService) {
    route("/auth") {

        // Register
        post("/register") {
            val request = call.receive<AuthRegisterRequest>()
            val response = authService.register(request)
            call.respond(
                if (response.success) HttpStatusCode.Created else HttpStatusCode.BadRequest,
                response,
            )
        }

        // Login
        post("/login") {
            val request = call.receive<AuthLoginRequest>()
            val response = authService.login(request)
            call.respond(
                if (response.success) HttpStatusCode.OK else HttpStatusCode.Unauthorized,
                response,
            )
        }

        // Change password
        put("/change-password") {
            val request = call.receive<ChangePasswordRequest>()
            val response = authService.changePassword(request)
            call.respond(
                if (response.success) HttpStatusCode.OK else HttpStatusCode.BadRequest,
                response,
            )
        }
    }
}
