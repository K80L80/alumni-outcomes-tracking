package client

import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.*
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
    install(HttpTimeout) {
        requestTimeoutMillis = 60000
    }
}


// Function to import records to RedCap with additional flags
suspend fun importRecordToRedCap(apiToken: String, projectUrl: String, recordData: String): String {
    // Making the POST request to RedCap API
    val response: HttpResponse = client.post(projectUrl) {
        parameter("token", apiToken)                  // API token for authentication
        parameter("content", "record")                // Specify the content type (record)
        parameter("action", "import")                 // The action to import data
        parameter("format", "json")                   // Specify JSON format
        parameter("type", "flat")                     // 'flat' format for the data
        parameter("overwriteBehavior", "normal")      // Normal overwrite behavior (ignores blank/empty fields)
        parameter("forceAutoNumber", "false")         // Do not force auto-numbering for records
        parameter("data", recordData)                 // The actual record data
        parameter("returnContent", "count")           // Return the count of imported records
        parameter("returnFormat", "json")             // Return the response in JSON format
    }
    return response.bodyAsText()                      // Return the response from RedCap as a string
}