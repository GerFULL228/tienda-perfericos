package com.example.tiendaperfericos.controllers;

import com.example.tiendaperfericos.entity.Carrito;
import com.example.tiendaperfericos.entity.ItemCarrito;
import com.example.tiendaperfericos.services.implement.AuthServiceImpl;
import com.example.tiendaperfericos.services.implement.CarritoServiceImpl;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/carrito")
@RequiredArgsConstructor
@Slf4j
public class CarritoController {

    private final CarritoServiceImpl carritoService;
    private final AuthServiceImpl authService;

    @GetMapping
    public String verCarrito(Model model) {
        try {
            Long usuarioId = authService.getUsuarioAutenticadoId();
            if (usuarioId == null) {
                return "redirect:/auth/login";
            }

            List<ItemCarrito> items = carritoService.obtenerItemsCarrito(usuarioId);
            Carrito carrito = carritoService.findByUsuarioId(usuarioId).orElse(null);

            model.addAttribute("items", items);
            model.addAttribute("carrito", carrito);
            model.addAttribute("totalItems", carritoService.contarItems(usuarioId));

            return "user/carrito/carrito";
        } catch (Exception e) {
            log.error("Error al cargar carrito: {}", e.getMessage());
            model.addAttribute("error", "Error al cargar el carrito");
            return "user/carrito/carrito";
        }
    }

    @PostMapping("/agregar")
    public String agregarAlCarrito(@RequestParam Long productoId,
                                   @RequestParam Integer cantidad,
                                   RedirectAttributes redirectAttributes) {
        try {
            Long usuarioId = authService.getUsuarioAutenticadoId();
            if (usuarioId == null) {
                return "redirect:/auth/login";
            }

            carritoService.agregarProducto(usuarioId, productoId, cantidad);

            redirectAttributes.addFlashAttribute("mensaje",
                    cantidad + " producto(s) agregado(s) al carrito exitosamente");

        } catch (Exception e) {
            log.error("Error al agregar producto al carrito: {}", e.getMessage());
            redirectAttributes.addFlashAttribute("error",
                    "Error al agregar producto: " + e.getMessage());
        }


        return "redirect:/productos/detalle/" + productoId;
    }

    @PostMapping("/actualizar")
    public String actualizarCantidad(@RequestParam Long productoId,
                                     @RequestParam Integer cantidad,
                                     RedirectAttributes redirectAttributes) {
        try {
            Long usuarioId = authService.getUsuarioAutenticadoId();
            carritoService.actualizarCantidad(usuarioId, productoId, cantidad);
            redirectAttributes.addFlashAttribute("mensaje", "Carrito actualizado");
        } catch (Exception e) {
            log.error("Error al actualizar carrito: {}", e.getMessage());
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }

        return "redirect:/carrito";
    }

    @PostMapping("/eliminar")
    public String eliminarDelCarrito(@RequestParam Long productoId,
                                     RedirectAttributes redirectAttributes) {
        try {
            Long usuarioId = authService.getUsuarioAutenticadoId();
            carritoService.eliminarProducto(usuarioId, productoId);
            redirectAttributes.addFlashAttribute("mensaje", "Producto eliminado del carrito"); 
        } catch (Exception e) {
            log.error("Error al eliminar producto del carrito: {}", e.getMessage());
            redirectAttributes.addFlashAttribute("error", "Error al eliminar producto"); 
        }

        return "redirect:/carrito";
    }

    @PostMapping("/limpiar")
    public String limpiarCarrito(RedirectAttributes redirectAttributes) {
        try {
            Long usuarioId = authService.getUsuarioAutenticadoId();
            carritoService.limpiarCarrito(usuarioId);
            redirectAttributes.addFlashAttribute("mensaje", "Carrito limpiado");
        } catch (Exception e) {
            log.error("Error al limpiar carrito: {}", e.getMessage());
            redirectAttributes.addFlashAttribute("error", "Error al limpiar carrito");
        }

        return "redirect:/carrito";
    }

    @GetMapping("/contador")
    @ResponseBody
    public String obtenerContadorCarrito() {
        try {
            Long usuarioId = authService.getUsuarioAutenticadoId();
            if (usuarioId == null) {
                return "0";
            }
            Integer totalItems = carritoService.contarItems(usuarioId);
            return totalItems != null ? totalItems.toString() : "0";
        } catch (Exception e) {
            log.error("Error al obtener contador del carrito: {}", e.getMessage());
            return "0";
        }
    }
}