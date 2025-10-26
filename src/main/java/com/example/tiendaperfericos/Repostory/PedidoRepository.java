package com.example.tiendaperfericos.Repostory;



import com.example.tiendaperfericos.entity.Pedido;
import com.example.tiendaperfericos.entity.Usuarios;
import com.example.tiendaperfericos.entity.emun.EstadoPedido;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface PedidoRepository extends JpaRepository<Pedido, Long> {

    List<Pedido> findByUsuario(Usuarios usuario);

    List<Pedido> findByUsuarioId(Long usuarioId);

    List<Pedido> findByEstado(EstadoPedido estado);



    List<Pedido> findByUsuarioOrderByFechaPedidoDesc(Usuarios usuario);
    @Query("SELECT SUM(p.total) FROM Pedido p WHERE p.estado = 'ENTREGADO' AND p.fechaPedido BETWEEN :fechaInicio AND :fechaFin")
    BigDecimal getIngresosTotales(@Param("fechaInicio") LocalDateTime fechaInicio,
                                  @Param("fechaFin") LocalDateTime fechaFin);

    List<Pedido> findByFechaPedidoBetween(LocalDateTime fechaInicio, LocalDateTime fechaFin);


    @Query("SELECT p FROM Pedido p WHERE p.usuario.id = :usuarioId ORDER BY p.fechaPedido DESC")
    List<Pedido> findPedidosRecientesByUsuarioId(@Param("usuarioId") Long usuarioId);

    @Query("SELECT p FROM Pedido p ORDER BY p.fechaPedido DESC")
    List<Pedido> findAllOrderByFechaDesc();

    @Query("SELECT p FROM Pedido p WHERE p.estado IN :estados ORDER BY p.fechaPedido DESC")
    List<Pedido> findByEstadoIn(@Param("estados") List<EstadoPedido> estados);

    @Query("SELECT COUNT(p) FROM Pedido p WHERE p.estado = :estado")
    Long countByEstado(@Param("estado") EstadoPedido estado);


    @Query("SELECT p FROM Pedido p WHERE p.fechaPedido >= :fecha")
    List<Pedido> findPedidosRecientes(@Param("fecha") LocalDateTime fecha);

    @Query("SELECT p FROM Pedido p WHERE p.usuario.email = :email ORDER BY p.fechaPedido DESC")
    List<Pedido> findByUsuarioEmail(@Param("email") String email);
}