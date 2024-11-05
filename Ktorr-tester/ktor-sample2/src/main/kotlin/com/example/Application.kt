package com.example

import client.importRecordToRedCap
import com.example.plugins.*
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.response.*
import io.ktor.server.routing.*


fun main() {
    embeddedServer(Netty, port = 8080, host = "0.0.0.0", module = Application::module)
        .start(wait = true)
}

fun Application.module() {
    configureSerialization()
    configureMonitoring()
    configureHTTP()
    configureRouting()

}
fun Application.configureRouting() {
    routing {
        get("/hello") {
            call.respondText("Hello from GET!")
        }

        post("/importRecord") {
            // Get RedCap API Token
            val redcapAPItoken = System.getenv("REDCAP_API_TOKEN")
            if (redcapAPItoken == null) {
                log.error("API token not found in environment variables!")
            } else {
                log.info("API token loaded successfully")
            }

            // Set up your RedCap API details here
            val apiToken = redcapAPItoken
            val projectUrl = "https://hci-redcap.hci.utah.edu/redcap/api/"

            // Create a list of users (this can be dynamic or from input)
            // Create a list of users with the new fields (phone_number and personal_email)
            // Create a list of users with the new fields (including end_date)
            val trainees = listOf(
                Trainee(
                    record_id = "9",
                    first_name = "John",
                    last_name = "Doe",
                    phone_number = "555-1234",
                    personal_email = "john.doe@example.com",
                    end_date = "2023-10-22"  // Example end date
                ),
                Trainee(
                    record_id = "10",
                    first_name = "Jane",
                    last_name = "Smith",
                    phone_number = "555-5678",
                    personal_email = "jane.smith@example.com",
                    end_date = "2023-09-15"  // Example end date
                )
            )

            log.info("About to import records to redcap")
            // Call the function to import the record to RedCap
            val redCapResponse = importRecordToRedCap(apiToken, projectUrl, trainees)

            // Respond with the result
            call.respondText("RedCap API Response: $redCapResponse")
        }
    }
}


