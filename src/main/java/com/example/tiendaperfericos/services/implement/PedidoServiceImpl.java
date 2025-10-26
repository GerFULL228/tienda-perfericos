package com.example.tiendaperfericos.services.implement;


import com.example.tiendaperfericos.Repostory.CarritoRepository;
import com.example.tiendaperfericos.Repostory.DetallePedidoRepository;
import com.example.tiendaperfericos.Repostory.ItemCarritoRepository;
import com.example.tiendaperfericos.Repostory.PedidoRepository;
import com.example.tiendaperfericos.entity.Carrito;
import com.example.tiendaperfericos.entity.DetallePedido;
import com.example.tiendaperfericos.entity.ItemCarrito;
import com.example.tiendaperfericos.entity.Pedido;
import com.example.tiendaperfericos.entity.emun.EstadoPedido;
import com.example.tiendaperfericos.services.PedidoService;
import com.example.tiendaperfericos.services.ProductoService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class PedidoServiceImpl implements PedidoService {

    private final PedidoRepository pedidoRepository;
    private final DetallePedidoRepository detallePedidoRepository;
    private final CarritoRepository carritoRepository;
    private final ItemCarritoRepository itemCarritoRepository;
    private final ProductoService productoService;

    @Override
    @Transactional(readOnly = true)
    public List<Pedido> findAll() {
        return pedidoRepository.findAllOrderByFechaDesc();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Pedido> findById(Long id) {
        return pedidoRepository.findById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Pedido> findByUsuarioId(Long usuarioId) {
        return pedidoRepository.findPedidosRecientesByUsuarioId(usuarioId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Pedido> findByEstado(EstadoPedido estado) {
        return pedidoRepository.findByEstado(estado);
    }

    @Override
    @Transactional
    public Pedido crearPedidoDesdeCarrito(Long usuarioId, String direccionEntrega, String telefonoContacto) {
        Carrito carrito = carritoRepository.findByUsuarioId(usuarioId)
                .orElseThrow(() -> new RuntimeException("Carrito no encontrado"));

        if (carrito.getItems().isEmpty()) {
            throw new RuntimeException("El carrito está vacío");
        }


        for (ItemCarrito item : carrito.getItems()) {
            if (item.getProducto().getStock() < item.getCantidad()) {
                throw new RuntimeException("Stock insuficiente para: " + item.getProducto().getNombre());
            }
        }


        Pedido pedido = Pedido.builder()
                .usuario(carrito.getUsuario())
                .estado(EstadoPedido.PENDIENTE)
                .direccionEntrega(direccionEntrega)
                .telefonoContacto(telefonoContacto)
                .total(BigDecimal.ZERO)
                .build();

        Pedido pedidoGuardado = pedidoRepository.save(pedido);


        BigDecimal total = BigDecimal.ZERO;
        for (ItemCarrito item : carrito.getItems()) {
            DetallePedido detalle = DetallePedido.builder()
                    .pedido(pedidoGuardado)
                    .producto(item.getProducto())
                    .cantidad(item.getCantidad())
                    .precioUnitario(item.getPrecioUnitario())
                    .build();

            detallePedidoRepository.save(detalle);
            total = total.add(detalle.getSubtotal());


            productoService.actualizarStock(item.getProducto().getId(), item.getCantidad());
        }


        pedidoGuardado.setTotal(total);
        Pedido pedidoFinal = pedidoRepository.save(pedidoGuardado);


        carrito.limpiarCarrito();
        carritoRepository.save(carrito);

        log.info("Pedido {} creado exitosamente para el usuario {}", pedidoFinal.getId(), usuarioId);
        return pedidoFinal;
    }

    @Override
    @Transactional
    public Pedido actualizarEstado(Long pedidoId, EstadoPedido nuevoEstado) {
        Pedido pedido = pedidoRepository.findById(pedidoId)
                .orElseThrow(() -> new RuntimeException("Pedido no encontrado"));

        pedido.setEstado(nuevoEstado);
        return pedidoRepository.save(pedido);
    }

    @Override
    @Transactional
    public void cancelarPedido(Long pedidoId) {
        Pedido pedido = pedidoRepository.findById(pedidoId)
                .orElseThrow(() -> new RuntimeException("Pedido no encontrado"));

        if (pedido.getEstado() == EstadoPedido.ENTREGADO) {
            throw new RuntimeException("No se puede cancelar un pedido ya entregado");
        }

        // Restaurar stock si el pedido estaba confirmado
        if (pedido.getEstado() == EstadoPedido.CONFIRMADO || pedido.getEstado() == EstadoPedido.EN_PREPARACION) {
            for (DetallePedido detalle : pedido.getDetalles()) {
                productoService.actualizarStock(detalle.getProducto().getId(), -detalle.getCantidad());
            }
        }

        pedido.setEstado(EstadoPedido.CANCELADO);
        pedidoRepository.save(pedido);
    }

    @Override
    @Transactional(readOnly = true)
    public List<DetallePedido> obtenerDetallesPedido(Long pedidoId) {
        return detallePedidoRepository.findByPedidoId(pedidoId);
    }

    @Override
    @Transactional(readOnly = true)
    public Long countByEstado(EstadoPedido estado) {
        return pedidoRepository.countByEstado(estado);
    }

    @Override
    @Transactional(readOnly = true)
    public BigDecimal getIngresosTotales(LocalDateTime fechaInicio, LocalDateTime fechaFin) {
        return pedidoRepository.getIngresosTotales(fechaInicio, fechaFin);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Pedido> findPedidosRecientes(int dias) {
        LocalDateTime fecha = LocalDateTime.now().minusDays(dias);
        return pedidoRepository.findPedidosRecientes(fecha);
    }
}