package com.GovTrack.app.repository;

import com.GovTrack.app.model.Requerimiento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

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

    @Query("""
            SELECT r
            FROM Requerimiento r
            LEFT JOIN r.responsable responsable
            WHERE (
                :verTodos = true
                OR (:verPropios = true AND r.solicitante.id = :usuarioId)
                OR (:verAsignados = true AND responsable.id = :usuarioId)
            )
            AND (:estadoId IS NULL OR r.estado.id = :estadoId)
            ORDER BY r.fechaRegistro DESC, r.id DESC
            """)
    List<Requerimiento> buscarVisibles(
            @Param("usuarioId") Long usuarioId,
            @Param("verTodos") boolean verTodos,
            @Param("verPropios") boolean verPropios,
            @Param("verAsignados") boolean verAsignados,
            @Param("estadoId") Long estadoId
    );
}