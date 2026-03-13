package covoyage.server.ui

import kotlinx.html.*
import io.ktor.server.html.*

fun HTML.adminLayout(pageTitle: String, strings: AdminStrings, bodyContent: FlowContent.() -> Unit) {
    head {
        title { +"CoVoyage Admin - $pageTitle" }
        meta { charset = "UTF-8" }
        meta { 
            name = "viewport"
            attributes["content"] = "width=device-width, initial-scale=1.0"
        }
        
        // Tailwind CSS
        script { src = "https://cdn.tailwindcss.com" }
        
        // Google Fonts - Inter
        link { 
            href = "https://fonts.googleapis.com/css2?family=Inter:wght@300;400;500;600;700&display=swap"
            rel = "stylesheet"
        }
        
        // Chart.js
        script { src = "https://cdn.jsdelivr.net/npm/chart.js" }
        
        style {
            unsafe {
                raw("""
                    body {
                        font-family: 'Inter', sans-serif;
                        background: #f0fdf4;
                        color: #166534;
                    }
                    .glass {
                        background: rgba(255, 255, 255, 0.8);
                        backdrop-filter: blur(12px);
                        border: 1px solid rgba(22, 101, 52, 0.1);
                        box-shadow: 0 4px 6px -1px rgb(0 0 0 / 0.05), 0 2px 4px -2px rgb(0 0 0 / 0.05);
                    }
                    .sidebar-link:hover {
                        background: rgba(34, 197, 94, 0.1);
                        color: #15803d;
                    }
                    .active-link {
                        background: rgba(34, 197, 94, 0.15);
                        color: #15803d;
                        border-right: 4px solid #22c55e;
                        font-weight: 600;
                    }
                """)
            }
        }
    }
    body {
        div("flex h-screen overflow-hidden") {
            // Sidebar
            div("w-64 bg-white flex-shrink-0 border-r border-green-100 hidden md:flex flex-col") {
                div("p-6 border-b border-green-50") {
                    h1("text-2xl font-bold bg-gradient-to-r from-green-600 to-emerald-500 bg-clip-text text-transparent") {
                        +"CoVoyage Admin"
                    }
                }
                nav("mt-6 flex-1") {
                    a(href = "/admin", classes = "sidebar-link flex items-center px-6 py-4 transition-all ${if (pageTitle == strings.dashboard) "active-link" else "text-green-700 font-medium"}") {
                        span("mr-3 text-xl") { +"📊" }
                        +strings.dashboard
                    }
                    a(href = "/admin/users", classes = "sidebar-link flex items-center px-6 py-4 transition-all ${if (pageTitle == strings.userManagement) "active-link" else "text-green-700 font-medium"}") {
                        span("mr-3 text-xl") { +"👥" }
                        +strings.userManagement
                    }
                    a(href = "/admin/rides", classes = "sidebar-link flex items-center px-6 py-4 transition-all ${if (pageTitle == strings.rideMonitoring) "active-link" else "text-green-700 font-medium"}") {
                        span("mr-3 text-xl") { +"🚗" }
                        +strings.rideMonitoring
                    }
                    a(href = "/admin/payments", classes = "sidebar-link flex items-center px-6 py-4 transition-all ${if (pageTitle == strings.payments) "active-link" else "text-green-700 font-medium"}") {
                        span("mr-3 text-xl") { +"💰" }
                        +strings.payments
                    }
                }
                
                // Logout Section
                div("p-4 border-t border-green-50") {
                    a(href = "/admin/logout", classes = "flex items-center px-4 py-3 text-red-600 hover:bg-red-50 rounded-xl transition-colors font-semibold") {
                        span("mr-3 text-xl") { +"🚪" }
                        +strings.logout
                    }
                }
                
                div("p-6 border-t border-green-50") {
                    div("flex items-center space-x-3") {
                        div("w-10 h-10 rounded-full bg-green-100 flex items-center justify-center text-green-700 font-bold") { +"A" }
                        div {
                            p("text-sm font-semibold text-green-900") { +strings.adminUser }
                            p("text-xs text-green-600") { +strings.systemManager }
                        }
                    }
                }
            }
            
            // Main Content
            div("flex-1 flex flex-col min-w-0") {
                // Header
                header("h-16 bg-white flex items-center justify-between px-8 border-b border-green-100 shadow-sm z-10") {
                    div("flex items-center space-x-4") {
                        h2("text-xl font-bold text-green-900") { +pageTitle }
                        // Language Switcher
                        div("flex items-center bg-green-50 rounded-lg p-1 space-x-1 ml-4") {
                            AdminLanguage.entries.forEach { lang ->
                                a(href = "/admin/set-language/${lang.code}", classes = "px-2 py-1 rounded text-xs font-bold transition-all ${if (lang.displayName == (if (strings.dashboard == "Dashboard") "English" else "Français")) "bg-green-600 text-white" else "text-green-700 hover:bg-green-100"}") {
                                    +lang.code.uppercase()
                                }
                            }
                        }
                    }
                    
                    div("flex items-center space-x-6") {
                        button(classes = "text-green-600 hover:text-green-700 relative") {
                            span("text-xl") { +"🔔" }
                            span("absolute top-0 right-0 block h-2 w-2 rounded-full bg-red-500 ring-2 ring-white")
                        }
                        span("h-6 w-px bg-green-100")
                        div("text-right hidden sm:block") {
                            p("text-sm font-medium text-green-900") { +strings.adminProfile }
                            p("text-xs text-green-500") { +strings.activeSession }
                        }
                    }
                }
                
                // Page Content
                main("flex-1 overflow-x-hidden overflow-y-auto p-8 bg-[#f8fafc]") {
                    bodyContent()
                }
            }
        }
    }
}

