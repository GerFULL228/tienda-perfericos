package com.example.tiendaperfericos.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class UsuarioRegistroRequest {
    private String email;
    private String password;
    private String confirmarPassword;
    private String nombre;
    private String apellido;
}
