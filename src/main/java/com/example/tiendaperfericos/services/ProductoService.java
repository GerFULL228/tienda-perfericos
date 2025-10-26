package com.example.tiendaperfericos.services;

import com.example.tiendaperfericos.entity.Producto;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface ProductoService {
    List<Producto> findAll();
    Optional<Producto> findById(Long id);
    Producto save(Producto producto);
    void deleteById(Long id);
    List<Producto> findByCategoria(Long categoriaId);
    List<Producto> findByCategoriaNombre(String categoriaNombre);
    List<Producto> findByPrecioBetween(BigDecimal precioMin, BigDecimal precioMax);
    List<Producto> findByCategoriaAndPrecioBetween(Long categoriaId, BigDecimal precioMin, BigDecimal precioMax);
    List<Producto> buscarPorTermino(String termino);
    List<Producto> findByNombreContaining(String nombre);
    List<Producto> findProductosConStock();
    List<Producto> findAllOrderByPrecioAsc();
    List<Producto> findAllOrderByPrecioDesc();
    void actualizarStock(Long productoId, Integer cantidad);
    Long countProductosActivos();
    List<Producto> findProductosDestacados();
}