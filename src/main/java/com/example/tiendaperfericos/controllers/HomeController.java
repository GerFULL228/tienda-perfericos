package com.example.tiendaperfericos.controllers;




import com.example.tiendaperfericos.services.implement.ProductoServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@RequiredArgsConstructor
@Slf4j
public class HomeController {

    private final ProductoServiceImpl productoService;

    @GetMapping({"", "/", "/home"})
    public String home(Model model) {
        try {
            model.addAttribute("title", "Inicio");
            model.addAttribute("productosDestacados", productoService.findProductosDestacados());
            model.addAttribute("totalProductos", productoService.countProductosActivos());
            return "home";
        } catch (Exception e) {
            log.error("Error al cargar página de inicio: {}", e.getMessage());
            model.addAttribute("title", "Inicio");
            model.addAttribute("error", "Error al cargar los productos destacados");
            return "home";
        }
    }

    @GetMapping("/contacto")
    public String contacto(Model model) {
        model.addAttribute("title", "Contacto");
        return "contacto";
    }

    @GetMapping("/nosotros")
    public String nosotros(Model model) {
        model.addAttribute("title", "Nosotros");
        return "nosotros";
    }

    @GetMapping("/error")
    public String error(Model model) {
        model.addAttribute("title", "Error");
        return "error";
    }
}