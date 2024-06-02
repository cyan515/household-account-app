plugins {
    kotlin("jvm") version "1.9.23"
    application
}

val group = "cyan0515.household-account-app"
val version = "1.0-SNAPSHOT"
val authVersion = "2.2.4"

repositories {
    mavenCentral()
}

dependencies {
    implementation("io.ktor:ktor-server-netty:2.3.0")
    implementation("io.ktor:ktor-server-core:2.3.0")
    implementation("io.ktor:ktor-server-content-negotiation:2.3.0")
    implementation("io.ktor:ktor-serialization-jackson:2.3.0")
    implementation("org.jetbrains.exposed:exposed-core:0.41.1")
    implementation("org.jetbrains.exposed:exposed-dao:0.41.1")
    implementation("org.jetbrains.exposed:exposed-jdbc:0.41.1")
    implementation("org.postgresql:postgresql:42.6.0")
    implementation("io.ktor:ktor-server-auth-jwt:$authVersion")
    implementation("io.ktor:ktor-server-auth:$authVersion")
    implementation("org.mindrot:jbcrypt:0.4")
    testImplementation(kotlin("test"))
}

application {
    mainClass.set("cyan0515.household.ApplicationKt")
}

tasks.test {
    useJUnitPlatform()
}