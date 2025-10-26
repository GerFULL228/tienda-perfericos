package com.example.tiendaperfericos.controllers;


import com.example.tiendaperfericos.entity.DetallePedido;
import com.example.tiendaperfericos.entity.Pedido;
import com.example.tiendaperfericos.services.implement.AuthServiceImpl;
import com.example.tiendaperfericos.services.implement.PedidoServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;


import java.util.List;

@Controller
@RequestMapping("/pedidos")
@RequiredArgsConstructor
@Slf4j
public class PedidoController {

    private final PedidoServiceImpl pedidoService;
    private final AuthServiceImpl authService;

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
                              Model redirectAttributes) {
        try {
            Long usuarioId = authService.getUsuarioAutenticadoId();
            if (usuarioId == null) {
                return "redirect:/auth/login";
            }

            Pedido pedido = pedidoService.crearPedidoDesdeCarrito(usuarioId, direccionEntrega, telefonoContacto);
            redirectAttributes.addAttribute("mensaje", "Pedido creado exitosamente. Número de pedido: " + pedido.getId());

            return "redirect:/pedidos/detalle/" + pedido.getId();
        } catch (Exception e) {
            log.error("Error al crear pedido: {}", e.getMessage());
            redirectAttributes.addAttribute("error", e.getMessage());
            return "redirect:/carrito";
        }
    }

    @PostMapping("/cancelar/{id}")
    public String cancelarPedido(@PathVariable Long id, Model redirectAttributes) {
        try {
            Long usuarioId = authService.getUsuarioAutenticadoId();
            Pedido pedido = pedidoService.findById(id)
                    .orElseThrow(() -> new RuntimeException("Pedido no encontrado"));


            if (!pedido.getUsuario().getId().equals(usuarioId)) {
                return "redirect:/auth/acceso-denegado";
            }

            pedidoService.cancelarPedido(id);
            redirectAttributes.addAttribute("mensaje", "Pedido cancelado exitosamente");

        } catch (Exception e) {
            log.error("Error al cancelar pedido: {}", e.getMessage());
            redirectAttributes.addAttribute("error", e.getMessage());
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


            var usuario = authService.obtenerUsuarioAutenticado();
            model.addAttribute("usuario", usuario);
            model.addAttribute("totalItems", 0); // Podrías calcular el total aquí

            return "user/pedidos/checkout";
        } catch (Exception e) {
            log.error("Error en checkout: {}", e.getMessage());
            return "redirect:/carrito";
        }
    }
}