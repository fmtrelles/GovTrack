package com.GovTrack.app.service;

import com.GovTrack.app.dto.RequerimientoRegistroDTO;
import com.GovTrack.app.dto.RequerimientoRespuestaDTO;
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
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.time.Year;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
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
    private UsuarioRepository usuarioRepository;

    @InjectMocks
    private RequerimientoService requerimientoService;

    @Test
    void debeRegistrarUnRequerimientoConEstadoRegistrado() {
        Area area = Area.builder()
                .id(1L)
                .nombre("Contabilidad")
                .siglas("CONT")
                .activo(true)
                .build();

        Usuario solicitante = Usuario.builder()
                .id(2L)
                .nombres("Usuario")
                .apellidos("Prueba")
                .correo("prueba@govtrack.pe")
                .activo(true)
                .area(area)
                .build();

        TipoRequerimiento tipo = TipoRequerimiento.builder()
                .id(1L)
                .nombre("BIEN")
                .activo(true)
                .build();

        Prioridad prioridad = Prioridad.builder()
                .id(3L)
                .nombre("ALTA")
                .nivel(3)
                .activo(true)
                .build();

        EstadoRequerimiento estado = EstadoRequerimiento.builder()
                .id(1L)
                .nombre("REGISTRADO")
                .orden(1)
                .activo(true)
                .build();

        Authentication autenticacion =
                new UsernamePasswordAuthenticationToken(
                        "prueba@govtrack.pe",
                        null,
                        List.of(
                                new SimpleGrantedAuthority("ROLE_SOLICITANTE")
                        )
                );

        RequerimientoRegistroDTO dto = new RequerimientoRegistroDTO();

        dto.setTitulo("Compra de computadoras");
        dto.setDescripcion(
                "Se solicita la adquisición de cinco computadoras"
        );
        dto.setTipoRequerimientoId(1L);
        dto.setPrioridadId(3L);

        when(usuarioRepository.findByCorreoIgnoreCase("prueba@govtrack.pe"))
                .thenReturn(Optional.of(solicitante));

        when(tipoRepository.findById(1L))
                .thenReturn(Optional.of(tipo));

        when(prioridadRepository.findById(3L))
                .thenReturn(Optional.of(prioridad));

        when(estadoRepository.findByNombreIgnoreCase("REGISTRADO"))
                .thenReturn(Optional.of(estado));

        when(requerimientoRepository.saveAndFlush(any(Requerimiento.class)))
                .thenAnswer(invocacion -> {
                    Requerimiento requerimiento = invocacion.getArgument(0);

                    assertNotNull(requerimiento.getCodigo());
                    assertFalse(requerimiento.getCodigo().isBlank());

                    if (requerimiento.getId() == null) {
                        requerimiento.setId(1L);
                    }

                    return requerimiento;
                });

        String codigoEsperado =
                "REQ-" + Year.now().getValue() + "-000001";

        RequerimientoRespuestaDTO resultado =
                requerimientoService.registrar(dto, autenticacion);

        assertEquals(1L, resultado.getId());
        assertEquals(codigoEsperado, resultado.getCodigo());
        assertEquals("Compra de computadoras", resultado.getTitulo());
        assertEquals("REGISTRADO", resultado.getEstado());
        assertEquals("BIEN", resultado.getTipo());
        assertEquals("ALTA", resultado.getPrioridad());
        assertEquals(1L, resultado.getAreaSolicitanteId());
        assertEquals("Contabilidad", resultado.getAreaSolicitante());
        assertEquals(2L, resultado.getSolicitanteId());
        assertEquals("Usuario Prueba", resultado.getSolicitante());

        verify(usuarioRepository)
                .findByCorreoIgnoreCase("prueba@govtrack.pe");

        verify(requerimientoRepository, times(2))
                .saveAndFlush(any(Requerimiento.class));
    }
}