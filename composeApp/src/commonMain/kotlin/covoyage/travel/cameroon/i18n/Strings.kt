package covoyage.travel.cameroon.i18n

import androidx.compose.runtime.compositionLocalOf

/**
 * App language enum.
 */
enum class Language(val displayName: String, val flag: String) {
    EN("English", "🇬🇧"),
    FR("Français", "🇫🇷"),
}

val LocalLanguage = compositionLocalOf { Language.EN }
val LocalStrings = compositionLocalOf { stringsEn() }

/**
 * All translatable strings in the app, organized by screen/feature.
 */
data class Strings(
    // ── Common ──
    val appName: String,
    val appTagline: String,
    val back: String,
    val cancel: String,
    val confirm: String,
    val done: String,
    val search: String,
    val clear: String,
    val refresh: String,
    val total: String,
    val error: String,
    val loading: String,

    // ── Auth: Login ──
    val welcomeBack: String,
    val signInSubtitle: String,
    val email: String,
    val password: String,
    val signIn: String,
    val dontHaveAccount: String,
    val signUp: String,
    val demoAccounts: String,
    val demoAccountsDetail: String,

    // ── Auth: Registration ──
    val createAccount: String,
    val iAmA: String,
    val passenger: String,
    val driver: String,
    val findRides: String,
    val offerRides: String,
    val fullName: String,
    val phoneNumber: String,
    val driverDocuments: String,
    val drivingPermitNumber: String,
    val greyCardNumber: String,
    val confirmPassword: String,
    val registerAsDriver: String,
    val registerAsPassenger: String,
    val alreadyHaveAccount: String,

    // ── Auth: Validation ──
    val errorFillAllFields: String,
    val errorFillAllRequired: String,
    val errorPasswordsNoMatch: String,
    val errorPasswordTooShort: String,
    val errorDriverDocsRequired: String,
    val errorLoginFailed: String,
    val errorRegistrationFailed: String,

    // ── Navigation ──
    val tabRides: String,
    val tabMyTrips: String,
    val tabProfile: String,

    // ── Journey Feed ──
    val availableRides: String,
    val noRidesAvailable: String,
    val checkBackLater: String,
    val postARide: String,
    val from: String,
    val to: String,

    // ── Journey Card ──
    val seatsLeft: String, // "{n} seats left" — use format
    val xafSuffix: String, // "XAF"

    // ── Journey Detail ──
    val rideDetails: String,
    val date: String,
    val time: String,
    val availableSeats: String,
    val ofSeats: String, // "{available} of {total}"
    val vehicle: String,
    val plate: String,
    val driverLabel: String,
    val phone: String,
    val notes: String,
    val bookSeat: String, // "Book a Seat — {price} XAF"
    val fullyBooked: String,
    val perSeat: String, // "{price} XAF / seat"

    // ── Create Journey ──
    val postRide: String,
    val route: String,
    val departureCity: String,
    val arrivalCity: String,
    val schedule: String,
    val dateFormat: String,
    val timeFormat: String,
    val details: String,
    val seats: String,
    val priceXaf: String,
    val vehicleName: String,
    val vehicleModel: String,
    val saveVehicle: String,
    val selectSavedVehicle: String,
    val plateNumber: String,
    val additionalNotes: String,

    // ── Booking ──
    val bookYourRide: String,
    val numberOfSeats: String,
    val paymentMethod: String,
    val mtnMobileMoney: String,
    val orangeMoney: String,
    val cardPayment: String,
    val mobileMoneyNumber: String,
    val phoneHint: String,
    val cardDetails: String,
    val cardNumber: String,
    val removeSeat: String,
    val addSeat: String,
    val seatsTimesPrice: String, // "{n} seat(s) × {price} XAF"
    val payAmount: String, // "Pay {total} XAF"
    val waitingForPayment: String,
    val approveMtnMomo: String,
    val approveOrangeMoney: String,
    val processingCard: String,
    val checkPhoneNotification: String,

    // ── Payment Confirmation ──
    val bookingConfirmed: String,
    val paymentEscrowInfo: String,
    val summaryRoute: String,
    val summarySeats: String,
    val summaryTotalPaid: String,
    val summaryPaymentMethod: String,
    val summaryStatus: String,
    val heldInEscrow: String,
    val reference: String,
    val escrowExplanation: String,

    // ── Profile ──
    val profile: String,
    val driverBadge: String,
    val passengerBadge: String,
    val drivingPermit: String,
    val greyCard: String,
    val signOut: String,
    val language: String,

    // ── Driver Dashboard ──
    val myTrips: String,
    val payouts: String,
    val noTripsYet: String,
    val createFirstTrip: String,
    val newTrip: String,
    val startTrip: String,
    val completeTrip: String,
    val completeAndRelease: String,
    val startTripQuestion: String,
    val startTripConfirmText: String,
    val completeTripQuestion: String,
    val completeTripConfirmText: String,
    val cancelTripQuestion: String,
    val cancelTripConfirmText: String,

    // ── Journey Status ──
    val statusScheduled: String,
    val statusInProgress: String,
    val statusCompleted: String,
    val statusCancelled: String,

    // ── Payout History ──
    val earningsAndPayouts: String,
    val totalEarned: String,
    val pending: String,
    val completedTrips: String,
    val payoutHistory: String,
    val noPayoutsYet: String,
    val passengerFallback: String,
    val ofAmount: String, // "of {amount} XAF"

    // ── Chat ──
    val chat: String,
    val startConversation: String,
    val typeMessage: String,
    val send: String,

    // ── Ride Requests ──
    val tabRequests: String,
    val rideRequests: String,
    val noRequestsYet: String,
    val postRequestHint: String,
    val postARequest: String,
    val createRideRequest: String,
    val destination: String,
    val travelDate: String,
    val seatsNeeded: String,
    val yourMessage: String,
    val submitRequest: String,
    val rideRequestTag: String,
    val contactPassenger: String,
    val closeRequest: String,
    val requestClosed: String,
    val anyDriverAvailable: String,
    val requestCreated: String,
)

