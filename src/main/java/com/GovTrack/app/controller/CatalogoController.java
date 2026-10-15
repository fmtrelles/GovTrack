package com.GovTrack.app.controller;

import com.GovTrack.app.model.Usuario;
import com.GovTrack.app.repository.EstadoRequerimientoRepository;
import com.GovTrack.app.repository.PrioridadRepository;
import com.GovTrack.app.repository.TipoRequerimientoRepository;
import com.GovTrack.app.repository.UsuarioRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.server.ResponseStatusException;

import java.util.Comparator;
import java.util.List;

@Controller
@ResponseBody
@RequestMapping("/api/catalogos")
public class CatalogoController {

    private final TipoRequerimientoRepository tipoRepository;
    private final PrioridadRepository prioridadRepository;
    private final EstadoRequerimientoRepository estadoRepository;
    private final UsuarioRepository usuarioRepository;

    public CatalogoController(
            TipoRequerimientoRepository tipoRepository,
            PrioridadRepository prioridadRepository,
            EstadoRequerimientoRepository estadoRepository,
            UsuarioRepository usuarioRepository) {

        this.tipoRepository = tipoRepository;
        this.prioridadRepository = prioridadRepository;
        this.estadoRepository = estadoRepository;
        this.usuarioRepository = usuarioRepository;
    }

    @GetMapping
    @Transactional(readOnly = true)
    public CatalogosRespuesta obtener(Authentication autenticacion) {
        Usuario usuario = usuarioRepository
                .findByCorreoIgnoreCase(autenticacion.getName())
                .filter(item -> Boolean.TRUE.equals(item.getActivo()))
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.UNAUTHORIZED,
                        "El usuario no está habilitado"
                ));

        List<Opcion> tipos = tipoRepository.findAll()
                .stream()
                .filter(item -> Boolean.TRUE.equals(item.getActivo()))
                .map(item -> new Opcion(item.getId(), item.getNombre()))
                .sorted(Comparator.comparing(Opcion::nombre))
                .toList();

        List<Opcion> prioridades = prioridadRepository.findAll()
                .stream()
                .filter(item -> Boolean.TRUE.equals(item.getActivo()))
                .map(item -> new Opcion(item.getId(), item.getNombre()))
                .sorted(Comparator.comparing(Opcion::id))
                .toList();

        List<Opcion> estados = estadoRepository.findAll()
                .stream()
                .map(item -> new Opcion(item.getId(), item.getNombre()))
                .sorted(Comparator.comparing(Opcion::id))
                .toList();

        List<String> roles = autenticacion.getAuthorities()
                .stream()
                .map(item -> item.getAuthority())
                .sorted()
                .toList();

        return new CatalogosRespuesta(
                usuario.getId(),
                usuario.getNombres() + " " + usuario.getApellidos(),
                roles,
                tipos,
                prioridades,
                estados
        );
    }

    public record Opcion(Long id, String nombre) {
    }

    public record CatalogosRespuesta(
            Long usuarioId,
            String nombre,
            List<String> roles,
            List<Opcion> tipos,
            List<Opcion> prioridades,
            List<Opcion> estados) {
    }
}