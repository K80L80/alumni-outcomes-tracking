package com.example

import client.importRecordToRedCap
import com.example.plugins.*
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.request.*
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
            val recordData = """[{"record_id": "4", "first_name": "John", "last_name": "Doe"}, {"record_id": "5", "first_name": "Jane", "last_name": "Smith"}]"""
            log.info("About to import records to redcap")
            // Call the function to import the record to RedCap
            val redCapResponse = importRecordToRedCap(apiToken, projectUrl, recordData)

            // Respond with the result
            call.respondText("RedCap API Response: $redCapResponse")
        }
    }
}

//    routing {
//        post("/importRedCap") {
//

//        }
//    }
//}

