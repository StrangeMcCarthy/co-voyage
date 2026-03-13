package covoyage.server.routes

import covoyage.server.model.AuthLoginRequest
import covoyage.server.service.AuthService
import covoyage.server.service.DashboardService
import covoyage.server.ui.*
import covoyage.server.security.AdminSession
import io.ktor.server.application.*
import io.ktor.server.html.*
import io.ktor.server.routing.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.sessions.*
import io.ktor.server.auth.*
import kotlinx.html.*

fun Route.adminRoutes(dashboardService: DashboardService, authService: AuthService) {
    route("/admin") {
        
        get("/set-language/{lang}") {
            val langCode = call.parameters["lang"] ?: "en"
            val session = call.sessions.get<AdminSession>()
            if (session != null) {
                call.sessions.set(session.copy(language = langCode.uppercase()))
            } else {
                // If no session, still store in a temporary session or cookie if needed, 
                // but for now we'll just set it for the next login
                call.sessions.set(AdminSession(userId = "", name = "", email = "", language = langCode.uppercase()))
            }
            call.respondRedirect(call.request.header("Referer") ?: "/admin")
        }

        get("/login") {
            val session = call.sessions.get<AdminSession>()
            val lang = AdminLanguage.fromCode(session?.language ?: "EN")
            val strings = adminStringsFor(lang)
            call.respondHtml {
                loginPage(strings)
            }
        }

        post("/login") {
            val params = call.receiveParameters()
            val email = params["email"] ?: ""
            val password = params["password"] ?: ""
            
            val session = call.sessions.get<AdminSession>()
            val currentLangCode = session?.language ?: "EN"
            
            val response = authService.verifyAdmin(AuthLoginRequest(email, password))
            if (response.success && response.user != null) {
                call.sessions.set(
                    AdminSession(
                        userId = response.user.id,
                        name = response.user.name,
                        email = response.user.email,
                        language = currentLangCode
                    )
                )
                call.respondRedirect("/admin")
            } else {
                val strings = adminStringsFor(AdminLanguage.fromCode(currentLangCode))
                call.respondHtml {
                    loginPage(strings, error = response.message)
                }
            }
        }

        get("/logout") {
            call.sessions.clear<AdminSession>()
            call.respondRedirect("/admin/login")
        }

        authenticate("admin-auth") {
            get {
                val session = call.sessions.get<AdminSession>()!!
                val strings = adminStringsFor(AdminLanguage.fromCode(session.language))
                val stats = dashboardService.getOverviewStats()
                val rideVolume = dashboardService.getRideVolumeByDayOfWeek()
                val userGrowth = dashboardService.getUserGrowthByWeek()
                
                call.respondHtml {
                    adminLayout(strings.dashboard, strings) {
                        // Stats Grid
                        div("grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6 mb-8") {
                            statCard(strings.totalUsers, stats.totalUsers.toString(), "👤", "bg-green-100 text-green-600")
                            statCard(strings.totalJourneys, stats.totalJourneys.toString(), "🚗", "bg-emerald-100 text-emerald-600")
                            statCard(strings.totalBookings, stats.totalBookings.toString(), "🎫", "bg-lime-100 text-lime-600")
                            statCard(strings.totalRevenue + " (XAF)", stats.totalRevenue.toString(), "💰", "bg-green-600 text-white")
                        }
                        
                        // Charts section
                        div("grid grid-cols-1 lg:grid-cols-2 gap-8 mb-8") {
                            div("glass p-6 rounded-2xl shadow-sm") {
                                h3("text-lg font-bold text-green-900 mb-4") { +strings.rideVolume }
                                canvas { id = "rideChart" }
                            }
                            div("glass p-6 rounded-2xl shadow-sm") {
                                h3("text-lg font-bold text-green-900 mb-4") { +strings.userGrowth }
                                canvas { id = "userChart" }
                            }
                        }
                        
                        // Chart initialization script
                        script {
                            unsafe {
                                raw("""
                                    const ctxRide = document.getElementById('rideChart').getContext('2d');
                                    new Chart(ctxRide, {
                                        type: 'line',
                                        data: {
                                            labels: ['Mon', 'Tue', 'Wed', 'Thu', 'Fri', 'Sat', 'Sun'],
                                            datasets: [{
                                                label: '${strings.rideVolume}',
                                                data: [${rideVolume.joinToString(", ")}],
                                                borderColor: '#16a34a',
                                                backgroundColor: 'rgba(22, 163, 74, 0.1)',
                                                fill: true,
                                                tension: 0.4,
                                                pointBackgroundColor: '#16a34a'
                                            }]
                                        },
                                        options: {
                                            responsive: true,
                                            plugins: { legend: { display: false } },
                                            scales: { 
                                                y: { beginAtZero: true, grid: { color: 'rgba(0,0,0,0.05)' } }, 
                                                x: { grid: { display: false } } 
                                            }
                                        }
                                    });
                                    
                                    const ctxUser = document.getElementById('userChart').getContext('2d');
                                    new Chart(ctxUser, {
                                        type: 'bar',
                                        data: {
                                            labels: ['Week 1', 'Week 2', 'Week 3', 'Week 4'],
                                            datasets: [{
                                                label: '${strings.userGrowth}',
                                                data: [${userGrowth.joinToString(", ")}],
                                                backgroundColor: '#22c55e',
                                                borderRadius: 8
                                            }]
                                        },
                                        options: {
                                            responsive: true,
                                            plugins: { legend: { display: false } },
                                            scales: { 
                                                y: { beginAtZero: true, grid: { color: 'rgba(0,0,0,0.05)' } }, 
                                                x: { grid: { display: false } } 
                                            }
                                        }
                                    });
                                """)
                            }
                        }
                    }
                }
            }
            
            get("/users") {
                val session = call.sessions.get<AdminSession>()!!
                val strings = adminStringsFor(AdminLanguage.fromCode(session.language))
                val users = dashboardService.getAllUsers()
                
                call.respondHtml {
                    adminLayout(strings.userManagement, strings) {
                        div("glass p-8 rounded-2xl shadow-sm") {
                            div("flex items-center justify-between mb-8") {
                                h3("text-2xl font-bold text-green-900") { +strings.userRegistry }
                                button(classes = "bg-green-600 text-white px-4 py-2 rounded-lg hover:bg-green-700 transition-colors flex items-center") {
                                    span("mr-2") { +"➕" }
                                    +strings.addUser
                                }
                            }
                            div("overflow-x-auto") {
                                table("w-full text-left") {
                                    thead("bg-green-50 border-b border-green-100") {
                                        tr {
                                            th(classes = "px-6 py-4 font-bold text-green-800") { +strings.name }
                                            th(classes = "px-6 py-4 font-bold text-green-800") { +strings.contact }
                                            th(classes = "px-6 py-4 font-bold text-green-800") { +strings.joined }
                                            th(classes = "px-6 py-4 font-bold text-green-800") { +strings.status }
                                            th(classes = "px-6 py-4 font-bold text-green-800") { +strings.action }
                                        }
                                    }
                                    tbody {
                                        if (users.isEmpty()) {
                                            tr {
                                                td(classes = "px-6 py-10 text-center text-green-500 italic") {
                                                    attributes["colspan"] = "5"
                                                    +strings.noUsersFound
                                                }
                                            }
                                        } else {
                                            users.forEach { user ->
                                                userRow(user.name, user.email.ifBlank { user.phone }, user.createdAt, user.status, strings.manage)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

fun FlowContent.statCard(title: String, value: String, icon: String, iconBgClass: String) {
    div("glass p-6 rounded-2xl flex items-center space-x-4 shadow-sm") {
        div("w-14 h-14 rounded-2xl flex items-center justify-center text-2xl $iconBgClass") {
            +icon
        }
        div {
            p("text-green-700 text-sm font-semibold mb-0.5") { +title }
            h4("text-2xl font-black text-green-900") { +value }
        }
    }
}

fun TBODY.userRow(name: String, contact: String, date: String, status: String, manageLabel: String) {
    tr("border-b border-green-50 hover:bg-green-50/50 transition-colors") {
        td("px-6 py-4") {
            div("flex items-center") {
                div("w-8 h-8 rounded-full bg-green-100 flex items-center justify-center text-green-700 font-bold mr-3 text-xs") {
                    +name.take(1).uppercase()
                }
                span("font-semibold text-green-900") { +name }
            }
        }
        td("px-6 py-4 text-green-700 font-medium") { +contact }
        td("px-6 py-4 text-green-600 text-sm") { +date }
        td("px-6 py-4") {
            val statusColor = if (status == "Active") "text-green-700 bg-green-100" else "text-orange-700 bg-orange-100"
            span("px-3 py-1 rounded-full text-xs font-bold $statusColor uppercase tracking-wider") { +status }
        }
        td("px-6 py-4") {
            button(classes = "text-green-600 hover:text-green-800 font-bold text-sm") { +manageLabel }
        }
    }
}
