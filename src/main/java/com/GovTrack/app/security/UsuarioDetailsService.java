package com.GovTrack.app.security;

import com.GovTrack.app.model.Usuario;
import com.GovTrack.app.repository.UsuarioRepository;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Locale;

@Service
public class UsuarioDetailsService implements UserDetailsService {

    private final UsuarioRepository usuarioRepository;

    public UsuarioDetailsService(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String correo)
            throws UsernameNotFoundException {

        if (correo == null || correo.isBlank()) {
            throw new UsernameNotFoundException("Credenciales inválidas");
        }

        Usuario usuario = usuarioRepository
                .findByCorreoIgnoreCase(correo.trim())
                .orElseThrow(() ->
                        new UsernameNotFoundException("Credenciales inválidas"));

        if (usuario.getPassword() == null || usuario.getPassword().isBlank()) {
            throw new UsernameNotFoundException("Credenciales inválidas");
        }

        List<SimpleGrantedAuthority> permisos = usuario.getRoles()
                .stream()
                .filter(rol -> Boolean.TRUE.equals(rol.getActivo()))
                .map(rol -> rol.getNombre().trim().toUpperCase(Locale.ROOT))
                .map(nombre -> nombre.startsWith("ROLE_")
                        ? nombre
                        : "ROLE_" + nombre)
                .distinct()
                .map(SimpleGrantedAuthority::new)
                .toList();

        return User.withUsername(usuario.getCorreo())
                .password(usuario.getPassword())
                .authorities(permisos)
                .disabled(!Boolean.TRUE.equals(usuario.getActivo()))
                .build();
    }
}