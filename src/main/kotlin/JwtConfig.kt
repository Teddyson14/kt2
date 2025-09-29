package com.example

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.auth0.jwt.JWTVerifier

object JwtConfig {
    fun makeVerifier(secret: String, issuer: String, audience: String): JWTVerifier =
        JWT.require(Algorithm.HMAC256(secret))
            .withIssuer(issuer)
            .withAudience(audience)
            .build()

    fun generateToken(secret: String, issuer: String, audience: String, username: String): String =
        JWT.create()
            .withAudience(audience)
            .withIssuer(issuer)
            .withClaim("username", username)
            .sign(Algorithm.HMAC256(secret))
}
