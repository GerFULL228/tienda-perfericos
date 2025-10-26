package com.example.tiendaperfericos.services;

import com.example.tiendaperfericos.entity.Categoria;


import java.util.List;
import java.util.Optional;

public interface CategoriaService {
    List<Categoria> findAll();
    List<Categoria> findAllWithInactivas();
    Optional<Categoria> findById(Long id);
    Optional<Categoria> findByNombre(String nombre);
    Categoria save(Categoria categoria);
    void deleteById(Long id);
    boolean existsByNombre(String nombre);
    List<Categoria> findCategoriasActivasOrdenadas();
    Long countProductosActivosByCategoriaId(Long categoriaId);
    void cambiarEstadoCategoria(Long id, boolean activa);
}