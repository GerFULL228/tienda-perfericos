package com.example.tiendaperfericos.services;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public interface EstadisticasService {
    Map<String, Object> obtenerEstadisticasGenerales();

    Map<String, Object> obtenerEstadisticasVentas(int año);

    List<Object[]> getVentasMensuales(int año);

    List<Object[]> getProductosPorCategoria();

    List<Object[]> getTopProductosMasVendidos(int limite);

    BigDecimal getIngresosTotalesPeriodo(int dias);

    Long getTotalPedidosPeriodo(int dias);

    Long getTotalUsuariosRegistradosPeriodo(int dias);

    Map<String, Long> getPedidosPorEstado();
}