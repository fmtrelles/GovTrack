package com.GovTrack.app.repository;

import com.GovTrack.app.model.TipoRequerimiento;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TipoRequerimientoRepository
        extends JpaRepository<TipoRequerimiento, Long> {

    Optional<TipoRequerimiento> findByNombreIgnoreCase(String nombre);

    boolean existsByNombreIgnoreCase(String nombre);
}