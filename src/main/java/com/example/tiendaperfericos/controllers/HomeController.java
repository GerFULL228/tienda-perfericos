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

    @GetMapping("/")
    public String home(Model model) {
        try {
            model.addAttribute("productosDestacados", productoService.findProductosDestacados());
            return "home";
        } catch (Exception e) {
            log.error("Error al cargar página de inicio: {}", e.getMessage());
            return "home";
        }
    }

    @GetMapping("/contacto")
    public String contacto() {
        return "contacto";
    }

    @GetMapping("/nosotros")
    public String nosotros() {
        return "nosotros";
    }
}