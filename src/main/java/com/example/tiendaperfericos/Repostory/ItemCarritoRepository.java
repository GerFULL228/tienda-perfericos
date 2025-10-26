package com.example.tiendaperfericos.Repostory;

import com.example.tiendaperfericos.entity.Carrito;
import com.example.tiendaperfericos.entity.ItemCarrito;
import com.example.tiendaperfericos.entity.Producto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ItemCarritoRepository extends JpaRepository<ItemCarrito, Long> {

    List<ItemCarrito> findByCarrito(Carrito carrito);

    Optional<ItemCarrito> findByCarritoAndProducto(Carrito carrito, Producto producto);

    @Query("SELECT i FROM ItemCarrito i WHERE i.carrito.id = :carritoId")
    List<ItemCarrito> findByCarritoId(@Param("carritoId") Long carritoId);

    @Query("SELECT i FROM ItemCarrito i WHERE i.carrito.usuario.id = :usuarioId")
    List<ItemCarrito> findByUsuarioId(@Param("usuarioId") Long usuarioId);

    @Modifying
    @Query("DELETE FROM ItemCarrito i WHERE i.carrito = :carrito AND i.producto = :producto")
    void deleteByCarritoAndProducto(@Param("carrito") Carrito carrito, @Param("producto") Producto producto);

    @Modifying
    @Query("DELETE FROM ItemCarrito i WHERE i.carrito.id = :carritoId")
    void deleteByCarritoId(@Param("carritoId") Long carritoId);

    @Modifying
    @Query("DELETE FROM ItemCarrito i WHERE i.carrito.usuario.id = :usuarioId")
    void deleteByUsuarioId(@Param("usuarioId") Long usuarioId);

    @Query("SELECT SUM(i.cantidad) FROM ItemCarrito i WHERE i.carrito.usuario.id = :usuarioId")
    Integer countItemsByUsuarioId(@Param("usuarioId") Long usuarioId);

    @Query("SELECT COUNT(i) FROM ItemCarrito i WHERE i.carrito.id = :carritoId")
    Integer countItemsByCarritoId(@Param("carritoId") Long carritoId);
}