fun HTML.loginPage(strings: AdminStrings, error: String? = null) {
    head {
        title { +"CoVoyage Admin - Login" }
        meta { charset = "UTF-8" }
        meta { 
            name = "viewport"
            attributes["content"] = "width=device-width, initial-scale=1.0"
        }
        script { src = "https://cdn.tailwindcss.com" }
        link { 
            href = "https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700&display=swap"
            rel = "stylesheet"
        }
        style {
            unsafe {
                raw("""
                    body { font-family: 'Inter', sans-serif; background: #f0fdf4; }
                """)
            }
        }
    }
    body("h-screen flex items-center justify-center p-6") {
        div("w-full max-w-md bg-white rounded-3xl shadow-2xl p-10 border border-green-100") {
            div("text-center mb-10") {
                h1("text-3xl font-black text-green-900 mb-2") { +strings.loginTitle }
                p("text-green-600 font-medium") { +strings.secureAccess }
            }
            
            // Language Switcher on Login Page
            div("flex justify-center space-x-4 mb-8") {
                AdminLanguage.entries.forEach { lang ->
                    a(href = "/admin/set-language/${lang.code}", classes = "flex items-center space-x-2 px-3 py-2 rounded-xl border border-green-100 hover:bg-green-50 transition-all font-semibold text-green-700 text-sm") {
                        span { +lang.flag }
                        span { +lang.displayName }
                    }
                }
            }
            
            if (error != null) {
                div("bg-red-50 border border-red-100 text-red-700 px-4 py-3 rounded-xl mb-6 text-sm font-medium flex items-center") {
                    span("mr-2") { +"⚠️" }
                    +error
                }
            }
            
            form(action = "/admin/login", method = FormMethod.post, classes = "space-y-6") {
                div {
                    label("block text-sm font-bold text-green-900 mb-2") { +strings.emailAddress }
                    input(type = InputType.email, name = "email", classes = "w-full px-4 py-3 rounded-xl border border-green-100 focus:outline-none focus:ring-2 focus:ring-green-500 transition-all font-medium") {
                        placeholder = "djoko@email.com"
                        required = true
                    }
                }
                div {
                    label("block text-sm font-bold text-green-900 mb-2") { +strings.password }
                    input(type = InputType.password, name = "password", classes = "w-full px-4 py-3 rounded-xl border border-green-100 focus:outline-none focus:ring-2 focus:ring-green-500 transition-all font-medium") {
                        placeholder = "password123"
                        required = true
                    }
                }
                button(type = ButtonType.submit, classes = "w-full bg-green-600 text-white py-4 rounded-xl font-bold text-lg hover:bg-green-700 transform hover:-translate-y-0.5 transition-all shadow-lg active:scale-95") {
                    +strings.signInButton
                }
            }
            
            div("mt-10 text-center text-sm text-green-500 font-medium") {
                +"© 2026 CoVoyage Terminal • Cameroon"
            }
        }
    }
}
