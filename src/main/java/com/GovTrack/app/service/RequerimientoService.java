package com.GovTrack.app.service;

import com.GovTrack.app.dto.RequerimientoRegistroDTO;
import com.GovTrack.app.dto.RequerimientoRespuestaDTO;
import com.GovTrack.app.exception.RecursoNoEncontradoException;
import com.GovTrack.app.model.Area;
import com.GovTrack.app.model.EstadoRequerimiento;
import com.GovTrack.app.model.Prioridad;
import com.GovTrack.app.model.Requerimiento;
import com.GovTrack.app.model.TipoRequerimiento;
import com.GovTrack.app.model.Usuario;
import com.GovTrack.app.repository.EstadoRequerimientoRepository;
import com.GovTrack.app.repository.PrioridadRepository;
import com.GovTrack.app.repository.RequerimientoRepository;
import com.GovTrack.app.repository.TipoRequerimientoRepository;
import com.GovTrack.app.repository.UsuarioRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.Year;
import java.util.List;
import java.util.Objects;

@Service
public class RequerimientoService {

    private final RequerimientoRepository requerimientoRepository;
    private final TipoRequerimientoRepository tipoRepository;
    private final PrioridadRepository prioridadRepository;
    private final EstadoRequerimientoRepository estadoRepository;
    private final UsuarioRepository usuarioRepository;

    public RequerimientoService(
            RequerimientoRepository requerimientoRepository,
            TipoRequerimientoRepository tipoRepository,
            PrioridadRepository prioridadRepository,
            EstadoRequerimientoRepository estadoRepository,
            UsuarioRepository usuarioRepository) {

        this.requerimientoRepository = requerimientoRepository;
        this.tipoRepository = tipoRepository;
        this.prioridadRepository = prioridadRepository;
        this.estadoRepository = estadoRepository;
        this.usuarioRepository = usuarioRepository;
    }

