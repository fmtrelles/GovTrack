package com.GovTrack.app.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RequerimientoRegistroDTO {

    @NotBlank(message = "El título es obligatorio")
    @Size(max = 150, message = "El título no puede superar los 150 caracteres")
    private String titulo;

    @NotBlank(message = "La descripción es obligatoria")
    @Size(
            min = 10,
            max = 5000,
            message = "La descripción debe tener entre 10 y 5000 caracteres"
    )
    private String descripcion;

    @NotNull(message = "El tipo de requerimiento es obligatorio")
    @Positive(message = "El identificador del tipo debe ser positivo")
    private Long tipoRequerimientoId;

    @NotNull(message = "La prioridad es obligatoria")
    @Positive(message = "El identificador de la prioridad debe ser positivo")
    private Long prioridadId;
}