package com.covoyage.backend.routes

import com.covoyage.backend.controllers.AuthController
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.routing.*

fun Route.authRoutes(authController: AuthController) {
    route("/auth") {
        post("/register") {
            authController.register(call)
        }
        
        post("/login") {
            authController.login(call)
        }
        
        authenticate("auth-jwt") {
            get("/me") {
                authController.getCurrentUser(call)
            }
            
            post("/logout") {
                authController.logout(call)
            }
        }
    }
}
