package com.GovTrack.app.repository;

import com.GovTrack.app.model.Requerimiento;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface RequerimientoRepository
        extends JpaRepository<Requerimiento, Long> {

    Optional<Requerimiento> findByCodigo(String codigo);

    boolean existsByCodigo(String codigo);

    List<Requerimiento> findAllByOrderByFechaRegistroDesc();

    List<Requerimiento> findByEstado_Id(Long estadoId);

    List<Requerimiento> findByAreaSolicitante_Id(Long areaId);

    List<Requerimiento> findBySolicitante_Id(Long usuarioId);
}