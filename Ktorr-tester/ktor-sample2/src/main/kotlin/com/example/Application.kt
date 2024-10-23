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
    val apiToken = System.getenv("REDCAP_API_TOKEN")
}

fun Application.module() {
    configureSerialization()
    configureMonitoring()
    configureHTTP()
    configureRouting()

    // Add your routing for RedCap API testing at the bottom
    val redcapAPItoken = System.getenv("REDCAP_API_TOKEN")
    if (redcapAPItoken == null) {
        log.error("API token not found in environment variables!")
    } else {
        log.info("API token loaded successfully")
    }

    routing {
        get("/importRedCap") {
            // Set up your RedCap API details here
            val apiToken = redcapAPItoken
            val projectUrl = "https://hci-redcap.hci.utah.edu/redcap/api/"
            val recordData = """
            [
                {
                    "record_id": "1",           // Record ID
                    "first_name": "John",     // Form field: first_name
                    "last_name": "Doe",       // Form field: last_name 
                }
            ]
            """

            // Call the function to import the record to RedCap
            val result = importRecordToRedCap(apiToken, projectUrl, recordData)

            // Respond with the result
            call.respondText("RedCap API Response: $result")
        }
    }
}

