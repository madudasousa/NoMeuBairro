package com.meubairro.api.service;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.meubairro.api.domain.User.User;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;


@Service
public class JwtService {

    @Value("${jwt.secret}")
    private String secret;

    public String gerarToken(User user) {
        return JWT.create()
                .withSubject(user.getDocument())
                .withClaim("id", user.getId().toString())
                .withClaim("name", user.getName())
                .withClaim("perfil", user.getPerfil().name())
                .withIssuedAt(Date.from(Instant.now()))
                .withExpiresAt(Date.from(Instant.now().plus(24, ChronoUnit.HOURS)))
                .sign(Algorithm.HMAC256(secret));
    }

    public String validarToken(String token) {
        try {
            return JWT.require(Algorithm.HMAC256(secret))
                    .build()
                    .verify(token)
                    .getSubject();
        } catch (JWTVerificationException e) {
            // Token inválido ou expirado — retorna vazio para bloquear o acesso
            return "";
        }
    }
}
