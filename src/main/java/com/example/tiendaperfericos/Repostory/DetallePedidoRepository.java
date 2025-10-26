package com.example.tiendaperfericos.Repostory;




import com.example.tiendaperfericos.entity.DetallePedido;
import com.example.tiendaperfericos.entity.Pedido;
import com.example.tiendaperfericos.entity.Producto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DetallePedidoRepository extends JpaRepository<DetallePedido, Long> {

    List<DetallePedido> findByPedido(Pedido pedido);

    List<DetallePedido> findByProducto(Producto producto);

    @Query("SELECT d FROM DetallePedido d WHERE d.pedido.id = :pedidoId")
    List<DetallePedido> findByPedidoId(@Param("pedidoId") Long pedidoId);

    @Query("SELECT d FROM DetallePedido d WHERE d.producto.id = :productoId")
    List<DetallePedido> findByProductoId(@Param("productoId") Long productoId);

    @Query("SELECT SUM(d.cantidad) FROM DetallePedido d WHERE d.producto.id = :productoId")
    Integer getTotalVendidoByProductoId(@Param("productoId") Long productoId);

    @Query("SELECT d.producto, SUM(d.cantidad) as totalVendido FROM DetallePedido d GROUP BY d.producto ORDER BY totalVendido DESC")
    List<Object[]> findProductosMasVendidos();

    @Query("SELECT d FROM DetallePedido d WHERE d.pedido IN (SELECT p FROM Pedido p WHERE p.usuario.id = :usuarioId)")
    List<DetallePedido> findByUsuarioId(@Param("usuarioId") Long usuarioId);
}