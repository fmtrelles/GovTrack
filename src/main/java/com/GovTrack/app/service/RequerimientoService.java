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
import com.GovTrack.app.repository.AreaRepository;
import com.GovTrack.app.repository.EstadoRequerimientoRepository;
import com.GovTrack.app.repository.PrioridadRepository;
import com.GovTrack.app.repository.RequerimientoRepository;
import com.GovTrack.app.repository.TipoRequerimientoRepository;
import com.GovTrack.app.repository.UsuarioRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Year;
import java.util.List;

@Service
public class RequerimientoService {

    private final RequerimientoRepository requerimientoRepository;
    private final TipoRequerimientoRepository tipoRepository;
    private final PrioridadRepository prioridadRepository;
    private final EstadoRequerimientoRepository estadoRepository;
    private final AreaRepository areaRepository;
    private final UsuarioRepository usuarioRepository;

    public RequerimientoService(
            RequerimientoRepository requerimientoRepository,
            TipoRequerimientoRepository tipoRepository,
            PrioridadRepository prioridadRepository,
            EstadoRequerimientoRepository estadoRepository,
            AreaRepository areaRepository,
            UsuarioRepository usuarioRepository
    ) {
        this.requerimientoRepository = requerimientoRepository;
        this.tipoRepository = tipoRepository;
        this.prioridadRepository = prioridadRepository;
        this.estadoRepository = estadoRepository;
        this.areaRepository = areaRepository;
        this.usuarioRepository = usuarioRepository;
    }

    @Transactional
    public RequerimientoRespuestaDTO registrar(
            RequerimientoRegistroDTO dto
    ) {
        TipoRequerimiento tipo = tipoRepository
                .findById(dto.getTipoRequerimientoId())
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "El tipo de requerimiento no existe"
                ));

        Prioridad prioridad = prioridadRepository
                .findById(dto.getPrioridadId())
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "La prioridad no existe"
                ));

        EstadoRequerimiento estadoInicial = estadoRepository
                .findByNombreIgnoreCase("REGISTRADO")
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "No se encontró el estado inicial REGISTRADO"
                ));

        Area area = areaRepository
                .findById(dto.getAreaSolicitanteId())
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "El área solicitante no existe"
                ));

        Usuario solicitante = usuarioRepository
                .findById(dto.getSolicitanteId())
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "El usuario solicitante no existe"
                ));

        if (
                solicitante.getArea() == null
                        || !solicitante.getArea().getId().equals(area.getId())
        ) {
            throw new IllegalArgumentException(
                    "El usuario solicitante no pertenece al área indicada"
            );
        }

        Requerimiento requerimiento = Requerimiento.builder()
                .titulo(dto.getTitulo())
                .descripcion(dto.getDescripcion())
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

    @Transactional(readOnly = true)
    public List<RequerimientoRespuestaDTO> listar() {
        return requerimientoRepository
                .findAllByOrderByFechaRegistroDesc()
                .stream()
                .map(this::convertirARespuesta)
                .toList();
    }

    @Transactional(readOnly = true)
    public RequerimientoRespuestaDTO buscarPorId(Long id) {

        Requerimiento requerimiento = requerimientoRepository
                .findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "No existe un requerimiento con ID " + id
                ));

        return convertirARespuesta(requerimiento);
    }

    private String generarCodigo(Long id) {
        return "REQ-"
                + Year.now().getValue()
                + "-"
                + String.format("%06d", id);
    }

    private RequerimientoRespuestaDTO convertirARespuesta(
            Requerimiento requerimiento
    ) {
        RequerimientoRespuestaDTO respuesta =
                RequerimientoRespuestaDTO.builder()
                        .id(requerimiento.getId())
                        .codigo(requerimiento.getCodigo())
                        .titulo(requerimiento.getTitulo())
                        .descripcion(requerimiento.getDescripcion())
                        .tipo(requerimiento.getTipo().getNombre())
                        .prioridad(
                                requerimiento.getPrioridad().getNombre()
                        )
                        .estado(requerimiento.getEstado().getNombre())
                        .areaSolicitanteId(
                                requerimiento
                                        .getAreaSolicitante()
                                        .getId()
                        )
                        .areaSolicitante(
                                requerimiento
                                        .getAreaSolicitante()
                                        .getNombre()
                        )
                        .solicitanteId(
                                requerimiento.getSolicitante().getId()
                        )
                        .solicitante(
                                nombreCompleto(
                                        requerimiento.getSolicitante()
                                )
                        )
                        .fechaRegistro(
                                requerimiento.getFechaRegistro()
                        )
                        .fechaActualizacion(
                                requerimiento.getFechaActualizacion()
                        )
                        .fechaLimiteAtencion(
                                requerimiento.getFechaLimiteAtencion()
                        )
                        .fechaCierre(
                                requerimiento.getFechaCierre()
                        )
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
        return usuario.getNombres()
                + " "
                + usuario.getApellidos();
    }
}