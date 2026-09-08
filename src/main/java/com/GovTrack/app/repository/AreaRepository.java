package com.GovTrack.app.repository;

import com.GovTrack.app.model.Area;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AreaRepository extends JpaRepository<Area, Long> {

    Optional<Area> findByNombreIgnoreCase(String nombre);

    boolean existsBySiglasIgnoreCase(String siglas);
}