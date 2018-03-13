package frc.team2186.recordingserver

import com.google.gson.Gson
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import io.ktor.application.call
import io.ktor.application.install
import io.ktor.features.ContentNegotiation
import io.ktor.gson.gson
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.request.receive
import io.ktor.response.contentType
import io.ktor.response.respond
import io.ktor.routing.get
import io.ktor.routing.post
import io.ktor.routing.routing
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import java.io.File
import java.text.DateFormat

data class TimeData(val timestamp: Double, val leftRPM: Double, val rightRPM: Double)
data class RecordingData(val name: String, val reversed: Boolean, val elements: ArrayList<TimeData>)

fun main(args: Array<String>) {
    val gson = Gson()
    val server = embeddedServer(Netty, 5802) {
        install(ContentNegotiation) {
            gson {
                setDateFormat(DateFormat.LONG)
                setPrettyPrinting()
            }
        }
        routing {
            post("/data") {
                try {
                    val jsonData = call.receive<RecordingData>()
                    val file = File("recording-${jsonData.name}.json")
                    val jsonString = gson.toJson(jsonData, RecordingData::class.java)
                    file.bufferedWriter().use {
                        it.write(jsonString)
                        it.newLine()
                    }
                    call.respond(HttpStatusCode.Created, jsonData.name)
                } catch (e: Exception) {
                    e.printStackTrace()
                    call.respond(HttpStatusCode.InternalServerError, e.message?:"")
                }
            }
            get("/data") {
                try {
                    val params = call.parameters
                    val name = params["name"]
                    val file = File("recording-$name.json")
                    if (file.exists() and file.canRead()) {
                        val contents = file.bufferedReader().readText()
                        call.respond(HttpStatusCode.OK, contents)
                    } else {
                        val response = JsonObject().apply {
                            addProperty("error", "File not found")
                        }
                        call.respond(HttpStatusCode.NotFound, response)
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    call.respond(HttpStatusCode.InternalServerError, e.message?:"")
                }
            }
            get("/data/all") {
                try {
                    val directory = File(".")
                    val filenames = directory.listFiles().filter { it.isFile and it.nameWithoutExtension.contains("recording-", ignoreCase = true) }.map { it.name }
                    val recordings = filenames.map { it.split(Regex.fromLiteral("-"))[1] }
                    val retArray = JsonArray().apply {
                        recordings.forEach {
                            add(it.split(Regex.fromLiteral("."))[0])
                        }
                    }
                    call.respond(HttpStatusCode.OK, retArray)
                } catch (e: Exception) {
                    e.printStackTrace()
                    call.respond(HttpStatusCode.InternalServerError, e.message?:"")
                }
            }
        }
    }
    server.start()
}