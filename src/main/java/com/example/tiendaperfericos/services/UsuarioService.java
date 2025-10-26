package com.example.tiendaperfericos.services;

import com.example.tiendaperfericos.entity.Usuarios;

import java.util.List;
import java.util.Optional;

public interface UsuarioService {
    List<Usuarios> findAll();
    Optional<Usuarios> findById(Long id);
    Optional<Usuarios> findByEmail(String email);
    Usuarios save(Usuarios Usuarios);
    void deleteById(Long id);
    boolean existsByEmail(String email);
    List<Usuarios> findByRol(String rolNombre);
    List<Usuarios> findUsuariosActivos();
    Usuarios registrarUsuarios(Usuarios Usuarioss, String rolNombre);
    void cambiarEstadoUsuarios(Long id, boolean activo);
    Long countByRol(String rolNombre);
}