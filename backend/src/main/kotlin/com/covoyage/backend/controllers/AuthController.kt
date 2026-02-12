package com.covoyage.backend.controllers

import com.covoyage.backend.config.JwtConfig
import com.covoyage.backend.models.*
import com.covoyage.backend.services.UserRepository
import com.covoyage.backend.utils.ApiResponse
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.request.*
import io.ktor.server.response.*

class AuthController(private val userRepository: UserRepository) {
    
    suspend fun register(call: ApplicationCall) {
        try {
            val request = call.receive<RegisterRequest>()
            
            // Validate request
            if (request.name.length < 2) {
                call.respond(HttpStatusCode.BadRequest, ApiResponse(
                    success = false,
                    message = "Name must be at least 2 characters"
                ))
                return
            }
            
            if (request.password.length < 6) {
                call.respond(HttpStatusCode.BadRequest, ApiResponse(
                    success = false,
                    message = "Password must be at least 6 characters"
                ))
                return
            }
            
            // Check if user exists
            val existingUser = userRepository.findByEmail(request.email)
            if (existingUser != null) {
                call.respond(HttpStatusCode.Conflict, ApiResponse(
                    success = false,
                    message = "User with this email already exists"
                ))
                return
            }
            
            // Validate driver-specific fields
            if (request.role == UserRole.DRIVER) {
                if (request.driverLicenseNumber.isNullOrBlank() || request.idNumber.isNullOrBlank()) {
                    call.respond(HttpStatusCode.BadRequest, ApiResponse(
                        success = false,
                        message = "Driver license number and ID number are required for drivers"
                    ))
                    return
                }
            }
            
            // Create user
            val user = User(
                name = request.name,
                email = request.email,
                passwordHash = User.hashPassword(request.password),
                phoneNumber = request.phoneNumber,
                town = request.town,
                role = request.role,
                status = if (request.role == UserRole.DRIVER) UserStatus.PENDING_VERIFICATION else UserStatus.VERIFIED,
                driverLicenseNumber = request.driverLicenseNumber,
                idNumber = request.idNumber
            )
            
            val savedUser = userRepository.create(user)
            
            // Generate tokens
            val token = JwtConfig.generateToken(savedUser.id)
            val refreshToken = JwtConfig.generateRefreshToken(savedUser.id)
            
            // Save refresh token
            userRepository.updateRefreshToken(savedUser.id, refreshToken)
            
            val response = AuthResponse(
                token = token,
                refreshToken = refreshToken,
                user = savedUser.toResponse()
            )
            
            call.respond(HttpStatusCode.Created, ApiResponse(
                success = true,
                data = response,
                message = "Registration successful"
            ))
            
        } catch (e: Exception) {
            call.application.environment.log.error("Registration error", e)
            call.respond(HttpStatusCode.InternalServerError, ApiResponse(
                success = false,
                message = "Registration failed: ${e.message}"
            ))
        }
    }
    
    suspend fun login(call: ApplicationCall) {
        try {
            val request = call.receive<LoginRequest>()
            
            // Find user
            val user = userRepository.findByEmail(request.email)
            if (user == null) {
                call.respond(HttpStatusCode.Unauthorized, ApiResponse(
                    success = false,
                    message = "Invalid credentials"
                ))
                return
            }
            
            // Verify password
            if (!User.verifyPassword(request.password, user.passwordHash)) {
                call.respond(HttpStatusCode.Unauthorized, ApiResponse(
                    success = false,
                    message = "Invalid credentials"
                ))
                return
            }
            
            // Check account status
            if (user.status == UserStatus.BANNED || user.status == UserStatus.SUSPENDED) {
                call.respond(HttpStatusCode.Forbidden, ApiResponse(
                    success = false,
                    message = "Your account has been ${user.status.name.lowercase()}"
                ))
                return
            }
            
            // Generate tokens
            val token = JwtConfig.generateToken(user.id)
            val refreshToken = JwtConfig.generateRefreshToken(user.id)
            
            // Update user
            userRepository.updateRefreshToken(user.id, refreshToken)
            userRepository.updateLastLogin(user.id)
            
            val response = AuthResponse(
                token = token,
                refreshToken = refreshToken,
                user = user.toResponse()
            )
            
            call.respond(HttpStatusCode.OK, ApiResponse(
                success = true,
                data = response,
                message = "Login successful"
            ))
            
        } catch (e: Exception) {
            call.application.environment.log.error("Login error", e)
            call.respond(HttpStatusCode.InternalServerError, ApiResponse(
                success = false,
                message = "Login failed: ${e.message}"
            ))
        }
    }
    
    suspend fun getCurrentUser(call: ApplicationCall) {
        try {
            val userId = call.principal<JWTPrincipal>()?.payload?.getClaim("userId")?.asString()
            if (userId == null) {
                call.respond(HttpStatusCode.Unauthorized, ApiResponse(
                    success = false,
                    message = "Unauthorized"
                ))
                return
            }
            
            val user = userRepository.findById(userId)
            if (user == null) {
                call.respond(HttpStatusCode.NotFound, ApiResponse(
                    success = false,
                    message = "User not found"
                ))
                return
            }
            
            call.respond(HttpStatusCode.OK, ApiResponse(
                success = true,
                data = user.toResponse()
            ))
            
        } catch (e: Exception) {
            call.application.environment.log.error("Get current user error", e)
            call.respond(HttpStatusCode.InternalServerError, ApiResponse(
                success = false,
                message = "Failed to get user: ${e.message}"
            ))
        }
    }
    
    suspend fun logout(call: ApplicationCall) {
        try {
            val userId = call.principal<JWTPrincipal>()?.payload?.getClaim("userId")?.asString()
            if (userId != null) {
                userRepository.updateRefreshToken(userId, null)
            }
            
            call.respond(HttpStatusCode.OK, ApiResponse(
                success = true,
                message = "Logout successful"
            ))
            
        } catch (e: Exception) {
            call.application.environment.log.error("Logout error", e)
            call.respond(HttpStatusCode.InternalServerError, ApiResponse(
                success = false,
                message = "Logout failed: ${e.message}"
            ))
        }
    }
}
