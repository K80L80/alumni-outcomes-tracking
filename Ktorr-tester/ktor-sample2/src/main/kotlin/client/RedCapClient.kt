package client

import com.example.plugins.Trainee
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import javax.net.ssl.X509TrustManager

// Create a Ktor HTTP client with JSON serialization support
val client = HttpClient(CIO) {
    install(ContentNegotiation) {
        json(Json {
            prettyPrint = true
            isLenient = true
            ignoreUnknownKeys = true
        })
    }
    //TODO: before for production, resolve the certificate issue
    //This tells the Ktor client to bypass SSL validation for the specified host
    engine {
        https {
            trustManager = object : X509TrustManager {
                override fun checkClientTrusted(chain: Array<out java.security.cert.X509Certificate>?, authType: String?) {}
                override fun checkServerTrusted(chain: Array<out java.security.cert.X509Certificate>?, authType: String?) {}
                override fun getAcceptedIssuers(): Array<java.security.cert.X509Certificate> = arrayOf()
            }
        }
    }

    install(HttpTimeout) {
        requestTimeoutMillis = 60000
    }

}


// Function to import records to RedCap with additional flags
suspend fun importRecordToRedCap(apiToken: String, projectUrl: String, trainees: List<Trainee>): String {
    println("Starting RedCap import process...")

    // Serialize the list of users to a JSON array
    val recordData = Json.encodeToString(trainees)

    // Debug: Log the API token and record data (Be cautious in production not to log sensitive information)
    println("API Token: $apiToken")
    println("Record Data: $trainees")

    // Create a form-encoded payload (just like your cURL request)
    val formParameters = listOf(
        "token" to apiToken,                   // API token for authentication
        "content" to "record",                 // Specify the content type (record)
        "action" to "import",                  // The action to import data
        "format" to "json",                    // Specify JSON format
        "type" to "flat",                      // 'flat' format for the data
        "overwriteBehavior" to "normal",       // Normal overwrite behavior (ignores blank/empty fields)
        "forceAutoNumber" to "false",          // Do not force auto-numbering for records
        "data" to recordData,                  // The actual record data
        "returnContent" to "count",            // Return the count of imported records
        "returnFormat" to "json"               // Return the response in JSON format
    ).formUrlEncode()

    // Making the POST request to RedCap API
    val response: HttpResponse = client.post(projectUrl) {
        headers {
            append(HttpHeaders.ContentType, "application/x-www-form-urlencoded")
            append(HttpHeaders.Accept, "application/json")
        }
        setBody(formParameters)  // Set the body as URL-encoded form data
    }

    // Debug: Log the full URL and response
    println("RedCap Request URL: ${response.request.url}")
    println("RedCap Response Status: ${response.status}")
    println("RedCap Response Body: ${response.bodyAsText()}")

    client.close()  // Close the client to free up resources

    return response.bodyAsText()                      // Return the response from RedCap as a string
}