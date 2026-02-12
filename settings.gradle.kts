rootProject.name = "CoVoyage"

include(":shared")
include(":androidApp")
include(":backend")

pluginManagement {
    repositories {
        // High-availability mirrors FIRST to bypass DNS failures
        maven("https://maven.aliyun.com/repository/google")
        maven("https://maven.aliyun.com/repository/public")
        maven("https://maven-central.storage-download.googleapis.com/maven2/")
        maven("https://repo1.maven.org/maven2/")
        
        google()
        mavenCentral()
        gradlePluginPortal()
        maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        // High-availability mirrors FIRST to bypass DNS failures
        maven("https://maven.aliyun.com/repository/google")
        maven("https://maven.aliyun.com/repository/public")
        maven("https://maven-central.storage-download.googleapis.com/maven2/")
        maven("https://repo1.maven.org/maven2/")
        
        google()
        mavenCentral()
        maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
    }
}
