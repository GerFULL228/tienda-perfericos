package com.example.tiendaperfericos.services.implement;


import com.example.tiendaperfericos.Repostory.CarritoRepository;
import com.example.tiendaperfericos.Repostory.ItemCarritoRepository;
import com.example.tiendaperfericos.Repostory.ProductoRepository;
import com.example.tiendaperfericos.entity.Carrito;
import com.example.tiendaperfericos.entity.ItemCarrito;
import com.example.tiendaperfericos.entity.Producto;
import com.example.tiendaperfericos.entity.Usuarios;
import com.example.tiendaperfericos.services.CarritoService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class CarritoServiceImpl implements CarritoService {

    private final CarritoRepository carritoRepository;
    private final ItemCarritoRepository itemCarritoRepository;
    private final ProductoRepository productoRepository;

    @Override
    @Transactional(readOnly = true)
    public Optional<Carrito> findByUsuario(Usuarios usuario) {
        return carritoRepository.findByUsuario(usuario);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Carrito> findByUsuarioId(Long usuarioId) {
        return carritoRepository.findByUsuarioId(usuarioId);
    }

    @Override
    @Transactional
    public Carrito crearCarrito(Usuarios usuarios) {
        Carrito carrito = Carrito.builder()
                .usuario(usuarios)
                .build();
        return carritoRepository.save(carrito);
    }

    @Override
    @Transactional
    public Carrito agregarProducto(Long usuarioId, Long productoId, Integer cantidad) {
        Carrito carrito = obtenerOCrearCarrito(usuarioId);
        Producto producto = productoRepository.findByIdAndActivoTrue(productoId)
                .orElseThrow(() -> new RuntimeException("Producto no encontrado o inactivo"));


        if (producto.getStock() < cantidad) {
            throw new RuntimeException("Stock insuficiente. Disponible: " + producto.getStock());
        }

        carrito.agregarItem(producto, cantidad);
        return carritoRepository.save(carrito);
    }

    @Override
    @Transactional
    public Carrito actualizarCantidad(Long usuarioId, Long productoId, Integer cantidad) {
        Carrito carrito = obtenerCarrito(usuarioId);
        Producto producto = productoRepository.findByIdAndActivoTrue(productoId)
                .orElseThrow(() -> new RuntimeException("Producto no encontrado"));

        if (cantidad <= 0) {
            carrito.eliminarItem(productoId);
        } else {

            if (producto.getStock() < cantidad) {
                throw new RuntimeException("Stock insuficiente. Disponible: " + producto.getStock());
            }

            ItemCarrito item = itemCarritoRepository.findByCarritoAndProducto(carrito, producto)
                    .orElseThrow(() -> new RuntimeException("Producto no encontrado en el carrito"));
            item.setCantidad(cantidad);
            itemCarritoRepository.save(item);
        }

        return carritoRepository.save(carrito);
    }

    @Override
    @Transactional
    public void eliminarProducto(Long usuarioId, Long productoId) {
        Carrito carrito = obtenerCarrito(usuarioId);
        carrito.eliminarItem(productoId);
        carritoRepository.save(carrito);
    }

    @Override
    @Transactional
    public void limpiarCarrito(Long usuarioId) {
        Carrito carrito = obtenerCarrito(usuarioId);
        carrito.limpiarCarrito();
        carritoRepository.save(carrito);
    }

    @Override
    @Transactional
    public void eliminarCarrito(Long usuarioId) {
        carritoRepository.findByUsuarioId(usuarioId).ifPresent(carritoRepository::delete);
    }

    @Override
    @Transactional(readOnly = true)
    public Integer contarItems(Long usuarioId) {
        return itemCarritoRepository.countItemsByUsuarioId(usuarioId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ItemCarrito> obtenerItemsCarrito(Long usuarioId) {
        return itemCarritoRepository.findByUsuarioId(usuarioId);
    }

    private Carrito obtenerCarrito(Long usuarioId) {
        return carritoRepository.findByUsuarioId(usuarioId)
                .orElseThrow(() -> new RuntimeException("Carrito no encontrado"));
    }

    private Carrito obtenerOCrearCarrito(Long usuarioId) {
        return carritoRepository.findByUsuarioId(usuarioId)
                .orElseGet(() -> {

                    throw new RuntimeException("Usuario no encontrado para crear carrito");
                });
    }
}