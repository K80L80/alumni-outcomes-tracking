package com.example.plugins


import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.plugins.contentnegotiation.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

fun Application.configureSerialization() {
    install(ContentNegotiation) {
        json(Json {
            prettyPrint = true  // Optional: makes the JSON response more readable
            isLenient = true    // Optional: allows for more lenient JSON parsing
            ignoreUnknownKeys = true  // Optional: allows ignoring unknown fields in JSON
        })
    }
}
@Serializable
data class Trainee(
    val record_id: String,
    val first_name: String,
    val last_name: String
)
