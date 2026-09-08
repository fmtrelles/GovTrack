package com.GovTrack.app.repository;

import com.GovTrack.app.model.EstadoRequerimiento;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface EstadoRequerimientoRepository
        extends JpaRepository<EstadoRequerimiento, Long> {

    Optional<EstadoRequerimiento> findByNombreIgnoreCase(String nombre);

    boolean existsByNombreIgnoreCase(String nombre);

    boolean existsByOrden(Integer orden);
}