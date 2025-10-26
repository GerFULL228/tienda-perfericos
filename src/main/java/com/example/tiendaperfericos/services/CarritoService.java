package com.example.tiendaperfericos.services;

import com.example.tiendaperfericos.entity.Carrito;
import com.example.tiendaperfericos.entity.ItemCarrito;
import com.example.tiendaperfericos.entity.Usuarios;

import java.util.List;
import java.util.Optional;

public interface CarritoService {
    Optional<Carrito> findByUsuario(Usuarios usuario);
    Optional<Carrito> findByUsuarioId(Long usuarioId);
    Carrito crearCarrito(Usuarios usuario);
    Carrito agregarProducto(Long usuarioId, Long productoId, Integer cantidad);
    Carrito actualizarCantidad(Long usuarioId, Long productoId, Integer cantidad);
    void eliminarProducto(Long usuarioId, Long productoId);
    void limpiarCarrito(Long usuarioId);
    void eliminarCarrito(Long usuarioId);
    Integer contarItems(Long usuarioId);
    List<ItemCarrito> obtenerItemsCarrito(Long usuarioId);
}