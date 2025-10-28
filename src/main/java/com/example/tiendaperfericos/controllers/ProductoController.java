package com.example.tiendaperfericos.controllers;

import com.example.tiendaperfericos.entity.Producto;
import com.example.tiendaperfericos.services.implement.CategoriaServiceImpl;
import com.example.tiendaperfericos.services.implement.ProductoServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/productos")
@RequiredArgsConstructor
@Slf4j
public class ProductoController {

    private final ProductoServiceImpl productoService;
    private final CategoriaServiceImpl categoriaService;


    @GetMapping("/tienda")
    public String tienda(@RequestParam(required = false) Long categoriaId,
                         @RequestParam(required = false) BigDecimal precioMin,
                         @RequestParam(required = false) BigDecimal precioMax,
                         @RequestParam(required = false) String busqueda,
                         @RequestParam(required = false) String orden,
                         Model model) {

        log.info("Parámetros recibidos - categoriaId: {}, precioMin: {}, precioMax: {}, busqueda: {}, orden: {}",
                categoriaId, precioMin, precioMax, busqueda, orden);

        try {
            List<Producto> productos;


            if (busqueda != null && !busqueda.trim().isEmpty()) {
                productos = productoService.buscarPorTermino(busqueda.trim());
                log.info("Búsqueda: '{}' - {} resultados", busqueda, productos.size());
            } else if (categoriaId != null && precioMin != null && precioMax != null) {
                productos = productoService.findByCategoriaAndPrecioBetween(categoriaId, precioMin, precioMax);
                log.info("Filtro categoría {} y precio {}-{} - {} resultados",
                        categoriaId, precioMin, precioMax, productos.size());
            } else if (categoriaId != null) {
                productos = productoService.findByCategoria(categoriaId);
                log.info("Filtro categoría {} - {} resultados", categoriaId, productos.size());
            } else if (precioMin != null && precioMax != null) {
                productos = productoService.findByPrecioBetween(precioMin, precioMax);
                log.info("Filtro precio {}-{} - {} resultados", precioMin, precioMax, productos.size());
            } else {
                productos = productoService.findAll();
                log.info("Sin filtros - {} productos", productos.size());
            }


            productos = aplicarOrdenamiento(productos, orden);

            model.addAttribute("title", "Tienda - PeriTech");
            model.addAttribute("productos", productos);
            model.addAttribute("categorias", categoriaService.findAll());
            model.addAttribute("categoriaId", categoriaId);
            model.addAttribute("precioMin", precioMin);
            model.addAttribute("precioMax", precioMax);
            model.addAttribute("busqueda", busqueda);
            model.addAttribute("orden", orden);
            model.addAttribute("resultados", productos.size());

            return "user/productos/tienda";
        } catch (Exception e) {
            log.error("Error al cargar tienda: {}", e.getMessage(), e);
            model.addAttribute("title", "Tienda - PeriTech");
            model.addAttribute("error", "Error al cargar los productos: " + e.getMessage());
            return "user/productos/tienda";
        }
    }

    @GetMapping("/detalle/{id}")
    public String detalleProducto(@PathVariable Long id, Model model) {
        try {
            Producto producto = productoService.findById(id)
                    .orElseThrow(() -> new RuntimeException("Producto no encontrado"));


            List<Producto> productosRelacionados = productoService.findByCategoria(
                            producto.getCategoria().getId()
                    ).stream()
                    .filter(p -> !p.getId().equals(producto.getId()))
                    .limit(4)
                    .collect(Collectors.toList());

            model.addAttribute("title", producto.getNombre() + " - PeriTech");
            model.addAttribute("producto", producto);
            model.addAttribute("productosRelacionados", productosRelacionados);

            return "user/productos/detalle";
        } catch (Exception e) {
            log.error("Error al cargar detalle de producto: {}", e.getMessage(), e);
            return "redirect:/productos/tienda";
        }
    }




    private List<Producto> aplicarOrdenamiento(List<Producto> productos, String orden) {
        if (orden == null || orden.isEmpty()) {
            return productos;
        }

        return productos.stream()
                .sorted((p1, p2) -> {
                    switch (orden) {
                        case "precio-asc":
                            return p1.getPrecio().compareTo(p2.getPrecio());
                        case "precio-desc":
                            return p2.getPrecio().compareTo(p1.getPrecio());
                        case "nombre-asc":
                            return p1.getNombre().compareToIgnoreCase(p2.getNombre());
                        case "nombre-desc":
                            return p2.getNombre().compareToIgnoreCase(p1.getNombre());
                        default:
                            return 0;
                    }
                })
                .collect(Collectors.toList());
    }
}