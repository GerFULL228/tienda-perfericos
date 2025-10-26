package com.example.tiendaperfericos.services;

import com.example.tiendaperfericos.entity.Usuarios;
import org.springframework.security.core.Authentication;

public interface SecurityService {
    boolean tieneAccesoAdmin();
    boolean tieneAccesoUsuario();
    boolean esPropietarioRecurso(Long usuarioId);
    Authentication getAuthentication();
    String getUsernameAutenticado();
    Usuarios getUsuarioAutenticado();
    boolean puedeVerPedido(Long pedidoId);
    boolean puedeEditarProducto(Long productoId);
}