// ════════════════════════════════════════════
//  ENGLISH
// ════════════════════════════════════════════

fun stringsEn() = Strings(
    // Common
    appName = "CoVoyage",
    appTagline = "Travel Together Across Cameroon",
    back = "Back",
    cancel = "Cancel",
    confirm = "Confirm",
    done = "Done",
    search = "Search",
    clear = "Clear",
    refresh = "Refresh",
    total = "Total",
    error = "Error",
    loading = "Loading…",

    // Auth: Login
    welcomeBack = "Welcome Back",
    signInSubtitle = "Sign in to continue",
    email = "Email",
    password = "Password",
    signIn = "Sign In",
    dontHaveAccount = "Don't have an account? ",
    signUp = "Sign Up",
    demoAccounts = "🧪 Demo Accounts",
    demoAccountsDetail = "Driver: jpkamga@email.cm\nPassenger: mfotso@email.cm\nPassword: password123",

    // Auth: Registration
    createAccount = "Create Account",
    iAmA = "I am a...",
    passenger = "Passenger",
    driver = "Driver",
    findRides = "Find rides",
    offerRides = "Offer rides",
    fullName = "Full Name",
    phoneNumber = "Phone Number",
    driverDocuments = "Driver Documents",
    drivingPermitNumber = "Driving Permit Number",
    greyCardNumber = "Grey Card Number",
    confirmPassword = "Confirm Password",
    registerAsDriver = "Register as Driver",
    registerAsPassenger = "Register as Passenger",
    alreadyHaveAccount = "Already have an account? ",

    // Auth: Validation
    errorFillAllFields = "Please fill in all fields",
    errorFillAllRequired = "Please fill in all required fields",
    errorPasswordsNoMatch = "Passwords do not match",
    errorPasswordTooShort = "Password must be at least 6 characters",
    errorDriverDocsRequired = "Driving permit and grey card numbers are required for drivers",
    errorLoginFailed = "Login failed",
    errorRegistrationFailed = "Registration failed",

    // Navigation
    tabRides = "Rides",
    tabMyTrips = "My Trips",
    tabProfile = "Profile",

    // Journey Feed
    availableRides = "Available rides",
    noRidesAvailable = "No rides available",
    checkBackLater = "Check back later or adjust your search",
    postARide = "Post a ride",
    from = "From",
    to = "To",

    // Journey Card
    seatsLeft = "seats left",
    xafSuffix = "XAF",

    // Journey Detail
    rideDetails = "Ride Details",
    date = "Date",
    time = "Time",
    availableSeats = "Available seats",
    ofSeats = "of",
    vehicle = "Vehicle",
    plate = "Plate",
    driverLabel = "Driver",
    phone = "Phone",
    notes = "Notes",
    bookSeat = "Book a Seat",
    fullyBooked = "Fully Booked",
    perSeat = "/ seat",

    // Create Journey
    postRide = "Post Ride",
    route = "Route",
    departureCity = "Departure City",
    arrivalCity = "Arrival City",
    schedule = "Schedule",
    dateFormat = "Date (YYYY-MM-DD)",
    timeFormat = "Time (HH:MM)",
    details = "Details",
    seats = "Seats",
    priceXaf = "Price (XAF)",
    vehicleName = "Car Name (e.g. Toyota)",
    vehicleModel = "Model (e.g. Corolla 2020)",
    saveVehicle = "Save this vehicle for future rides",
    selectSavedVehicle = "Select a saved vehicle",
    plateNumber = "Plate Number",
    additionalNotes = "Additional notes (optional)",

    // Booking
    bookYourRide = "Book Your Ride",
    numberOfSeats = "Number of Seats",
    paymentMethod = "Payment Method",
    mtnMobileMoney = "MTN Mobile Money",
    orangeMoney = "Orange Money",
    cardPayment = "Card Payment",
    mobileMoneyNumber = "Mobile Money Number",
    phoneHint = "Phone number (e.g. 237670000000)",
    cardDetails = "Card Details",
    cardNumber = "Card Number",
    removeSeat = "Remove seat",
    addSeat = "Add seat",
    seatsTimesPrice = "seat(s) ×",
    payAmount = "Pay",
    waitingForPayment = "Waiting for payment...",
    approveMtnMomo = "Approve the payment on your MTN MoMo app",
    approveOrangeMoney = "Approve the payment on your Orange Money app",
    processingCard = "Processing your card payment...",
    checkPhoneNotification = "Check your phone for a notification",

    // Payment Confirmation
    bookingConfirmed = "Booking Confirmed!",
    paymentEscrowInfo = "Your payment has been received and is held securely in escrow.",
    summaryRoute = "Route",
    summarySeats = "Seats",
    summaryTotalPaid = "Total Paid",
    summaryPaymentMethod = "Payment Method",
    summaryStatus = "Status",
    heldInEscrow = "Held in Escrow ✓",
    reference = "Reference",
    escrowExplanation = "🔒 Your payment is held securely. The driver receives 90% after trip completion. The remaining 10% is the platform service fee.",

    // Profile
    profile = "Profile",
    driverBadge = "🚗 Driver",
    passengerBadge = "🧳 Passenger",
    drivingPermit = "Driving Permit",
    greyCard = "Grey Card",
    signOut = "Sign Out",
    language = "Language",

    // Driver Dashboard
    myTrips = "My Trips",
    payouts = "Payouts",
    noTripsYet = "No trips yet",
    createFirstTrip = "Create your first trip to start earning",
    newTrip = "New Trip",
    startTrip = "Start Trip",
    completeTrip = "Complete Trip",
    completeAndRelease = "Complete Trip & Release Payments",
    startTripQuestion = "Start Trip?",
    startTripConfirmText = "Passengers will be notified that the trip is underway.",
    completeTripQuestion = "Complete Trip?",
    completeTripConfirmText = "This will release escrow payments (90%) to your account.",
    cancelTripQuestion = "Cancel Trip?",
    cancelTripConfirmText = "All held payments will be refunded to passengers.",

    // Journey Status
    statusScheduled = "Scheduled",
    statusInProgress = "In Progress",
    statusCompleted = "Completed",
    statusCancelled = "Cancelled",

    // Payout History
    earningsAndPayouts = "Earnings & Payouts",
    totalEarned = "Total Earned",
    pending = "Pending",
    completedTrips = "Completed Trips",
    payoutHistory = "Payout History",
    noPayoutsYet = "No payouts yet. Complete trips to earn!",
    passengerFallback = "Passenger",
    ofAmount = "of",

    // Chat
    chat = "Chat",
    startConversation = "Start the conversation! 💬",
    typeMessage = "Type a message...",
    send = "Send",

    // Ride Requests
    tabRequests = "Requests",
    rideRequests = "Ride Requests",
    noRequestsYet = "No ride requests yet",
    postRequestHint = "Post a request if you need a ride",
    postARequest = "Request a Ride",
    createRideRequest = "New Ride Request",
    destination = "Destination",
    travelDate = "Travel Date",
    seatsNeeded = "Seats Needed",
    yourMessage = "Your message (optional)",
    submitRequest = "Post Request",
    rideRequestTag = "🙋 Ride Request",
    contactPassenger = "Contact Passenger",
    closeRequest = "Close Request",
    requestClosed = "This will remove your request from the feed.",
    anyDriverAvailable = "Any driver available?",
    requestCreated = "Request posted!",
)

