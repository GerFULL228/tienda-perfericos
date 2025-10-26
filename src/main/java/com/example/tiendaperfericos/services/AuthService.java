package com.example.tiendaperfericos.services;

import com.example.tiendaperfericos.entity.Usuarios;
import org.springframework.security.core.userdetails.UserDetailsService;

public interface AuthService extends UserDetailsService {
    Usuarios registrarUsuario(String email, String password, String nombre, String apellido, String rol);
    boolean validarCredenciales(String email, String password);
    Usuarios obtenerUsuarioAutenticado();
    Long getUsuarioAutenticadoId();
    void cambiarPassword(String email, String nuevaPassword);
    boolean existeUsuario(String email);
    boolean tieneRol(String rolNombre);
    boolean esAdmin();
    boolean esUsuario();
}