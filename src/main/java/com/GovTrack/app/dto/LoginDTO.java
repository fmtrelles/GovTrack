package com.GovTrack.app.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class LoginDTO {

    @NotBlank(message = "El correo es obligatorio")
    @Email(message = "El correo debe tener un formato válido")
    @Size(max = 150, message = "El correo no debe superar los 150 caracteres")
    private String correo;

    @NotBlank(message = "La contraseña es obligatoria")
    @Size(max = 64, message = "La contraseña no debe superar los 64 caracteres")
    private String password;
}