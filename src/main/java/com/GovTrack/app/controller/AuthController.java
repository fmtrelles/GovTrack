package com.GovTrack.app.controller;

import com.GovTrack.app.dto.AuthRespuestaDTO;
import com.GovTrack.app.dto.LoginDTO;
import com.GovTrack.app.dto.UsuarioRegistroDTO;
import com.GovTrack.app.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public ResponseEntity<AuthRespuestaDTO> registrar(
            @Valid @RequestBody UsuarioRegistroDTO dto) {

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(authService.registrar(dto));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthRespuestaDTO> iniciarSesion(
            @Valid @RequestBody LoginDTO dto) {

        return ResponseEntity.ok(authService.iniciarSesion(dto));
    }
}