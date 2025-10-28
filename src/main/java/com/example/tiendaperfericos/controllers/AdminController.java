package com.example.tiendaperfericos.controllers;


import com.example.tiendaperfericos.entity.*;
import com.example.tiendaperfericos.entity.emun.EstadoPedido;
import com.example.tiendaperfericos.services.implement.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.util.List;

@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
@Slf4j
public class AdminController {

    private final ProductoServiceImpl productoService;
    private final CategoriaServiceImpl categoriaService;
    private final UsuarioServiceImpl usuarioService;
    private final PedidoServiceImpl pedidoService;
    private final EstadisticasServiceImpl estadisticasService;

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        try {

            Long totalProductos = productoService.countProductosActivos();
            Long totalUsuarios = usuarioService.countByRol("USER");
            Long totalPedidos = pedidoService.countByEstado(EstadoPedido.ENTREGADO);
            BigDecimal ingresosTotales = pedidoService.getIngresosTotales(
                    java.time.LocalDateTime.now().minusMonths(1),
                    java.time.LocalDateTime.now()
            );

            model.addAttribute("totalProductos", totalProductos);
            model.addAttribute("totalUsuarios", totalUsuarios);
            model.addAttribute("totalPedidos", totalPedidos);
            model.addAttribute("ingresosTotales", ingresosTotales != null ? ingresosTotales : BigDecimal.ZERO);
            model.addAttribute("pedidosRecientes", pedidoService.findPedidosRecientes(7));

            return "admin/dashboard";
        } catch (Exception e) {
            log.error("Error en dashboard admin: {}", e.getMessage());
            model.addAttribute("error", "Error al cargar el dashboard");
            return "admin/dashboard";
        }
    }


    @GetMapping("/productos")
    public String listarProductos(Model model) {
        List<Producto> productos = productoService.findAll();
        List<Categoria> categorias = categoriaService.findAll();

        model.addAttribute("productos", productos);
        model.addAttribute("categorias", categorias);
        model.addAttribute("producto", new Producto());

        return "admin/productos/lista";
    }


    @PostMapping("/productos/guardar")
    public String guardarProducto(@ModelAttribute Producto producto,
                                  RedirectAttributes redirectAttributes) { // ✅ BIEN

        try {
            productoService.save(producto);
            redirectAttributes.addFlashAttribute("mensaje", "Producto guardado exitosamente");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error al guardar producto");
        }
        return "redirect:/admin/productos";
    }

    @GetMapping("/productos/editar/{id}")
    public String editarProducto(@PathVariable Long id, Model model) {
        try {
            Producto producto = productoService.findById(id)
                    .orElseThrow(() -> new RuntimeException("Producto no encontrado"));
            List<Categoria> categorias = categoriaService.findAll();

            model.addAttribute("producto", producto);
            model.addAttribute("categorias", categorias);

            return "admin/productos/editar";
        } catch (Exception e) {
            log.error("Error al cargar producto para editar: {}", e.getMessage());
            return "redirect:/admin/productos";
        }
    }

    @PostMapping("/productos/actualizar/{id}")
    public String actualizarProducto(@PathVariable Long id,
                                     @ModelAttribute Producto producto,
                                     RedirectAttributes redirectAttributes) {
        try {
            producto.setId(id);
            productoService.save(producto);
            redirectAttributes.addAttribute("mensaje", "Producto actualizado exitosamente");
        } catch (Exception e) {
            log.error("Error al actualizar producto: {}", e.getMessage());
            redirectAttributes.addAttribute("error", "Error al actualizar producto");
        }
        return "redirect:/admin/productos";
    }

    @PostMapping("/productos/eliminar/{id}")
    public String eliminarProducto(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            productoService.deleteById(id);
            redirectAttributes.addAttribute("mensaje", "Producto eliminado exitosamente");
        } catch (Exception e) {
            log.error("Error al eliminar producto: {}", e.getMessage());
            redirectAttributes.addAttribute("error", "Error al eliminar producto");
        }
        return "redirect:/admin/productos";
    }


    @GetMapping("/categorias")
    public String listarCategorias(Model model) {
        List<Categoria> categorias = categoriaService.findAllWithInactivas();
        model.addAttribute("categorias", categorias);
        model.addAttribute("categoria", new Categoria());
        return "admin/categorias/lista";
    }

    @PostMapping("/categorias/guardar")
    public String guardarCategoria(@ModelAttribute Categoria categoria,
                                   RedirectAttributes redirectAttributes) {
        try {
            categoriaService.save(categoria);
            redirectAttributes.addAttribute("mensaje", "Categoría guardada exitosamente");
        } catch (Exception e) {
            log.error("Error al guardar categoría: {}", e.getMessage());
            redirectAttributes.addAttribute("error", "Error al guardar categoría");
        }
        return "redirect:/admin/categorias";
    }

    @PostMapping("/categorias/eliminar/{id}")
    public String eliminarCategoria(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            categoriaService.deleteById(id);
            redirectAttributes.addAttribute("mensaje", "Categoría eliminada exitosamente");
        } catch (Exception e) {
            log.error("Error al eliminar categoría: {}", e.getMessage());
            redirectAttributes.addAttribute("error", "Error al eliminar categoría");
        }
        return "redirect:/admin/categorias";
    }


    @GetMapping("/pedidos")
    public String listarPedidos(Model model) {
        List<Pedido> pedidos = pedidoService.findAll();
        model.addAttribute("pedidos", pedidos);
        return "admin/pedidos/lista";
    }

    @GetMapping("/pedidos/detalle/{id}")
    public String verDetallePedido(@PathVariable Long id, Model model) {
        try {
            Pedido pedido = pedidoService.findById(id)
                    .orElseThrow(() -> new RuntimeException("Pedido no encontrado"));
            List<DetallePedido> detalles = pedidoService.obtenerDetallesPedido(id);

            model.addAttribute("pedido", pedido);
            model.addAttribute("detalles", detalles);

            return "admin/pedidos/detalle";
        } catch (Exception e) {
            log.error("Error al cargar detalle de pedido: {}", e.getMessage());
            return "redirect:/admin/pedidos";
        }
    }

    @PostMapping("/pedidos/actualizar-estado/{id}")
    public String actualizarEstadoPedido(@PathVariable Long id,
                                         @RequestParam EstadoPedido nuevoEstado,
                                         RedirectAttributes redirectAttributes) {
        try {
            pedidoService.actualizarEstado(id, nuevoEstado);
            redirectAttributes.addFlashAttribute("mensaje", "Estado del pedido actualizado exitosamente");
        } catch (Exception e) {
            log.error("Error al actualizar estado del pedido: {}", e.getMessage());
            redirectAttributes.addFlashAttribute("error", "Error al actualizar estado del pedido");
        }
        return "redirect:/admin/pedidos/detalle/" + id;
    }

    @GetMapping("/usuarios")
    public String listarUsuarios(Model model) {
        List<Usuarios> usuarios = usuarioService.findUsuariosActivos();
        model.addAttribute("usuarios", usuarios);
        return "admin/usuarios/lista";
    }

    @PostMapping("/usuarios/cambiar-estado/{id}")
    public String cambiarEstadoUsuario(@PathVariable Long id,
                                       @RequestParam boolean activo,
                                       RedirectAttributes redirectAttributes) {
        try {
            usuarioService.cambiarEstadoUsuarios(id, activo);
            String mensaje = activo ? "Usuario activado exitosamente" : "Usuario desactivado exitosamente";
            redirectAttributes.addFlashAttribute("mensaje", mensaje);
        } catch (Exception e) {
            log.error("Error al cambiar estado de usuario: {}", e.getMessage());
            redirectAttributes.addFlashAttribute("error", "Error al cambiar estado de usuario");
        }
        return "redirect:/admin/usuarios";
    }


    @GetMapping("/estadisticas")
    public String mostrarEstadisticas(Model model) {
        try {
            model.addAttribute("estadisticas", estadisticasService.obtenerEstadisticasGenerales());
            model.addAttribute("ventasMensuales", estadisticasService.getVentasMensuales(
                    java.time.LocalDate.now().getYear()
            ));
            model.addAttribute("productosPorCategoria", estadisticasService.getProductosPorCategoria());
            model.addAttribute("topProductos", estadisticasService.getTopProductosMasVendidos(10));

            return "admin/estadisticas";
        } catch (Exception e) {
            log.error("Error al cargar estadísticas: {}", e.getMessage());
            model.addAttribute("error", "Error al cargar estadísticas");
            return "admin/estadisticas";
        }
    }
}