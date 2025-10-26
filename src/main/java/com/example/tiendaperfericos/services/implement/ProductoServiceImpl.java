package com.example.tiendaperfericos.services.implement;


import com.example.tiendaperfericos.Repostory.CategoriaRepository;
import com.example.tiendaperfericos.Repostory.ProductoRepository;
import com.example.tiendaperfericos.entity.Categoria;
import com.example.tiendaperfericos.entity.Producto;
import com.example.tiendaperfericos.services.ProductoService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductoServiceImpl implements ProductoService {

    private final ProductoRepository productoRepository;
    private final CategoriaRepository categoriaRepository;

    @Override
    @Transactional(readOnly = true)
    public List<Producto> findAll() {
        return productoRepository.findProductosActivos();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Producto> findById(Long id) {
        return productoRepository.findByIdAndActivoTrue(id);
    }

    @Override
    @Transactional
    public Producto save(Producto producto) {
        if (producto.getCategoria() != null && producto.getCategoria().getId() != null) {
            Categoria categoria = categoriaRepository.findById(producto.getCategoria().getId())
                    .orElseThrow(() -> new RuntimeException("Categoría no encontrada"));
            producto.setCategoria(categoria);
        }
        return productoRepository.save(producto);
    }

    @Override
    @Transactional
    public void deleteById(Long id) {
        productoRepository.findById(id).ifPresent(producto -> {
            producto.setActivo(false);
            productoRepository.save(producto);
        });
    }

    @Override
    @Transactional(readOnly = true)
    public List<Producto> findByCategoria(Long categoriaId) {
        return productoRepository.findByCategoriaIdAndActivoTrue(categoriaId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Producto> findByCategoriaNombre(String categoriaNombre) {
        return productoRepository.findByCategoriaNombre(categoriaNombre);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Producto> findByPrecioBetween(BigDecimal precioMin, BigDecimal precioMax) {
        return productoRepository.findByPrecioBetween(precioMin, precioMax);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Producto> findByCategoriaAndPrecioBetween(Long categoriaId, BigDecimal precioMin, BigDecimal precioMax) {
        return productoRepository.findByCategoriaAndPrecioBetween(categoriaId, precioMin, precioMax);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Producto> buscarPorTermino(String termino) {
        return productoRepository.buscarPorTermino(termino);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Producto> findByNombreContaining(String nombre) {
        return productoRepository.findByNombreContainingIgnoreCaseAndActivoTrue(nombre);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Producto> findProductosConStock() {
        return productoRepository.findByStockGreaterThanAndActivoTrue(0);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Producto> findAllOrderByPrecioAsc() {
        return productoRepository.findAllOrderByPrecioAsc();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Producto> findAllOrderByPrecioDesc() {
        return productoRepository.findAllOrderByPrecioDesc();
    }

    @Override
    @Transactional
    public void actualizarStock(Long productoId, Integer cantidad) {
        productoRepository.findById(productoId).ifPresent(producto -> {
            int nuevoStock = producto.getStock() - cantidad;
            if (nuevoStock < 0) {
                throw new RuntimeException("Stock insuficiente para el producto: " + producto.getNombre());
            }
            producto.setStock(nuevoStock);
            productoRepository.save(producto);
            log.info("Stock actualizado para producto {}: {} unidades", producto.getNombre(), nuevoStock);
        });
    }

    @Override
    @Transactional(readOnly = true)
    public Long countProductosActivos() {
        return productoRepository.countProductosActivos();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Producto> findProductosDestacados() {
        return productoRepository.findByActivoTrue().stream()
                .filter(p -> p.getStock() > 0)
                .limit(8)
                .toList();
    }
}