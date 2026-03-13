package covoyage.server.ui

enum class AdminLanguage(val code: String, val displayName: String, val flag: String) {
    EN("en", "English", "🇬🇧"),
    FR("fr", "Français", "🇫🇷");

    companion object {
        fun fromCode(code: String?): AdminLanguage = 
            entries.find { it.code.lowercase() == code?.lowercase() } ?: EN
    }
}

data class AdminStrings(
    val dashboard: String,
    val userManagement: String,
    val rideMonitoring: String,
    val payments: String,
    val logout: String,
    val totalUsers: String,
    val totalJourneys: String,
    val totalBookings: String,
    val totalRevenue: String,
    val rideVolume: String,
    val userGrowth: String,
    val last7Days: String,
    val userRegistry: String,
    val addUser: String,
    val name: String,
    val contact: String,
    val joined: String,
    val status: String,
    val action: String,
    val manage: String,
    val noUsersFound: String,
    val loginTitle: String,
    val secureAccess: String,
    val emailAddress: String,
    val password: String,
    val signInButton: String,
    val activeSession: String,
    val adminProfile: String,
    val adminUser: String,
    val systemManager: String,
)

fun adminStringsEn() = AdminStrings(
    dashboard = "Dashboard",
    userManagement = "User Management",
    rideMonitoring = "Ride Monitoring",
    payments = "Payments",
    logout = "Logout",
    totalUsers = "Total Users",
    totalJourneys = "Total Journeys",
    totalBookings = "Total Bookings",
    totalRevenue = "Total Revenue",
    rideVolume = "Ride Volume",
    userGrowth = "User Growth",
    last7Days = "Last 7 Days",
    userRegistry = "User Registry",
    addUser = "Add User",
    name = "Name",
    contact = "Contact",
    joined = "Joined",
    status = "Status",
    action = "Action",
    manage = "Manage",
    noUsersFound = "No users found in the registry.",
    loginTitle = "CoVoyage Admin",
    secureAccess = "Secure Dashboard Access",
    emailAddress = "Email Address",
    password = "Password",
    signInButton = "Sign In to Dashboard",
    activeSession = "Active Session",
    adminProfile = "Admin Profile",
    adminUser = "Admin User",
    systemManager = "System Manager"
)

fun adminStringsFr() = AdminStrings(
    dashboard = "Tableau de Bord",
    userManagement = "Gestion des Utilisateurs",
    rideMonitoring = "Suivi des Trajets",
    payments = "Paiements",
    logout = "Déconnexion",
    totalUsers = "Total Utilisateurs",
    totalJourneys = "Total Trajets",
    totalBookings = "Total Réservations",
    totalRevenue = "Revenu Total",
    rideVolume = "Volume de Trajets",
    userGrowth = "Croissance des Utilisateurs",
    last7Days = "7 Derniers Jours",
    userRegistry = "Registre des Utilisateurs",
    addUser = "Ajouter un Utilisateur",
    name = "Nom",
    contact = "Contact",
    joined = "Rejoint le",
    status = "Statut",
    action = "Action",
    manage = "Gérer",
    noUsersFound = "Aucun utilisateur trouvé dans le registre.",
    loginTitle = "CoVoyage Admin",
    secureAccess = "Accès Sécurisé au Tableau de Bord",
    emailAddress = "Adresse E-mail",
    password = "Mot de passe",
    signInButton = "Se connecter au Tableau de Bord",
    activeSession = "Session Active",
    adminProfile = "Profil Administrateur",
    adminUser = "Utilisateur Admin",
    systemManager = "Gestionnaire du Système"
)

fun adminStringsFor(language: AdminLanguage): AdminStrings = when (language) {
    AdminLanguage.EN -> adminStringsEn()
    AdminLanguage.FR -> adminStringsFr()
}
