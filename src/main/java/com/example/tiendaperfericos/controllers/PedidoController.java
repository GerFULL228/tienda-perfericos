package com.example.tiendaperfericos.controllers;

import com.example.tiendaperfericos.entity.DetallePedido;
import com.example.tiendaperfericos.entity.ItemCarrito;
import com.example.tiendaperfericos.entity.Pedido;
import com.example.tiendaperfericos.entity.Usuarios;
import com.example.tiendaperfericos.services.implement.AuthServiceImpl;
import com.example.tiendaperfericos.services.implement.CarritoServiceImpl;
import com.example.tiendaperfericos.services.implement.PedidoServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/pedidos")
@RequiredArgsConstructor
@Slf4j
public class PedidoController {

    private final PedidoServiceImpl pedidoService;
    private final AuthServiceImpl authService;
    private final CarritoServiceImpl carritoService;

    @GetMapping("/historial")
    public String historialPedidos(Model model) {
        try {
            Long usuarioId = authService.getUsuarioAutenticadoId();
            if (usuarioId == null) {
                return "redirect:/auth/login";
            }

            List<Pedido> pedidos = pedidoService.findByUsuarioId(usuarioId);
            model.addAttribute("pedidos", pedidos);

            return "user/pedidos/historial";
        } catch (Exception e) {
            log.error("Error al cargar historial de pedidos: {}", e.getMessage());
            model.addAttribute("error", "Error al cargar el historial de pedidos");
            return "user/pedidos/historial";
        }
    }

    @GetMapping("/detalle/{id}")
    public String detallePedido(@PathVariable Long id, Model model) {
        try {
            Long usuarioId = authService.getUsuarioAutenticadoId();
            Pedido pedido = pedidoService.findById(id)
                    .orElseThrow(() -> new RuntimeException("Pedido no encontrado"));


            if (!pedido.getUsuario().getId().equals(usuarioId)) {
                return "redirect:/auth/acceso-denegado";
            }

            List<DetallePedido> detalles = pedidoService.obtenerDetallesPedido(id);

            model.addAttribute("pedido", pedido);
            model.addAttribute("detalles", detalles);

            return "user/pedidos/detalle";
        } catch (Exception e) {
            log.error("Error al cargar detalle de pedido: {}", e.getMessage());
            return "redirect:/pedidos/historial";
        }
    }

    @PostMapping("/crear")
    public String crearPedido(@RequestParam String direccionEntrega,
                              @RequestParam String telefonoContacto,
                              RedirectAttributes redirectAttributes) { 
        try {
            Long usuarioId = authService.getUsuarioAutenticadoId();
            if (usuarioId == null) {
                return "redirect:/auth/login";
            }

            Pedido pedido = pedidoService.crearPedidoDesdeCarrito(usuarioId, direccionEntrega, telefonoContacto);
            redirectAttributes.addFlashAttribute("mensaje", "Pedido creado exitosamente. Número de pedido: " + pedido.getId()); 

            return "redirect:/pedidos/detalle/" + pedido.getId();
        } catch (Exception e) {
            log.error("Error al crear pedido: {}", e.getMessage());
            redirectAttributes.addFlashAttribute("error", e.getMessage()); 
            return "redirect:/carrito";
        }
    }

    @PostMapping("/cancelar/{id}")
    public String cancelarPedido(@PathVariable Long id, RedirectAttributes redirectAttributes) { 
        try {
            Long usuarioId = authService.getUsuarioAutenticadoId();
            Pedido pedido = pedidoService.findById(id)
                    .orElseThrow(() -> new RuntimeException("Pedido no encontrado"));

            if (!pedido.getUsuario().getId().equals(usuarioId)) {
                return "redirect:/auth/acceso-denegado";
            }

            pedidoService.cancelarPedido(id);
            redirectAttributes.addFlashAttribute("mensaje", "Pedido cancelado exitosamente"); 

        } catch (Exception e) {
            log.error("Error al cancelar pedido: {}", e.getMessage());
            redirectAttributes.addFlashAttribute("error", e.getMessage()); 
        }

        return "redirect:/pedidos/historial";
    }

    @GetMapping("/checkout")
    public String checkout(Model model) {
        try {
            Long usuarioId = authService.getUsuarioAutenticadoId();
            if (usuarioId == null) {
                return "redirect:/auth/login";
            }

          
            Usuarios usuario = authService.obtenerUsuarioAutenticado();
            model.addAttribute("usuario", usuario);

          
            List<ItemCarrito> itemsCarrito = carritoService.obtenerItemsCarrito(usuarioId);

           
            if (itemsCarrito == null || itemsCarrito.isEmpty()) {
                model.addAttribute("error", "Tu carrito está vacío");
                return "redirect:/carrito";
            }

            model.addAttribute("itemsCarrito", itemsCarrito);

          
            BigDecimal subtotal = calcularSubtotal(itemsCarrito);
            BigDecimal envio = calcularCostoEnvio(subtotal);
            BigDecimal total = subtotal.add(envio);

           
            Integer totalItems = carritoService.contarItems(usuarioId);

            model.addAttribute("subtotal", subtotal);
            model.addAttribute("costoEnvio", envio);
            model.addAttribute("total", total);
            model.addAttribute("totalItems", totalItems != null ? totalItems : 0);
            model.addAttribute("itemsCount", itemsCarrito.size());

         
            model.addAttribute("direccionEnvio", obtenerDireccionUsuario(usuario));

            log.info("Checkout cargado - Usuario: {}, Items: {}, Total: ${}",
                    usuarioId, itemsCarrito.size(), total);

            return "user/pedidos/checkout";

        } catch (Exception e) {
            log.error(" Error en checkout: {}", e.getMessage(), e);
            model.addAttribute("error", "Error al cargar el checkout: " + e.getMessage());
            return "redirect:/carrito";
        }
    }
    private BigDecimal calcularSubtotal(List<ItemCarrito> items) {
        if (items == null || items.isEmpty()) {
            return BigDecimal.ZERO;
        }

        return items.stream()
                .map(item -> {
                    BigDecimal precio = item.getProducto().getPrecio();
                    BigDecimal cantidad = BigDecimal.valueOf(item.getCantidad());
                    return precio.multiply(cantidad);
                })
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private BigDecimal calcularCostoEnvio(BigDecimal subtotal) {
      
        if (subtotal.compareTo(new BigDecimal("50")) >= 0) {
            return BigDecimal.ZERO;
        }
       
        return new BigDecimal("5.99");
    }

    private Map<String, String> obtenerDireccionUsuario(Usuarios usuario) {
        Map<String, String> direccion = new HashMap<>();

       
        direccion.put("nombreCompleto", usuario.getNombre() + " " + usuario.getApellido());
        direccion.put("email", usuario.getEmail());
        direccion.put("telefono", usuario.getTelefono() != null ? usuario.getTelefono() : "No especificado");
        direccion.put("direccion", usuario.getDireccion() != null ? usuario.getDireccion() : "Dirección no especificada");

        return direccion;
    }

}