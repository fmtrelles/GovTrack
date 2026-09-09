package com.GovTrack.app.dto;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RequerimientoRegistroDTOTest {

    private final Validator validator =
            Validation.buildDefaultValidatorFactory().getValidator();

    @Test
    void debeRechazarUnRequerimientoConDatosInvalidos() {

        RequerimientoRegistroDTO dto =
                new RequerimientoRegistroDTO();

        dto.setTitulo("");
        dto.setDescripcion("Corta");

        Set<ConstraintViolation<RequerimientoRegistroDTO>> errores =
                validator.validate(dto);

        assertFalse(errores.isEmpty());

        assertTrue(tieneErrorEnCampo(errores, "titulo"));
        assertTrue(tieneErrorEnCampo(errores, "descripcion"));
        assertTrue(tieneErrorEnCampo(
                errores,
                "tipoRequerimientoId"
        ));
        assertTrue(tieneErrorEnCampo(errores, "prioridadId"));
        assertTrue(tieneErrorEnCampo(
                errores,
                "areaSolicitanteId"
        ));
        assertTrue(tieneErrorEnCampo(errores, "solicitanteId"));
    }

    @Test
    void debeAceptarUnRequerimientoConDatosValidos() {

        RequerimientoRegistroDTO dto =
                new RequerimientoRegistroDTO();

        dto.setTitulo("Compra de computadoras");
        dto.setDescripcion(
                "Se solicita la compra de cinco computadoras"
        );
        dto.setTipoRequerimientoId(1L);
        dto.setPrioridadId(3L);
        dto.setAreaSolicitanteId(1L);
        dto.setSolicitanteId(1L);

        Set<ConstraintViolation<RequerimientoRegistroDTO>> errores =
                validator.validate(dto);

        assertTrue(errores.isEmpty());
    }

    private boolean tieneErrorEnCampo(
            Set<ConstraintViolation<RequerimientoRegistroDTO>> errores,
            String campo
    ) {
        return errores.stream()
                .anyMatch(error ->
                        error.getPropertyPath()
                                .toString()
                                .equals(campo)
                );
    }
}