// ════════════════════════════════════════════
//  FRANÇAIS
// ════════════════════════════════════════════

fun stringsFr() = Strings(
    // Commun
    appName = "CoVoyage",
    appTagline = "Voyageons ensemble à travers le Cameroun",
    back = "Retour",
    cancel = "Annuler",
    confirm = "Confirmer",
    done = "Terminé",
    search = "Rechercher",
    clear = "Effacer",
    refresh = "Actualiser",
    total = "Total",
    error = "Erreur",
    loading = "Chargement…",

    // Auth : Connexion
    welcomeBack = "Bon retour",
    signInSubtitle = "Connectez-vous pour continuer",
    email = "E-mail",
    password = "Mot de passe",
    signIn = "Se connecter",
    dontHaveAccount = "Pas encore de compte ? ",
    signUp = "S'inscrire",
    demoAccounts = "🧪 Comptes de démonstration",
    demoAccountsDetail = "Chauffeur : jpkamga@email.cm\nPassager : mfotso@email.cm\nMot de passe : password123",

    // Auth : Inscription
    createAccount = "Créer un compte",
    iAmA = "Je suis…",
    passenger = "Passager",
    driver = "Chauffeur",
    findRides = "Trouver des trajets",
    offerRides = "Proposer des trajets",
    fullName = "Nom complet",
    phoneNumber = "Numéro de téléphone",
    driverDocuments = "Documents du chauffeur",
    drivingPermitNumber = "Numéro de permis de conduire",
    greyCardNumber = "Numéro de carte grise",
    confirmPassword = "Confirmer le mot de passe",
    registerAsDriver = "S'inscrire en tant que chauffeur",
    registerAsPassenger = "S'inscrire en tant que passager",
    alreadyHaveAccount = "Vous avez déjà un compte ? ",

    // Auth : Validation
    errorFillAllFields = "Veuillez remplir tous les champs",
    errorFillAllRequired = "Veuillez remplir tous les champs obligatoires",
    errorPasswordsNoMatch = "Les mots de passe ne correspondent pas",
    errorPasswordTooShort = "Le mot de passe doit contenir au moins 6 caractères",
    errorDriverDocsRequired = "Le numéro de permis et de carte grise sont requis pour les chauffeurs",
    errorLoginFailed = "Échec de la connexion",
    errorRegistrationFailed = "Échec de l'inscription",

    // Navigation
    tabRides = "Trajets",
    tabMyTrips = "Mes trajets",
    tabProfile = "Profil",

    // Flux de trajets
    availableRides = "Trajets disponibles",
    noRidesAvailable = "Aucun trajet disponible",
    checkBackLater = "Revenez plus tard ou ajustez votre recherche",
    postARide = "Publier un trajet",
    from = "De",
    to = "À",

    // Carte de trajet
    seatsLeft = "places restantes",
    xafSuffix = "XAF",

    // Détails du trajet
    rideDetails = "Détails du trajet",
    date = "Date",
    time = "Heure",
    availableSeats = "Places disponibles",
    ofSeats = "sur",
    vehicle = "Véhicule",
    plate = "Plaque",
    driverLabel = "Chauffeur",
    phone = "Téléphone",
    notes = "Notes",
    bookSeat = "Réserver une place",
    fullyBooked = "Complet",
    perSeat = "/ place",

    // Créer un trajet
    postRide = "Publier le trajet",
    route = "Itinéraire",
    departureCity = "Ville de départ",
    arrivalCity = "Ville d'arrivée",
    schedule = "Horaire",
    dateFormat = "Date (AAAA-MM-JJ)",
    timeFormat = "Heure (HH:MM)",
    details = "Détails",
    seats = "Places",
    priceXaf = "Prix (XAF)",
    vehicleName = "Nom du véhicule (ex. Toyota)",
    vehicleModel = "Modèle (ex. Corolla 2020)",
    saveVehicle = "Enregistrer ce véhicule pour les trajets futurs",
    selectSavedVehicle = "Sélectionner un véhicule enregistré",
    plateNumber = "Numéro de plaque",
    additionalNotes = "Notes supplémentaires (facultatif)",

    // Réservation
    bookYourRide = "Réserver votre trajet",
    numberOfSeats = "Nombre de places",
    paymentMethod = "Mode de paiement",
    mtnMobileMoney = "MTN Mobile Money",
    orangeMoney = "Orange Money",
    cardPayment = "Paiement par carte",
    mobileMoneyNumber = "Numéro Mobile Money",
    phoneHint = "Numéro de téléphone (ex. 237670000000)",
    cardDetails = "Détails de la carte",
    cardNumber = "Numéro de carte",
    removeSeat = "Retirer une place",
    addSeat = "Ajouter une place",
    seatsTimesPrice = "place(s) ×",
    payAmount = "Payer",
    waitingForPayment = "En attente du paiement…",
    approveMtnMomo = "Approuvez le paiement sur votre application MTN MoMo",
    approveOrangeMoney = "Approuvez le paiement sur votre application Orange Money",
    processingCard = "Traitement de votre paiement par carte…",
    checkPhoneNotification = "Vérifiez la notification sur votre téléphone",

    // Confirmation de paiement
    bookingConfirmed = "Réservation confirmée !",
    paymentEscrowInfo = "Votre paiement a été reçu et est conservé en séquestre de manière sécurisée.",
    summaryRoute = "Itinéraire",
    summarySeats = "Places",
    summaryTotalPaid = "Total payé",
    summaryPaymentMethod = "Mode de paiement",
    summaryStatus = "Statut",
    heldInEscrow = "Conservé en séquestre ✓",
    reference = "Référence",
    escrowExplanation = "🔒 Votre paiement est conservé en sécurité. Le chauffeur reçoit 90 % après la fin du trajet. Les 10 % restants constituent les frais de service de la plateforme.",

    // Profil
    profile = "Profil",
    driverBadge = "🚗 Chauffeur",
    passengerBadge = "🧳 Passager",
    drivingPermit = "Permis de conduire",
    greyCard = "Carte grise",
    signOut = "Se déconnecter",
    language = "Langue",

    // Tableau de bord chauffeur
    myTrips = "Mes trajets",
    payouts = "Versements",
    noTripsYet = "Aucun trajet pour le moment",
    createFirstTrip = "Créez votre premier trajet pour commencer à gagner",
    newTrip = "Nouveau trajet",
    startTrip = "Démarrer le trajet",
    completeTrip = "Terminer le trajet",
    completeAndRelease = "Terminer le trajet et libérer les paiements",
    startTripQuestion = "Démarrer le trajet ?",
    startTripConfirmText = "Les passagers seront informés que le trajet est en cours.",
    completeTripQuestion = "Terminer le trajet ?",
    completeTripConfirmText = "Les paiements en séquestre (90 %) seront versés sur votre compte.",
    cancelTripQuestion = "Annuler le trajet ?",
    cancelTripConfirmText = "Tous les paiements retenus seront remboursés aux passagers.",

    // Statuts de trajet
    statusScheduled = "Planifié",
    statusInProgress = "En cours",
    statusCompleted = "Terminé",
    statusCancelled = "Annulé",

    // Historique des versements
    earningsAndPayouts = "Gains et versements",
    totalEarned = "Total gagné",
    pending = "En attente",
    completedTrips = "Trajets terminés",
    payoutHistory = "Historique des versements",
    noPayoutsYet = "Aucun versement pour le moment. Terminez des trajets pour gagner !",
    passengerFallback = "Passager",
    ofAmount = "sur",

    // Chat
    chat = "Discussion",
    startConversation = "Commencez la conversation ! 💬",
    typeMessage = "Tapez un message…",
    send = "Envoyer",

    // Demandes de trajet
    tabRequests = "Demandes",
    rideRequests = "Demandes de trajet",
    noRequestsYet = "Aucune demande pour le moment",
    postRequestHint = "Publiez une demande si vous cherchez un trajet",
    postARequest = "Demander un trajet",
    createRideRequest = "Nouvelle demande",
    destination = "Destination",
    travelDate = "Date de voyage",
    seatsNeeded = "Places requises",
    yourMessage = "Votre message (facultatif)",
    submitRequest = "Publier la demande",
    rideRequestTag = "🙋 Demande de trajet",
    contactPassenger = "Contacter le passager",
    closeRequest = "Fermer la demande",
    requestClosed = "Cela retirera votre demande du flux.",
    anyDriverAvailable = "Un chauffeur disponible ?",
    requestCreated = "Demande publiée !",
)

/**
 * Resolve strings for a given language.
 */
fun stringsFor(language: Language): Strings = when (language) {
    Language.EN -> stringsEn()
    Language.FR -> stringsFr()
}
