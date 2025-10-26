package com.example.tiendaperfericos.services.implement;




import com.example.tiendaperfericos.Repostory.RolRepository;
import com.example.tiendaperfericos.Repostory.UsuarioRepository;
import com.example.tiendaperfericos.entity.Rol;
import com.example.tiendaperfericos.entity.Usuarios;
import com.example.tiendaperfericos.services.UsuarioService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class UsuarioServiceImpl implements UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final RolRepository rolRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional(readOnly = true)
    public List<Usuarios> findAll() {
        return usuarioRepository.findAll();
    }



    @Override
    @Transactional(readOnly = true)
    public Optional<Usuarios> findById(Long id) {
        return usuarioRepository.findById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Usuarios> findByEmail(String email) {
        return usuarioRepository.findByEmail(email);
    }

    @Override
    @Transactional
    public Usuarios save(Usuarios usuario) {
        if (usuario.getId() == null) {
            usuario.setPassword(passwordEncoder.encode(usuario.getPassword()));
        } else {

            usuarioRepository.findById(usuario.getId()).ifPresent(existingUser -> {
                if (!usuario.getPassword().equals(existingUser.getPassword())) {
                    usuario.setPassword(passwordEncoder.encode(usuario.getPassword()));
                }
            });
        }
        return usuarioRepository.save(usuario);
    }

    @Override
    @Transactional
    public void deleteById(Long id) {
        usuarioRepository.findById(id).ifPresent(usuario -> {
            usuario.setActivo(false);
            usuarioRepository.save(usuario);
        });
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByEmail(String email) {
        return usuarioRepository.existsByEmail(email);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Usuarios> findByRol(String rolNombre) {
        return usuarioRepository.findByRolNombre(rolNombre);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Usuarios> findUsuariosActivos() {
        return usuarioRepository.findByActivoTrue();
    }




    @Override
    @Transactional
    public Usuarios registrarUsuarios(Usuarios usuario, String rolNombre) {
        Rol rol = rolRepository.findByNombre(rolNombre)
                .orElseThrow(() -> new RuntimeException("Rol no encontrado: " + rolNombre));

        usuario.setRol(rol);
        usuario.setPassword(passwordEncoder.encode(usuario.getPassword()));
        usuario.setActivo(true);

        return usuarioRepository.save(usuario);
    }

    @Override
    @Transactional
    public void cambiarEstadoUsuarios(Long id, boolean activo) {
        usuarioRepository.findById(id).ifPresent(usuario -> {
            usuario.setActivo(activo);
            usuarioRepository.save(usuario);
        });
    }

    @Override
    @Transactional(readOnly = true)
    public Long countByRol(String rolNombre) {
        return usuarioRepository.countByRolNombre(rolNombre);
    }


}