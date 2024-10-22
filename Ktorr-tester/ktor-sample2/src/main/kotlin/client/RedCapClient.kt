package client

import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json

// Create a Ktor HTTP client with JSON serialization support
val client = HttpClient(CIO) {
    install(ContentNegotiation) {
        json(Json {
            prettyPrint = true
            isLenient = true
            ignoreUnknownKeys = true
        })
    }
}

// Function to send a record to RedCap API
suspend fun importRecordToRedCap(apiToken: String, projectUrl: String, recordData: String): String {
    val response: HttpResponse = client.post(projectUrl) {
        parameter("token", apiToken)
        parameter("content", "record")
        parameter("format", "json")
        parameter("type", "flat")
        parameter("data", recordData)
    }
    return response.bodyAsText()
}