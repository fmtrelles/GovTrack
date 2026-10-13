package com.GovTrack.app.security;

import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UsuarioDetailsService usuarioDetailsService;

    public JwtAuthenticationFilter(
            JwtService jwtService,
            UsuarioDetailsService usuarioDetailsService) {
        this.jwtService = jwtService;
        this.usuarioDetailsService = usuarioDetailsService;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String ruta = request.getServletPath();

        return "/api/auth/login".equals(ruta)
                || "/api/auth/register".equals(ruta);
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {

        String encabezado = request.getHeader(HttpHeaders.AUTHORIZATION);

        if (encabezado == null) {
            filterChain.doFilter(request, response);
            return;
        }

        if (!encabezado.regionMatches(true, 0, "Bearer ", 0, 7)) {
            responderNoAutorizado(response);
            return;
        }

        String token = encabezado.substring(7).trim();

        try {
            String correo = jwtService.extraerCorreo(token);

            UserDetails usuario = usuarioDetailsService
                    .loadUserByUsername(correo);

            if (!jwtService.esTokenValido(token, usuario)) {
                responderNoAutorizado(response);
                return;
            }

            UsernamePasswordAuthenticationToken autenticacion =
                    new UsernamePasswordAuthenticationToken(
                            usuario,
                            null,
                            usuario.getAuthorities()
                    );

            autenticacion.setDetails(
                    new WebAuthenticationDetailsSource()
                            .buildDetails(request)
            );

            SecurityContext contexto =
                    SecurityContextHolder.createEmptyContext();

            contexto.setAuthentication(autenticacion);
            SecurityContextHolder.setContext(contexto);

        } catch (JwtException | AuthenticationException
                 | IllegalArgumentException exception) {
            responderNoAutorizado(response);
            return;
        }

        filterChain.doFilter(request, response);
    }

    private void responderNoAutorizado(
            HttpServletResponse response) throws IOException {

        SecurityContextHolder.clearContext();

        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setHeader(HttpHeaders.WWW_AUTHENTICATE, "Bearer");
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());

        response.getWriter().write(
                "{\"status\":401,\"mensaje\":\"Token inválido o usuario no autorizado\"}"
        );
    }
}