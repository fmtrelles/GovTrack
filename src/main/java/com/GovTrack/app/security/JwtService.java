package com.GovTrack.app.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.time.Instant;
import java.util.Date;

@Service
public class JwtService {

    private final SecretKey clave;
    private final long duracionSegundos;

    public JwtService(
            @Value("${jwt.secret}") String secreto,
            @Value("${jwt.expiration-seconds}") long duracionSegundos) {

        this.clave = Keys.hmacShaKeyFor(Decoders.BASE64.decode(secreto));

        if (duracionSegundos <= 0) {
            throw new IllegalArgumentException(
                    "La duración del token debe ser mayor que cero");
        }

        this.duracionSegundos = duracionSegundos;
    }

    public String generarToken(UserDetails usuario) {
        Instant ahora = Instant.now();

        return Jwts.builder()
                .issuer("govtrack")
                .subject(usuario.getUsername())
                .issuedAt(Date.from(ahora))
                .expiration(Date.from(ahora.plusSeconds(duracionSegundos)))
                .signWith(clave, Jwts.SIG.HS256)
                .compact();
    }

    public String extraerCorreo(String token) {
        return extraerClaims(token).getSubject();
    }

    public boolean esTokenValido(String token, UserDetails usuario) {
        try {
            Claims claims = extraerClaims(token);

            return usuario.getUsername().equals(claims.getSubject())
                    && claims.getExpiration() != null
                    && claims.getExpiration().after(new Date())
                    && usuario.isEnabled()
                    && usuario.isAccountNonLocked()
                    && usuario.isAccountNonExpired()
                    && usuario.isCredentialsNonExpired();

        } catch (JwtException | IllegalArgumentException exception) {
            return false;
        }
    }

    private Claims extraerClaims(String token) {
        return Jwts.parser()
                .verifyWith(clave)
                .requireIssuer("govtrack")
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}