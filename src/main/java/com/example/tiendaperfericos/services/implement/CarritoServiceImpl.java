package com.example.tiendaperfericos.services.implement;

import com.example.tiendaperfericos.Repostory.CarritoRepository;
import com.example.tiendaperfericos.Repostory.ItemCarritoRepository;
import com.example.tiendaperfericos.Repostory.ProductoRepository;
import com.example.tiendaperfericos.Repostory.UsuarioRepository;
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
    private final UsuarioRepository usuarioRepository;

    @Override
    @Transactional
    public Carrito agregarProducto(Long usuarioId, Long productoId, Integer cantidad) {
        try {
            log.info("🔵 INICIANDO agregarProducto - usuarioId: {}, productoId: {}, cantidad: {}",
                    usuarioId, productoId, cantidad);

            Carrito carrito = obtenerOCrearCarrito(usuarioId);
            log.info("🛒 Carrito obtenido/creado: {}", carrito.getId());

            Producto producto = productoRepository.findByIdAndActivoTrue(productoId)
                    .orElseThrow(() -> {
                        log.error("❌ Producto no encontrado: {}", productoId);
                        return new RuntimeException("Producto no encontrado o inactivo");
                    });
            log.info(" Producto encontrado: {} - Stock: {}", producto.getNombre(), producto.getStock());

            if (producto.getStock() < cantidad) {
                log.error(" Stock insuficiente: {} < {}", producto.getStock(), cantidad);
                throw new RuntimeException("Stock insuficiente. Disponible: " + producto.getStock());
            }

            // Buscar item existente
            Optional<ItemCarrito> itemExistente = itemCarritoRepository.findByCarritoAndProducto(carrito, producto);
            log.info("🔍 Item existente encontrado: {}", itemExistente.isPresent());

            if (itemExistente.isPresent()) {
                ItemCarrito item = itemExistente.get();
                item.setCantidad(item.getCantidad() + cantidad);
                ItemCarrito savedItem = itemCarritoRepository.save(item);
                log.info("Producto actualizado en carrito: {} - Nueva cantidad: {}",
                        producto.getNombre(), savedItem.getCantidad());
            } else {
                ItemCarrito nuevoItem = ItemCarrito.builder()
                        .carrito(carrito)
                        .producto(producto)
                        .cantidad(cantidad)
                        .precioUnitario(producto.getPrecio())
                        .build();

                ItemCarrito savedItem = itemCarritoRepository.save(nuevoItem);
                log.info(" Nuevo producto agregado al carrito: {} - Cantidad: {}",
                        producto.getNombre(), savedItem.getCantidad());
            }

            Carrito carritoActualizado = carritoRepository.findById(carrito.getId())
                    .orElseThrow(() -> new RuntimeException("Error al recargar carrito"));

            log.info("Carrito actualizado exitosamente. Total items: {}",
                    carritoActualizado.getItems() != null ? carritoActualizado.getItems().size() : 0);

            return carritoActualizado;

        } catch (Exception e) {
            log.error(" ERROR en agregarProducto: {}", e.getMessage(), e);
            throw new RuntimeException("Error al agregar producto al carrito: " + e.getMessage());
        }
    }

    @Override
    @Transactional
    public Carrito actualizarCantidad(Long usuarioId, Long productoId, Integer cantidad) {
        try {
            Carrito carrito = obtenerCarrito(usuarioId);
            Producto producto = productoRepository.findByIdAndActivoTrue(productoId)
                    .orElseThrow(() -> new RuntimeException("Producto no encontrado"));

            if (cantidad <= 0) {

                itemCarritoRepository.findByCarritoAndProducto(carrito, producto)
                        .ifPresent(itemCarritoRepository::delete);
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
        } catch (Exception e) {
            log.error("Error en actualizarCantidad: {}", e.getMessage(), e);
            throw new RuntimeException("Error al actualizar cantidad: " + e.getMessage());
        }
    }

    @Override
    @Transactional
    public void eliminarProducto(Long usuarioId, Long productoId) {
        try {
            Carrito carrito = obtenerCarrito(usuarioId);
            Producto producto = productoRepository.findById(productoId)
                    .orElseThrow(() -> new RuntimeException("Producto no encontrado"));

            itemCarritoRepository.findByCarritoAndProducto(carrito, producto)
                    .ifPresent(itemCarritoRepository::delete);

            carritoRepository.save(carrito);
        } catch (Exception e) {
            log.error("Error en eliminarProducto: {}", e.getMessage(), e);
            throw new RuntimeException("Error al eliminar producto: " + e.getMessage());
        }
    }

    @Override
    @Transactional
    public void limpiarCarrito(Long usuarioId) {
        try {
            Carrito carrito = obtenerCarrito(usuarioId);
            itemCarritoRepository.deleteByCarritoId(carrito.getId());
            carritoRepository.save(carrito);
        } catch (Exception e) {
            log.error("Error en limpiarCarrito: {}", e.getMessage(), e);
            throw new RuntimeException("Error al limpiar carrito: " + e.getMessage());
        }
    }


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
    @Transactional(readOnly = true)
    public Integer contarItems(Long usuarioId) {
        try {
            Integer count = itemCarritoRepository.countItemsByUsuarioId(usuarioId);
            return count != null ? count : 0;
        } catch (Exception e) {
            log.error("Error al contar items: {}", e.getMessage());
            return 0;
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<ItemCarrito> obtenerItemsCarrito(Long usuarioId) {
        return itemCarritoRepository.findByUsuarioId(usuarioId);
    }

    @Override
    @Transactional
    public void eliminarCarrito(Long usuarioId) {
        carritoRepository.findByUsuarioId(usuarioId).ifPresent(carritoRepository::delete);
    }

    private Carrito obtenerCarrito(Long usuarioId) {
        return carritoRepository.findByUsuarioId(usuarioId)
                .orElseThrow(() -> new RuntimeException("Carrito no encontrado"));
    }

    private Carrito obtenerOCrearCarrito(Long usuarioId) {
        return carritoRepository.findByUsuarioId(usuarioId)
                .orElseGet(() -> {
                    Usuarios usuario = usuarioRepository.findById(usuarioId)
                            .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
                    return crearCarrito(usuario);
                });
    }
}