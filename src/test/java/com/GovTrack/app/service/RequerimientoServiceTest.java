package com.GovTrack.app.service;

import com.GovTrack.app.dto.RequerimientoRegistroDTO;
import com.GovTrack.app.dto.RequerimientoRespuestaDTO;
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
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RequerimientoServiceTest {

    @Mock
    private RequerimientoRepository requerimientoRepository;

    @Mock
    private TipoRequerimientoRepository tipoRepository;

    @Mock
    private PrioridadRepository prioridadRepository;

    @Mock
    private EstadoRequerimientoRepository estadoRepository;

    @Mock
    private AreaRepository areaRepository;

    @Mock
    private UsuarioRepository usuarioRepository;

    @InjectMocks
    private RequerimientoService requerimientoService;

    @Test
    void debeRegistrarUnRequerimientoConEstadoRegistrado() {

        Area area = Area.builder()
                .id(1L)
                .nombre("Contabilidad")
                .siglas("CONT")
                .build();

        Usuario solicitante = Usuario.builder()
                .id(1L)
                .nombres("Fernando")
                .apellidos("Muñoz")
                .correo("fernando@govtrack.pe")
                .area(area)
                .build();

        TipoRequerimiento tipo = TipoRequerimiento.builder()
                .id(1L)
                .nombre("BIEN")
                .build();

        Prioridad prioridad = Prioridad.builder()
                .id(3L)
                .nombre("ALTA")
                .nivel(3)
                .build();

        EstadoRequerimiento estado =
                EstadoRequerimiento.builder()
                        .id(1L)
                        .nombre("REGISTRADO")
                        .orden(1)
                        .build();

        RequerimientoRegistroDTO dto =
                new RequerimientoRegistroDTO();

        dto.setTitulo("Compra de computadoras");
        dto.setDescripcion(
                "Se solicita la adquisición de cinco computadoras"
        );
        dto.setTipoRequerimientoId(1L);
        dto.setPrioridadId(3L);
        dto.setAreaSolicitanteId(1L);
        dto.setSolicitanteId(1L);

        when(tipoRepository.findById(1L))
                .thenReturn(Optional.of(tipo));

        when(prioridadRepository.findById(3L))
                .thenReturn(Optional.of(prioridad));

        when(estadoRepository.findByNombreIgnoreCase("REGISTRADO"))
                .thenReturn(Optional.of(estado));

        when(areaRepository.findById(1L))
                .thenReturn(Optional.of(area));

        when(usuarioRepository.findById(1L))
                .thenReturn(Optional.of(solicitante));

        when(requerimientoRepository.save(any(Requerimiento.class)))
                .thenAnswer(invocacion -> {
                    Requerimiento requerimiento =
                            invocacion.getArgument(0);

                    if (requerimiento.getId() == null) {
                        requerimiento.setId(1L);
                    }

                    return requerimiento;
                });

        RequerimientoRespuestaDTO resultado =
                requerimientoService.registrar(dto);

        assertEquals(1L, resultado.getId());
        assertEquals(
                "REQ-2026-000001",
                resultado.getCodigo()
        );
        assertEquals(
                "Compra de computadoras",
                resultado.getTitulo()
        );
        assertEquals("REGISTRADO", resultado.getEstado());
        assertEquals(
                "Contabilidad",
                resultado.getAreaSolicitante()
        );

        verify(requerimientoRepository, times(2))
                .save(any(Requerimiento.class));
    }
}