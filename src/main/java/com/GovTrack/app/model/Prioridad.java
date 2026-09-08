package com.GovTrack.app.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "prioridades")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Prioridad {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String nombre;

    @Column(nullable = false, unique = true)
    private Integer nivel;

    @Column(length = 250)
    private String descripcion;

    @Column(nullable = false)
    @Builder.Default
    private Boolean activo = true;
}