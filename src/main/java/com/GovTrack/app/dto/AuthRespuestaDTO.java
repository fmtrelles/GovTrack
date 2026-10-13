package com.GovTrack.app.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class AuthRespuestaDTO {

    private String token;
    private String tipo;
    private String correo;
}