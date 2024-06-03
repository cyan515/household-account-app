plugins {
    kotlin("jvm") version "1.9.23"
    application
}

val group = "cyan0515.household-account-app"
val version = "1.0-SNAPSHOT"
val ktorVersion = "2.3.11"
val exposedVersion = "0.41.1"
val authVersion = "2.2.4"

repositories {
    mavenCentral()
}

dependencies {
    implementation("io.ktor:ktor-server-netty:$ktorVersion")
    implementation("io.ktor:ktor-server-core:$ktorVersion")
    implementation("io.ktor:ktor-server-content-negotiation:$ktorVersion")
    implementation("io.ktor:ktor-serialization-jackson:$ktorVersion")
    implementation("org.jetbrains.exposed:exposed-core:$exposedVersion")
    implementation("org.jetbrains.exposed:exposed-dao:$exposedVersion")
    implementation("org.jetbrains.exposed:exposed-jdbc:$exposedVersion")
    implementation("org.jetbrains.exposed:exposed-java-time:$exposedVersion")
    implementation("org.postgresql:postgresql:42.7.3")
    implementation("io.ktor:ktor-server-auth-jwt:$authVersion")
    implementation("io.ktor:ktor-server-auth:$authVersion")
    implementation("org.mindrot:jbcrypt:0.4")
    implementation("ch.qos.logback:logback-classic:1.5.6")
    testImplementation(kotlin("test"))
}

application {
    mainClass.set("cyan0515.household.ApplicationKt")
}

tasks.test {
    useJUnitPlatform()
}