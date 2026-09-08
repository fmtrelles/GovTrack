package com.GovTrack.app.repository;

import com.GovTrack.app.model.Prioridad;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PrioridadRepository extends JpaRepository<Prioridad, Long> {

    Optional<Prioridad> findByNombreIgnoreCase(String nombre);

    boolean existsByNombreIgnoreCase(String nombre);

    boolean existsByNivel(Integer nivel);
}