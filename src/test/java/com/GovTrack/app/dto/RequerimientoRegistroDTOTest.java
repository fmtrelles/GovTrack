package com.GovTrack.app.dto;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertTrue;

class RequerimientoRegistroDTOTest {

    private static ValidatorFactory factory;
    private static Validator validator;

    @BeforeAll
    static void configurarValidador() {
        factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @AfterAll
    static void cerrarValidador() {
        factory.close();
    }

    @Test
    void debeRechazarUnRequerimientoConDatosInvalidos() {
        RequerimientoRegistroDTO dto = new RequerimientoRegistroDTO();

        dto.setTitulo("");
        dto.setDescripcion("Corta");

        Set<ConstraintViolation<RequerimientoRegistroDTO>> errores =
                validator.validate(dto);

        assertTrue(tieneErrorEnCampo(errores, "titulo"));
        assertTrue(tieneErrorEnCampo(errores, "descripcion"));
        assertTrue(tieneErrorEnCampo(errores, "tipoRequerimientoId"));
        assertTrue(tieneErrorEnCampo(errores, "prioridadId"));
    }

    @Test
    void debeAceptarUnRequerimientoConDatosValidos() {
        RequerimientoRegistroDTO dto = new RequerimientoRegistroDTO();

        dto.setTitulo("Compra de computadoras");
        dto.setDescripcion(
                "Se solicita la compra de cinco computadoras"
        );
        dto.setTipoRequerimientoId(1L);
        dto.setPrioridadId(3L);

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
                        error.getPropertyPath().toString().equals(campo)
                );
    }
}