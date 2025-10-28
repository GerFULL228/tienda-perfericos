package com.example.tiendaperfericos.config;

import com.example.tiendaperfericos.services.AuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class AdminInitializer implements CommandLineRunner {

    private final AuthService authService;

    @Override
    public void run(String... args) throws Exception {
        try {
            // Verificar si ya existe un admin
            if (!authService.existeUsuario("admin@peritech.com")) {
                authService.registrarUsuario(
                        "admin@peritech.com",
                        "123",
                        "luis",
                        "perez",
                        "ADMIN"
                );

            } else {

            }
        } catch (Exception e) {
            log.error("Error al crear usuario administrador: {}", e.getMessage());
        }
    }
}