package com.GovTrack.app.controller;

import com.GovTrack.app.dto.RequerimientoRegistroDTO;
import com.GovTrack.app.dto.RequerimientoRespuestaDTO;
import com.GovTrack.app.service.RequerimientoService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/requerimientos")
public class RequerimientoController {

    private final RequerimientoService requerimientoService;

    public RequerimientoController(
            RequerimientoService requerimientoService) {
        this.requerimientoService = requerimientoService;
    }

    @PostMapping
    public ResponseEntity<RequerimientoRespuestaDTO> registrar(
            @Valid @RequestBody RequerimientoRegistroDTO dto,
            Authentication autenticacion) {

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(requerimientoService.registrar(dto, autenticacion));
    }

    @GetMapping
    public ResponseEntity<List<RequerimientoRespuestaDTO>> listar(
            Authentication autenticacion,
            @RequestParam(required = false) Long estadoId) {

        return ResponseEntity.ok(
                requerimientoService.listar(autenticacion, estadoId)
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<RequerimientoRespuestaDTO> buscarPorId(
            @PathVariable Long id,
            Authentication autenticacion) {

        return ResponseEntity.ok(
                requerimientoService.buscarPorId(id, autenticacion)
        );
    }
}