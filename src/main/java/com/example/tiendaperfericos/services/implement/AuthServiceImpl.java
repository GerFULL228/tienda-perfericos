package com.example.tiendaperfericos.services.implement;


import com.example.tiendaperfericos.Repostory.RolRepository;
import com.example.tiendaperfericos.Repostory.UsuarioRepository;
import com.example.tiendaperfericos.entity.Rol;
import com.example.tiendaperfericos.entity.Usuarios;
import com.example.tiendaperfericos.services.AuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Collection;
import java.util.Collections;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthServiceImpl implements AuthService {

    private final UsuarioRepository usuarioRepository;
    private final RolRepository rolRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        Usuarios usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado con email: " + email));

        if (!usuario.isActivo()) {
            throw new UsernameNotFoundException("Usuario inactivo: " + email);
        }

        return User.builder()
                .username(usuario.getEmail())
                .password(usuario.getPassword())
                .authorities(getAuthorities(usuario))
                .accountExpired(false)
                .accountLocked(false)
                .credentialsExpired(false)
                .disabled(!usuario.isActivo())
                .build();
    }

    private Collection<? extends GrantedAuthority> getAuthorities(Usuarios usuario) {
        return Collections.singletonList(
                new SimpleGrantedAuthority("ROLE_" + usuario.getRol().getNombre())
        );
    }

    @Override
    @Transactional
    public Usuarios registrarUsuario(String email, String password, String nombre, String apellido, String rolNombre) {
        log.info("Intentando registrar usuario: {}", email);

        if (usuarioRepository.existsByEmail(email)) {
            log.warn("Email ya registrado: {}", email);
            throw new RuntimeException("El email ya está registrado: " + email);
        }

        Rol rol = rolRepository.findByNombre(rolNombre)
                .orElseGet(() -> {
                    log.info("Creando nuevo rol: {}", rolNombre);
                    Rol nuevoRol = new Rol();
                    nuevoRol.setNombre(rolNombre);
                    nuevoRol.setDescripcion("Rol de " + rolNombre);
                    return rolRepository.save(nuevoRol);
                });

        Usuarios usuario = new Usuarios();
        usuario.setEmail(email);
        usuario.setPassword(passwordEncoder.encode(password));
        usuario.setNombre(nombre);
        usuario.setApellido(apellido);
        usuario.setRol(rol);
        usuario.setActivo(true);

        Usuarios usuarioGuardado = usuarioRepository.save(usuario);
        log.info("Usuario registrado exitosamente: {} con ID: {}", email, usuarioGuardado.getId());

        return usuarioGuardado;
    }


    @Override
    @Transactional(readOnly = true)
    public boolean validarCredenciales(String email, String password) {
        Optional<Usuarios> usuarioOpt = usuarioRepository.findByEmail(email);

        if (usuarioOpt.isPresent()) {
            Usuarios usuario = usuarioOpt.get();
            return passwordEncoder.matches(password, usuario.getPassword()) && usuario.isActivo();
        }

        return false;
    }

    @Override
    @Transactional(readOnly = true)
    public Usuarios obtenerUsuarioAutenticado() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated() ||
                "anonymousUser".equals(authentication.getPrincipal())) {
            return null;
        }

        String email = authentication.getName();
        return usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado: " + email));
    }

    @Override
    @Transactional(readOnly = true)
    public Long getUsuarioAutenticadoId() {
        Usuarios usuario = obtenerUsuarioAutenticado();
        return usuario != null ? usuario.getId() : null;
    }

    @Override
    @Transactional
    public void cambiarPassword(String email, String nuevaPassword) {
        Usuarios usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado: " + email));

        usuario.setPassword(passwordEncoder.encode(nuevaPassword));
        usuarioRepository.save(usuario);
        log.info("Password cambiado exitosamente para el usuario: {}", email);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existeUsuario(String email) {
        return usuarioRepository.existsByEmail(email);
    }

    @Transactional(readOnly = true)
    public boolean tieneRol(String rolNombre) {
        Usuarios usuario = obtenerUsuarioAutenticado();
        return usuario != null && usuario.getRol().getNombre().equals(rolNombre);
    }

    @Transactional(readOnly = true)
    public boolean esAdmin() {
        return tieneRol("ADMIN");
    }

    @Transactional(readOnly = true)
    public boolean esUsuario() {
        return tieneRol("USER");
    }

    @Transactional
    public void activarUsuario(Long usuarioId) {
        usuarioRepository.findById(usuarioId).ifPresent(usuario -> {
            usuario.setActivo(true);
            usuarioRepository.save(usuario);
            log.info("Usuario activado: {}", usuario.getEmail());
        });
    }

    @Transactional
    public void desactivarUsuario(Long usuarioId) {
        usuarioRepository.findById(usuarioId).ifPresent(usuario -> {
            usuario.setActivo(false);
            usuarioRepository.save(usuario);
            log.info("Usuario desactivado: {}", usuario.getEmail());
        });
    }

    @Transactional(readOnly = true)
    public boolean puedeAccederRecurso(Long usuarioId) {
        Usuarios usuarioAutenticado = obtenerUsuarioAutenticado();

        if (usuarioAutenticado == null) {
            return false;
        }


        if (esAdmin()) {
            return true;
        }


        return usuarioAutenticado.getId().equals(usuarioId);
    }
}