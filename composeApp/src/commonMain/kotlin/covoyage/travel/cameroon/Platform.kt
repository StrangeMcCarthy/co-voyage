package covoyage.travel.cameroon

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform