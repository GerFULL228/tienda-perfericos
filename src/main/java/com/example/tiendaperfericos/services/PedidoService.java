package com.example.tiendaperfericos.services;

import com.example.tiendaperfericos.entity.DetallePedido;
import com.example.tiendaperfericos.entity.Pedido;
import com.example.tiendaperfericos.entity.emun.EstadoPedido;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface PedidoService {
    List<Pedido> findAll();
    Optional<Pedido> findById(Long id);
    List<Pedido> findByUsuarioId(Long usuarioId);
    List<Pedido> findByEstado(EstadoPedido estado);
    Pedido crearPedidoDesdeCarrito(Long usuarioId, String direccionEntrega, String telefonoContacto);
    Pedido actualizarEstado(Long pedidoId, EstadoPedido nuevoEstado);
    void cancelarPedido(Long pedidoId);
    List<DetallePedido> obtenerDetallesPedido(Long pedidoId);
    Long countByEstado(EstadoPedido estado);
    BigDecimal getIngresosTotales(LocalDateTime fechaInicio, LocalDateTime fechaFin);
    List<Pedido> findPedidosRecientes(int dias);
}