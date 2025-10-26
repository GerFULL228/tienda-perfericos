package com.example.tiendaperfericos.Repostory;



import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public interface EstadisticasRepository {

    @Query("SELECT MONTH(p.fechaPedido) as mes, SUM(p.total) as total " +
            "FROM Pedido p WHERE YEAR(p.fechaPedido) = :year AND p.estado = 'ENTREGADO' " +
            "GROUP BY MONTH(p.fechaPedido) ORDER BY mes")
    List<Object[]> getVentasMensuales(@Param("year") int year);

    @Query("SELECT c.nombre, COUNT(p) as cantidad " +
            "FROM Producto p JOIN p.categoria c " +
            "WHERE p.activo = true GROUP BY c.nombre")
    List<Object[]> getProductosPorCategoria();

    @Query("SELECT p.nombre, SUM(d.cantidad) as totalVendido " +
            "FROM DetallePedido d JOIN d.producto p " +
            "GROUP BY p.nombre ORDER BY totalVendido DESC LIMIT 10")
    List<Object[]> getTop10ProductosMasVendidos();
}