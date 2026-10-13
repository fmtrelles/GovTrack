package com.GovTrack.app.service;

import com.GovTrack.app.dto.AuthRespuestaDTO;
import com.GovTrack.app.dto.LoginDTO;
import com.GovTrack.app.dto.UsuarioRegistroDTO;
import com.GovTrack.app.model.Area;
import com.GovTrack.app.model.Rol;
import com.GovTrack.app.model.Usuario;
import com.GovTrack.app.repository.AreaRepository;
import com.GovTrack.app.repository.RolRepository;
import com.GovTrack.app.repository.UsuarioRepository;
import com.GovTrack.app.security.JwtService;
import com.GovTrack.app.security.UsuarioDetailsService;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

@Service
public class AuthService {

    private final UsuarioRepository usuarioRepository;
    private final RolRepository rolRepository;
    private final AreaRepository areaRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final UsuarioDetailsService usuarioDetailsService;
    private final JwtService jwtService;

    public AuthService(
            UsuarioRepository usuarioRepository,
            RolRepository rolRepository,
            AreaRepository areaRepository,
            PasswordEncoder passwordEncoder,
            AuthenticationManager authenticationManager,
            UsuarioDetailsService usuarioDetailsService,
            JwtService jwtService) {

        this.usuarioRepository = usuarioRepository;
        this.rolRepository = rolRepository;
        this.areaRepository = areaRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.usuarioDetailsService = usuarioDetailsService;
        this.jwtService = jwtService;
    }

    @Transactional
    public AuthRespuestaDTO registrar(UsuarioRegistroDTO dto) {
        validarLongitudPassword(dto.getPassword());

        String correo = dto.getCorreo()
                .trim()
                .toLowerCase(Locale.ROOT);

        if (usuarioRepository.existsByCorreoIgnoreCase(correo)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "El correo ya está registrado"
            );
        }

        Area area = areaRepository.findById(dto.getAreaId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "El área seleccionada no existe"
                ));

        if (!Boolean.TRUE.equals(area.getActivo())) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "El área seleccionada está inactiva"
            );
        }

        Rol rol = rolRepository.findByNombreIgnoreCase("SOLICITANTE")
                .filter(item -> Boolean.TRUE.equals(item.getActivo()))
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.SERVICE_UNAVAILABLE,
                        "El registro de usuarios no está habilitado"
                ));

        Usuario usuario = Usuario.builder()
                .nombres(dto.getNombres().trim())
                .apellidos(dto.getApellidos().trim())
                .correo(correo)
                .password(passwordEncoder.encode(dto.getPassword()))
                .activo(true)
                .area(area)
                .roles(new HashSet<>(Set.of(rol)))
                .build();

        try {
            usuarioRepository.saveAndFlush(usuario);
        } catch (DataIntegrityViolationException exception) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "No se pudo registrar al usuario por un conflicto de datos"
            );
        }

        UserDetails detalles = usuarioDetailsService
                .loadUserByUsername(correo);

        return crearRespuesta(detalles);
    }

    public AuthRespuestaDTO iniciarSesion(LoginDTO dto) {
        validarLongitudPassword(dto.getPassword());

        String correo = dto.getCorreo()
                .trim()
                .toLowerCase(Locale.ROOT);

        try {
            Authentication autenticacion =
                    authenticationManager.authenticate(
                            new UsernamePasswordAuthenticationToken(
                                    correo,
                                    dto.getPassword()
                            )
                    );

            UserDetails usuario =
                    (UserDetails) autenticacion.getPrincipal();

            return crearRespuesta(usuario);

        } catch (AuthenticationException exception) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Correo o contraseña incorrectos, o cuenta no habilitada"
            );
        }
    }

    private AuthRespuestaDTO crearRespuesta(UserDetails usuario) {
        return new AuthRespuestaDTO(
                jwtService.generarToken(usuario),
                "Bearer",
                usuario.getUsername()
        );
    }

    private void validarLongitudPassword(String password) {
        if (password.getBytes(StandardCharsets.UTF_8).length > 72) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "La contraseña supera el límite de 72 bytes"
            );
        }
    }
}