package com.example

import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.response.*
import io.ktor.server.request.*
import io.ktor.server.routing.*
import com.example.Users
import io.ktor.http.*
import kotlinx.serialization.Serializable
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.SchemaUtils.create
import org.jetbrains.exposed.sql.SchemaUtils.drop
import org.mindrot.jbcrypt.BCrypt

fun main() {
    embeddedServer(Netty, port = 8080, module = Application::module).start(wait = true)
}

fun Application.module() {
    DatabaseFactory.init()

    install(ContentNegotiation) { json() }

    // --- DB init ---
    val config = environment.config
    Database.connect(
        url = config.property("db.url").getString(),
        driver = config.property("db.driver").getString(),
        user = config.property("db.user").getString(),
        password = config.property("db.password").getString()
    )

    transaction { create(Users) }

    // --- JWT setup ---
    val jwtSecret = config.property("jwt.secret").getString()
    val jwtIssuer = config.property("jwt.issuer").getString()
    val jwtAudience = config.property("jwt.audience").getString()

    install(Authentication) {
        jwt {
            realm = config.property("jwt.realm").getString()
            verifier(JwtConfig.makeVerifier(jwtSecret, jwtIssuer, jwtAudience))
            validate { credential ->
                if (credential.payload.getClaim("username").asString().isNotEmpty()) JWTPrincipal(credential.payload)
                else null
            }
        }
    }

    routing {
        get("/") { call.respond(mapOf("message" to "Ktor server with DB & JWT is running!")) }

        // === AUTH ===
        post("/register") {
            val request = call.receive<UserRequest>()
            val hashed = BCrypt.hashpw(request.password, BCrypt.gensalt())
            transaction {
                UserEntity.new {
                    username = request.username
                    passwordHash = hashed
                }
            }
            call.respond(HttpStatusCode.Created, mapOf("message" to "User registered"))
        }

        post("/login") {
            val request = call.receive<UserRequest>()
            val user = transaction { UserEntity.find { Users.username eq request.username }.singleOrNull() }
            if (user == null || !BCrypt.checkpw(request.password, user.passwordHash)) {
                call.respond(HttpStatusCode.Unauthorized, mapOf("error" to "Invalid credentials"))
                return@post
            }
            val token = JwtConfig.generateToken(jwtSecret, jwtIssuer, jwtAudience, user.username)
            call.respond(mapOf("token" to token))
        }

        authenticate {
            // === CRUD (protected) ===
            get("/users") {
                val users = transaction { UserEntity.all().map { it.toResponse() } }
                call.respond(users)
            }

            delete("/users/{id}") {
                val id = call.parameters["id"]?.toIntOrNull()
                if (id == null) {
                    call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Invalid ID"))
                    return@delete
                }
                val deleted = transaction { UserEntity.findById(id)?.delete() != null }
                if (deleted) call.respond(mapOf("message" to "User deleted"))
                else call.respond(HttpStatusCode.NotFound, mapOf("error" to "User not found"))
            }
        }
    }
}

class UserEntity(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<UserEntity>(Users)

    var username by Users.username
    var passwordHash by Users.passwordHash

    fun toResponse() = UserResponse(id.value, username)
}

// === DTOs ===
@Serializable data class UserRequest(val username: String, val password: String)
@Serializable data class UserResponse(val id: Int, val username: String)
