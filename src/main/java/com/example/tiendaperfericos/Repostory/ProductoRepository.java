package com.example.tiendaperfericos.Repostory;

import com.example.tiendaperfericos.entity.Categoria;
import com.example.tiendaperfericos.entity.Producto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface ProductoRepository extends JpaRepository<Producto, Long> {

    List<Producto> findByActivoTrue();

    List<Producto> findByCategoriaAndActivoTrue(Categoria categoria);

    List<Producto> findByCategoriaIdAndActivoTrue(Long categoriaId);

    List<Producto> findByPrecioBetweenAndActivoTrue(BigDecimal precioMin, BigDecimal precioMax);

    List<Producto> findByStockGreaterThanAndActivoTrue(Integer stock);

    List<Producto> findByNombreContainingIgnoreCaseAndActivoTrue(String nombre);

    @Query("SELECT p FROM Producto p WHERE p.activo = true AND p.categoria.activa = true")
    List<Producto> findProductosActivos();

    @Query("SELECT p FROM Producto p WHERE p.activo = true AND p.categoria.id = :categoriaId AND p.precio BETWEEN :precioMin AND :precioMax")
    List<Producto> findByCategoriaAndPrecioBetween(
            @Param("categoriaId") Long categoriaId,
            @Param("precioMin") BigDecimal precioMin,
            @Param("precioMax") BigDecimal precioMax);

    @Query("SELECT p FROM Producto p WHERE p.activo = true AND p.precio BETWEEN :precioMin AND :precioMax")
    List<Producto> findByPrecioBetween(@Param("precioMin") BigDecimal precioMin,
                                       @Param("precioMax") BigDecimal precioMax);

    @Query("SELECT p FROM Producto p WHERE p.activo = true AND p.categoria.nombre = :categoriaNombre")
    List<Producto> findByCategoriaNombre(@Param("categoriaNombre") String categoriaNombre);

    @Query("SELECT p FROM Producto p WHERE p.activo = true AND p.nombre LIKE %:termino% OR p.descripcion LIKE %:termino%")
    List<Producto> buscarPorTermino(@Param("termino") String termino);

    @Query("SELECT COUNT(p) FROM Producto p WHERE p.activo = true")
    Long countProductosActivos();

    @Query("SELECT p FROM Producto p WHERE p.activo = true ORDER BY p.precio ASC")
    List<Producto> findAllOrderByPrecioAsc();

    @Query("SELECT p FROM Producto p WHERE p.activo = true ORDER BY p.precio DESC")
    List<Producto> findAllOrderByPrecioDesc();

    Optional<Producto> findByIdAndActivoTrue(Long id);
}