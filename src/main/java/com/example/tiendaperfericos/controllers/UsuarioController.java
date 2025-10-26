package com.example.tiendaperfericos.controllers;


import com.example.tiendaperfericos.entity.Usuarios;
import com.example.tiendaperfericos.services.implement.AuthServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/user")
@RequiredArgsConstructor
@Slf4j
public class UsuarioController {

    private final AuthServiceImpl authService;

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        try {
            Usuarios usuario = authService.obtenerUsuarioAutenticado();
            model.addAttribute("usuario", usuario);
            return "user/dashboard";
        } catch (Exception e) {
            log.error("Error al cargar dashboard de usuario: {}", e.getMessage());
            return "redirect:/auth/login";
        }
    }

    @GetMapping("/perfil")
    public String perfil(Model model) {
        try {
            Usuarios usuario = authService.obtenerUsuarioAutenticado();
            model.addAttribute("usuario", usuario);
            return "user/perfil";
        } catch (Exception e) {
            log.error("Error al cargar perfil de usuario: {}", e.getMessage());
            return "redirect:/auth/login";
        }
    }
}