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
import java.util.List;

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
                         Model model) {
        try {
            List<Producto> productos;

            if (busqueda != null && !busqueda.trim().isEmpty()) {

                productos = productoService.buscarPorTermino(busqueda.trim());
            } else if (categoriaId != null && precioMin != null && precioMax != null) {

                productos = productoService.findByCategoriaAndPrecioBetween(categoriaId, precioMin, precioMax);
            } else if (categoriaId != null) {

                productos = productoService.findByCategoria(categoriaId);
            } else if (precioMin != null && precioMax != null) {

                productos = productoService.findByPrecioBetween(precioMin, precioMax);
            } else {

                productos = productoService.findAll();
            }

            model.addAttribute("productos", productos);
            model.addAttribute("categorias", categoriaService.findAll());
            model.addAttribute("categoriaId", categoriaId);
            model.addAttribute("precioMin", precioMin);
            model.addAttribute("precioMax", precioMax);
            model.addAttribute("busqueda", busqueda);

            return "user/productos/tienda";
        } catch (Exception e) {
            log.error("Error al cargar tienda: {}", e.getMessage());
            model.addAttribute("error", "Error al cargar los productos");
            return "user/productos/tienda";
        }
    }

    @GetMapping("/detalle/{id}")
    public String detalleProducto(@PathVariable Long id, Model model) {
        try {
            Producto producto = productoService.findById(id)
                    .orElseThrow(() -> new RuntimeException("Producto no encontrado"));

            model.addAttribute("producto", producto);
            model.addAttribute("productosRelacionados", productoService.findByCategoria(
                    producto.getCategoria().getId()
            ).stream().limit(4).toList());

            return "user/productos/detalle";
        } catch (Exception e) {
            log.error("Error al cargar detalle de producto: {}", e.getMessage());
            return "redirect:/productos/tienda";
        }
    }

    @GetMapping("/buscar")
    public String buscarProductos(@RequestParam String q, Model model) {
        try {
            List<Producto> productos = productoService.buscarPorTermino(q);
            model.addAttribute("productos", productos);
            model.addAttribute("busqueda", q);
            model.addAttribute("categorias", categoriaService.findAll());
            model.addAttribute("resultados", productos.size());

            return "user/productos/tienda";
        } catch (Exception e) {
            log.error("Error en búsqueda de productos: {}", e.getMessage());
            model.addAttribute("error", "Error en la búsqueda");
            return "user/productos/tienda";
        }
    }

    @GetMapping("/filtrar")
    public String filtrarProductos(@RequestParam(required = false) Long categoriaId,
                                   @RequestParam(required = false) BigDecimal precioMin,
                                   @RequestParam(required = false) BigDecimal precioMax,
                                   @RequestParam(required = false) String orden,
                                   Model model) {
        try {
            List<Producto> productos;

            if (categoriaId != null && precioMin != null && precioMax != null) {
                productos = productoService.findByCategoriaAndPrecioBetween(categoriaId, precioMin, precioMax);
            } else if (categoriaId != null) {
                productos = productoService.findByCategoria(categoriaId);
            } else if (precioMin != null && precioMax != null) {
                productos = productoService.findByPrecioBetween(precioMin, precioMax);
            } else {
                productos = productoService.findAll();
            }


            if ("precio-asc".equals(orden)) {
                productos = productos.stream()
                        .sorted((p1, p2) -> p1.getPrecio().compareTo(p2.getPrecio()))
                        .toList();
            } else if ("precio-desc".equals(orden)) {
                productos = productos.stream()
                        .sorted((p1, p2) -> p2.getPrecio().compareTo(p1.getPrecio()))
                        .toList();
            }

            model.addAttribute("productos", productos);
            model.addAttribute("categorias", categoriaService.findAll());
            model.addAttribute("categoriaId", categoriaId);
            model.addAttribute("precioMin", precioMin);
            model.addAttribute("precioMax", precioMax);
            model.addAttribute("orden", orden);

            return "user/productos/tienda";
        } catch (Exception e) {
            log.error("Error al filtrar productos: {}", e.getMessage());
            model.addAttribute("error", "Error al filtrar productos");
            return "user/productos/tienda";
        }
    }
}