    @PreAuthorize("hasAnyRole('SOLICITANTE', 'ADMINISTRADOR')")
    @Transactional
    public RequerimientoRespuestaDTO registrar(
            RequerimientoRegistroDTO dto,
            Authentication autenticacion) {

        Usuario solicitante = obtenerUsuario(autenticacion);
        Area area = solicitante.getArea();

        if (area == null || !Boolean.TRUE.equals(area.getActivo())) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "El usuario debe pertenecer a un área activa"
            );
        }

        TipoRequerimiento tipo = tipoRepository
                .findById(dto.getTipoRequerimientoId())
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "El tipo de requerimiento no existe"
                ));

        if (!Boolean.TRUE.equals(tipo.getActivo())) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "El tipo de requerimiento está inactivo"
            );
        }

        Prioridad prioridad = prioridadRepository
                .findById(dto.getPrioridadId())
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "La prioridad no existe"
                ));

        if (!Boolean.TRUE.equals(prioridad.getActivo())) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "La prioridad está inactiva"
            );
        }

        EstadoRequerimiento estadoInicial = estadoRepository
                .findByNombreIgnoreCase("REGISTRADO")
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "No se encontró el estado inicial REGISTRADO"
                ));

        if (!Boolean.TRUE.equals(estadoInicial.getActivo())) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "El estado inicial REGISTRADO está inactivo"
            );
        }

        Requerimiento requerimiento = Requerimiento.builder()
                .titulo(dto.getTitulo().trim())
                .descripcion(dto.getDescripcion().trim())
                .tipo(tipo)
                .prioridad(prioridad)
                .estado(estadoInicial)
                .areaSolicitante(area)
                .solicitante(solicitante)
                .build();

        Requerimiento guardado =
                requerimientoRepository.save(requerimiento);

        guardado.setCodigo(generarCodigo(guardado.getId()));

        guardado = requerimientoRepository.save(guardado);

        return convertirARespuesta(guardado);
    }

    @PreAuthorize("""
            hasAnyRole(
                'SOLICITANTE', 'RESPONSABLE',
                'SUPERVISOR', 'ADMINISTRADOR'
            )
            """)
    @Transactional(readOnly = true)
    public List<RequerimientoRespuestaDTO> listar(
            Authentication autenticacion,
            Long estadoId) {

        Usuario usuario = obtenerUsuario(autenticacion);

        if (estadoId != null && estadoId <= 0) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "El identificador del estado debe ser positivo"
            );
        }

        return requerimientoRepository.buscarVisibles(
                        usuario.getId(),
                        puedeVerTodos(autenticacion),
                        tieneRol(autenticacion, "SOLICITANTE"),
                        tieneRol(autenticacion, "RESPONSABLE"),
                        estadoId
                )
                .stream()
                .map(this::convertirARespuesta)
                .toList();
    }

    @PreAuthorize("""
            hasAnyRole(
                'SOLICITANTE', 'RESPONSABLE',
                'SUPERVISOR', 'ADMINISTRADOR'
            )
            """)
    @Transactional(readOnly = true)
    public RequerimientoRespuestaDTO buscarPorId(
            Long id,
            Authentication autenticacion) {

        Usuario usuario = obtenerUsuario(autenticacion);

        Requerimiento requerimiento = requerimientoRepository
                .findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "No existe un requerimiento con ID " + id
                ));

        boolean esPropio =
                tieneRol(autenticacion, "SOLICITANTE")
                        && Objects.equals(
                        requerimiento.getSolicitante().getId(),
                        usuario.getId()
                );

        boolean estaAsignado =
                tieneRol(autenticacion, "RESPONSABLE")
                        && requerimiento.getResponsable() != null
                        && Objects.equals(
                        requerimiento.getResponsable().getId(),
                        usuario.getId()
                );

        if (!puedeVerTodos(autenticacion) && !esPropio && !estaAsignado) {
            throw new AccessDeniedException(
                    "No tienes permiso para consultar este requerimiento"
            );
        }

        return convertirARespuesta(requerimiento);
    }

    private Usuario obtenerUsuario(Authentication autenticacion) {
        if (autenticacion == null || !autenticacion.isAuthenticated()) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Debes iniciar sesión"
            );
        }

        return usuarioRepository
                .findByCorreoIgnoreCase(autenticacion.getName())
                .filter(usuario -> Boolean.TRUE.equals(usuario.getActivo()))
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.UNAUTHORIZED,
                        "El usuario no está habilitado"
                ));
    }

    private boolean tieneRol(Authentication autenticacion, String rol) {
        return autenticacion.getAuthorities()
                .stream()
                .anyMatch(permiso ->
                        permiso.getAuthority().equals("ROLE_" + rol));
    }

    private boolean puedeVerTodos(Authentication autenticacion) {
        return tieneRol(autenticacion, "ADMINISTRADOR")
                || tieneRol(autenticacion, "SUPERVISOR");
    }

    private String generarCodigo(Long id) {
        return "REQ-"
                + Year.now().getValue()
                + "-"
                + String.format("%06d", id);
    }

    private RequerimientoRespuestaDTO convertirARespuesta(
            Requerimiento requerimiento) {

        RequerimientoRespuestaDTO respuesta =
                RequerimientoRespuestaDTO.builder()
                        .id(requerimiento.getId())
                        .codigo(requerimiento.getCodigo())
                        .titulo(requerimiento.getTitulo())
                        .descripcion(requerimiento.getDescripcion())
                        .tipo(requerimiento.getTipo().getNombre())
                        .prioridad(requerimiento.getPrioridad().getNombre())
                        .estado(requerimiento.getEstado().getNombre())
                        .areaSolicitanteId(
                                requerimiento.getAreaSolicitante().getId()
                        )
                        .areaSolicitante(
                                requerimiento.getAreaSolicitante().getNombre()
                        )
                        .solicitanteId(requerimiento.getSolicitante().getId())
                        .solicitante(
                                nombreCompleto(requerimiento.getSolicitante())
                        )
                        .fechaRegistro(requerimiento.getFechaRegistro())
                        .fechaActualizacion(
                                requerimiento.getFechaActualizacion()
                        )
                        .fechaLimiteAtencion(
                                requerimiento.getFechaLimiteAtencion()
                        )
                        .fechaCierre(requerimiento.getFechaCierre())
                        .build();

        if (requerimiento.getAreaResponsable() != null) {
            respuesta.setAreaResponsableId(
                    requerimiento.getAreaResponsable().getId()
            );
            respuesta.setAreaResponsable(
                    requerimiento.getAreaResponsable().getNombre()
            );
        }

        if (requerimiento.getResponsable() != null) {
            respuesta.setResponsableId(
                    requerimiento.getResponsable().getId()
            );
            respuesta.setResponsable(
                    nombreCompleto(requerimiento.getResponsable())
            );
        }

        return respuesta;
    }

    private String nombreCompleto(Usuario usuario) {
        return usuario.getNombres() + " " + usuario.getApellidos();
    }
}