package com.example.tiendaperfericos.controllers;

import com.example.tiendaperfericos.entity.Usuarios;
import com.example.tiendaperfericos.request.UsuarioRegistroRequest;
import com.example.tiendaperfericos.services.AuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.HashMap;
import java.util.Map;

@Controller
@RequestMapping("/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {

    private final AuthService authService;

    @GetMapping("/login")
    public String mostrarLogin(@RequestParam(value = "error", required = false) String error,
                               @RequestParam(value = "logout", required = false) String logout,
                               @RequestParam(value = "expired", required = false) String expired,
                               Model model) {

        model.addAttribute("title", "Iniciar Sesión");

        if (error != null) {
            model.addAttribute("error", "Credenciales inválidas. Por favor, intente nuevamente.");
        }

        if (logout != null) {
            model.addAttribute("mensaje", "Ha cerrado sesión exitosamente.");
        }

        if (expired != null) {
            model.addAttribute("mensaje", "Su sesión ha expirado. Por favor, inicie sesión nuevamente.");
        }

        return "auth/login";
    }

    @GetMapping("/registro")
    public String mostrarRegistro(Model model) {
        model.addAttribute("title", "Registrarse");
        model.addAttribute("usuario", new Usuarios());
        return "auth/registro";
    }

    @PostMapping("/registrar")
    public String registrarUsuario(@ModelAttribute("usuario") Usuarios usuario,
                                   @RequestParam String confirmarPassword,
                                   RedirectAttributes redirectAttributes) {

        try {
            if (!usuario.getPassword().equals(confirmarPassword)) {
                redirectAttributes.addFlashAttribute("error", "Las contraseñas no coinciden.");
                return "redirect:/auth/registro";
            }

            if (authService.existeUsuario(usuario.getEmail())) {
                redirectAttributes.addFlashAttribute("error", "El email ya está registrado.");
                return "redirect:/auth/registro";
            }

            authService.registrarUsuario(
                    usuario.getEmail(),
                    usuario.getPassword(),
                    usuario.getNombre(),
                    usuario.getApellido(),
                    "USER"
            );

            redirectAttributes.addFlashAttribute("mensaje",
                    "Registro exitoso. Ahora puede iniciar sesión.");
            return "redirect:/auth/login";

        } catch (Exception e) {
            log.error("Error al registrar usuario: {}", e.getMessage(), e);
            redirectAttributes.addFlashAttribute("error",
                    "Error al registrar usuario: " + e.getMessage());
            return "redirect:/auth/registro";
        }
    }

    @GetMapping("/redireccionar")
    public String redireccionarSegunRol() {
        try {
            if (authService.obtenerUsuarioAutenticado() != null) {
                String rol = authService.obtenerUsuarioAutenticado().getRol().getNombre();
                if ("ADMIN".equals(rol)) {
                    return "redirect:/admin/dashboard";
                } else if ("USER".equals(rol)) {
                    return "redirect:/user/dashboard";
                }
            }
        } catch (Exception e) {
            log.error("Error al redireccionar: {}", e.getMessage());
        }
        return "redirect:/";
    }

    @GetMapping("/acceso-denegado")
    public String accesoDenegado(Model model) {
        model.addAttribute("title", "Acceso Denegado");
        return "auth/acceso-denegado";
    }

    @PostMapping("/registrar-admin")
    public ResponseEntity<Map<String, String>> registrarAdmin(@RequestBody UsuarioRegistroRequest request) {
        Map<String, String> response = new HashMap<>();

        try {

            if (authService.existeUsuario(request.getEmail())) {
                response.put("error", "El email ya está registrado.");
                return ResponseEntity.badRequest().body(response);
            }

            if (!request.getPassword().equals(request.getConfirmarPassword())) {
                response.put("error", "Las contraseñas no coinciden.");
                return ResponseEntity.badRequest().body(response);
            }


            authService.registrarUsuario(
                    request.getEmail(),
                    request.getPassword(),
                    request.getNombre(),
                    request.getApellido(),
                    "ADMIN"
            );

            response.put("mensaje", "Administrador registrado exitosamente.");
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Error al registrar administrador: {}", e.getMessage(), e);
            response.put("error", "Error al registrar administrador: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
}