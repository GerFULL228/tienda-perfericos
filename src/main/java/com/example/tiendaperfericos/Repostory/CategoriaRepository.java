package com.example.tiendaperfericos.Repostory;

import com.example.tiendaperfericos.entity.Categoria;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CategoriaRepository extends JpaRepository<Categoria, Long> {

    List<Categoria> findByActivaTrue();

    Optional<Categoria> findByNombre(String nombre);

    Boolean existsByNombre(String nombre);

    @Query("SELECT c FROM Categoria c WHERE c.activa = true ORDER BY c.nombre ASC")
    List<Categoria> findCategoriasActivasOrdenadas();

    @Query("SELECT COUNT(p) FROM Producto p WHERE p.categoria.id = :categoriaId AND p.activo = true")
    Long countProductosActivosByCategoriaId(Long categoriaId);


    @Query("SELECT c.nombre, COUNT(p) as cantidadProductos, SUM(p.stock) as totalStock " +
            "FROM Categoria c LEFT JOIN c.productos p " +
            "WHERE c.activa = true AND (p.activo = true OR p IS NULL) " +
            "GROUP BY c.id, c.nombre " +
            "ORDER BY cantidadProductos DESC")
    List<Object[]> getProductosPorCategoria();
}