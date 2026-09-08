package com.GovTrack.app.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RequerimientoRespuestaDTO {

    private Long id;
    private String codigo;
    private String titulo;
    private String descripcion;

    private String tipo;
    private String prioridad;
    private String estado;

    private Long areaSolicitanteId;
    private String areaSolicitante;

    private Long solicitanteId;
    private String solicitante;

    private Long areaResponsableId;
    private String areaResponsable;

    private Long responsableId;
    private String responsable;

    private LocalDateTime fechaRegistro;
    private LocalDateTime fechaActualizacion;
    private LocalDateTime fechaLimiteAtencion;
    private LocalDateTime fechaCierre;
}