package com.GovTrack.app.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "estados_requerimiento")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EstadoRequerimiento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 80)
    private String nombre;

    @Column(length = 250)
    private String descripcion;

    @Column(nullable = false, unique = true)
    private Integer orden;

    @Column(nullable = false)
    @Builder.Default
    private Boolean estadoFinal = false;

    @Column(nullable = false)
    @Builder.Default
    private Boolean activo = true;
}