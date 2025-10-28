package com.example.tiendaperfericos.services.implement;

import com.example.tiendaperfericos.Repostory.*;
import com.example.tiendaperfericos.entity.Pedido;
import com.example.tiendaperfericos.entity.emun.EstadoPedido;
import com.example.tiendaperfericos.services.EstadisticasService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class EstadisticasServiceImpl implements EstadisticasService {

    private final PedidoRepository pedidoRepository;
    private final ProductoRepository productoRepository;
    private final UsuarioRepository usuarioRepository;
    private final CategoriaRepository categoriaRepository;
    private final DetallePedidoRepository detallePedidoRepository;
    private final EstadisticasRepository estadisticasRepository;

    @Override
    @Transactional(readOnly = true)
    public Map<String, Object> obtenerEstadisticasGenerales() {
        Map<String, Object> estadisticas = new HashMap<>();

        try {

            Long totalProductos = productoRepository.countProductosActivos();
            estadisticas.put("totalProductos", totalProductos);


            Long totalUsuarios = usuarioRepository.countByRolNombre("USER");
            estadisticas.put("totalUsuarios", totalUsuarios);


            Long totalPedidosEntregados = pedidoRepository.countByEstado(com.example.tiendaperfericos.entity.emun.EstadoPedido.ENTREGADO);
            estadisticas.put("totalPedidosEntregados", totalPedidosEntregados);


            LocalDateTime haceUnMes = LocalDateTime.now().minusMonths(1);
            BigDecimal ingresosUltimoMes = pedidoRepository.getIngresosTotales(haceUnMes, LocalDateTime.now());
            estadisticas.put("ingresosUltimoMes", ingresosUltimoMes != null ? ingresosUltimoMes : BigDecimal.ZERO);


            Map<String, Long> pedidosPorEstado = getPedidosPorEstado();
            estadisticas.put("pedidosPorEstado", pedidosPorEstado);


            List<com.example.tiendaperfericos.entity.Producto> productosStockBajo =
                    productoRepository.findByStockGreaterThanAndActivoTrue(0).stream()
                            .filter(p -> p.getStock() < 10)
                            .toList();
            estadisticas.put("productosStockBajo", productosStockBajo.size());

            log.info("Estadísticas generales generadas exitosamente");

        } catch (Exception e) {
            log.error("Error al obtener estadísticas generales: {}", e.getMessage());
            // Valores por defecto en caso de error
            estadisticas.put("totalProductos", 0L);
            estadisticas.put("totalUsuarios", 0L);
            estadisticas.put("totalPedidosEntregados", 0L);
            estadisticas.put("ingresosUltimoMes", BigDecimal.ZERO);
            estadisticas.put("pedidosPorEstado", new HashMap<>());
            estadisticas.put("productosStockBajo", 0);
        }

        return estadisticas;
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, Object> obtenerEstadisticasVentas(int año) {
        Map<String, Object> estadisticas = new HashMap<>();

        try {

            List<Object[]> ventasMensuales = getVentasMensuales(año);
            estadisticas.put("ventasMensuales", ventasMensuales);


            BigDecimal totalVentasAnual = ventasMensuales.stream()
                    .map(item -> (BigDecimal) item[1])
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            estadisticas.put("totalVentasAnual", totalVentasAnual);


            List<Object[]> topProductos = getTopProductosMasVendidos(10);
            estadisticas.put("topProductos", topProductos);


            List<Object[]> ventasPorCategoria = getProductosPorCategoria();
            estadisticas.put("ventasPorCategoria", ventasPorCategoria);

            log.info("Estadísticas de ventas para el año {} generadas exitosamente", año);

        } catch (Exception e) {
            log.error("Error al obtener estadísticas de ventas: {}", e.getMessage());
            estadisticas.put("ventasMensuales", List.of());
            estadisticas.put("totalVentasAnual", BigDecimal.ZERO);
            estadisticas.put("topProductos", List.of());
            estadisticas.put("ventasPorCategoria", List.of());
        }

        return estadisticas;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Object[]> getVentasMensuales(int año) {
        try {

            return estadisticasRepository.getVentasMensuales(año);
        } catch (Exception e) {
            log.error("Error al obtener ventas mensuales: {}", e.getMessage());
            return List.of();
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<Object[]> getProductosPorCategoria() {
        try {

            return estadisticasRepository.getProductosPorCategoria();
        } catch (Exception e) {
            log.error("Error al obtener productos por categoría: {}", e.getMessage());
            return List.of();
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<Object[]> getTopProductosMasVendidos(int limite) {
        try {

            List<Object[]> resultados = estadisticasRepository.getTop10ProductosMasVendidos();
            return resultados.stream().limit(limite).toList();
        } catch (Exception e) {
            log.error("Error al obtener top productos más vendidos: {}", e.getMessage());
            return List.of();
        }
    }

    @Override
    @Transactional(readOnly = true)
    public BigDecimal getIngresosTotalesPeriodo(int dias) {
        try {
            LocalDateTime fechaInicio = LocalDateTime.now().minusDays(dias);
            LocalDateTime fechaFin = LocalDateTime.now();

            BigDecimal ingresos = pedidoRepository.getIngresosTotales(fechaInicio, fechaFin);
            return ingresos != null ? ingresos : BigDecimal.ZERO;

        } catch (Exception e) {
            log.error("Error al obtener ingresos totales del período: {}", e.getMessage());
            return BigDecimal.ZERO;
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Long getTotalPedidosPeriodo(int dias) {
        try {
            LocalDateTime fechaInicio = LocalDateTime.now().minusDays(dias);
            LocalDateTime fechaFin = LocalDateTime.now();

            List<com.example.tiendaperfericos.entity.Pedido> pedidos =
                    pedidoRepository.findByFechaPedidoBetween(fechaInicio, fechaFin);

            return (long) pedidos.size();

        } catch (Exception e) {
            log.error("Error al obtener total de pedidos del período: {}", e.getMessage());
            return 0L;
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Long getTotalUsuariosRegistradosPeriodo(int dias) {
        try {
            LocalDateTime fechaInicio = LocalDateTime.now().minusDays(dias);


            return usuarioRepository.countByRolNombre("USER");

        } catch (Exception e) {
            log.error("Error al obtener total de usuarios registrados: {}", e.getMessage());
            return 0L;
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, Long> getPedidosPorEstado() {
        Map<String, Long> pedidosPorEstado = new HashMap<>();

        try {
            for (com.example.tiendaperfericos.entity.emun.EstadoPedido estado :
                    com.example.tiendaperfericos.entity.emun.EstadoPedido.values()) {

                Long cantidad = pedidoRepository.countByEstado(estado);
                pedidosPorEstado.put(estado.name(), cantidad);
            }

        } catch (Exception e) {
            log.error("Error al obtener pedidos por estado: {}", e.getMessage());

            for (com.example.tiendaperfericos.entity.emun.EstadoPedido estado :
                    com.example.tiendaperfericos.entity.emun.EstadoPedido.values()) {
                pedidosPorEstado.put(estado.name(), 0L);
            }
        }

        return pedidosPorEstado;
    }


    @Transactional(readOnly = true)
    public Map<String, BigDecimal> getIngresosMensuales(int año) {
        Map<String, BigDecimal> ingresosMensuales = new HashMap<>();

        try {
            List<Object[]> ventas = getVentasMensuales(año);
            for (Object[] venta : ventas) {
                Integer mes = (Integer) venta[0];
                BigDecimal total = (BigDecimal) venta[1];
                ingresosMensuales.put(getNombreMes(mes), total);
            }

        } catch (Exception e) {
            log.error("Error al obtener ingresos mensuales: {}", e.getMessage());
        }

        return ingresosMensuales;
    }

    @Transactional(readOnly = true)
    public Map<String, Long> getPedidosMensuales(int año) {
        Map<String, Long> pedidosMensuales = new HashMap<>();

        try {
            List<Object[]> ventas = getVentasMensuales(año);
            for (Object[] venta : ventas) {
                Integer mes = (Integer) venta[0];
                Long totalPedidos = (Long) venta[2];
                pedidosMensuales.put(getNombreMes(mes), totalPedidos);
            }

        } catch (Exception e) {
            log.error("Error al obtener pedidos mensuales: {}", e.getMessage());
        }

        return pedidosMensuales;
    }

    private String getNombreMes(int numeroMes) {
        String[] meses = {
                "Enero", "Febrero", "Marzo", "Abril", "Mayo", "Junio",
                "Julio", "Agosto", "Septiembre", "Octubre", "Noviembre", "Diciembre"
        };
        return meses[numeroMes - 1];
    }

    @Transactional(readOnly = true)
    public BigDecimal getTicketPromedio() {
        try {
            List<Pedido> pedidosEntregados =
                    pedidoRepository.findByEstado(EstadoPedido.ENTREGADO);

            if (pedidosEntregados.isEmpty()) {
                return BigDecimal.ZERO;
            }

            BigDecimal totalVentas = pedidosEntregados.stream()
                    .map(com.example.tiendaperfericos.entity.Pedido::getTotal)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            return totalVentas.divide(BigDecimal.valueOf(pedidosEntregados.size()), 2, java.math.RoundingMode.HALF_UP);

        } catch (Exception e) {
            log.error("Error al calcular ticket promedio: {}", e.getMessage());
            return BigDecimal.ZERO;
        }
    }

    @Transactional(readOnly = true)
    public Map<String, Object> getMetricasRendimiento() {
        Map<String, Object> metricas = new HashMap<>();

        try {

            Long totalUsuarios = usuarioRepository.countByRolNombre("USER");
            Long totalPedidos = pedidoRepository.count();

            double tasaConversion = totalUsuarios > 0 ?
                    (double) totalPedidos / totalUsuarios * 100 : 0;
            metricas.put("tasaConversion", Math.round(tasaConversion * 100.0) / 100.0);


            metricas.put("ticketPromedio", getTicketPromedio());


            metricas.put("topProductos", getTopProductosMasVendidos(5));


            metricas.put("categoriasPopulares", getProductosPorCategoria());

        } catch (Exception e) {
            log.error("Error al obtener métricas de rendimiento: {}", e.getMessage());
            metricas.put("tasaConversion", 0.0);
            metricas.put("ticketPromedio", BigDecimal.ZERO);
            metricas.put("topProductos", List.of());
            metricas.put("categoriasPopulares", List.of());
        }

        return metricas;
    }
}