package com.example.tiendaperfericos.excepcions;

public class ProductoNoEncontrado extends RuntimeException {
    public ProductoNoEncontrado(String message) {
        super(message);
    }
}
