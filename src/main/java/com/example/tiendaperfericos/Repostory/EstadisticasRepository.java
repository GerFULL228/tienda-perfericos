package com.example.tiendaperfericos.Repostory;

import com.example.tiendaperfericos.entity.Pedido;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EstadisticasRepository extends JpaRepository<Pedido, Long> { // ✅ EXTENDER JpaRepository

    @Query(value = "SELECT MONTH(p.fecha_pedido) as mes, " +
            "COALESCE(SUM(p.total), 0) as total_ventas, " +
            "COUNT(p.id) as total_pedidos " +
            "FROM pedidos p " +
            "WHERE YEAR(p.fecha_pedido) = :year AND p.estado = 'ENTREGADO' " +
            "GROUP BY MONTH(p.fecha_pedido) " +
            "ORDER BY mes", nativeQuery = true)
    List<Object[]> getVentasMensuales(@Param("year") int year);

    @Query("SELECT c.nombre, COUNT(p) as cantidad " +
            "FROM Producto p JOIN p.categoria c " +
            "WHERE p.activo = true " +
            "GROUP BY c.nombre")
    List<Object[]> getProductosPorCategoria();

    @Query("SELECT p.nombre, SUM(d.cantidad) as totalVendido " +
            "FROM DetallePedido d JOIN d.producto p " +
            "GROUP BY p.nombre " +
            "ORDER BY totalVendido DESC " +
            "LIMIT 10")
    List<Object[]> getTop10ProductosMasVendidos();
}