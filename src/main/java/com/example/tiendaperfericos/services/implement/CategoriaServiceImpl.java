package com.example.tiendaperfericos.services.implement;


import com.example.tiendaperfericos.Repostory.CategoriaRepository;
import com.example.tiendaperfericos.entity.Categoria;
import com.example.tiendaperfericos.services.CategoriaService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CategoriaServiceImpl implements CategoriaService {

    private final CategoriaRepository categoriaRepository;

    @Override
    @Transactional(readOnly = true)
    public List<Categoria> findAll() {
        return categoriaRepository.findByActivaTrue();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Categoria> findAllWithInactivas() {
        return categoriaRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Categoria> findById(Long id) {
        return categoriaRepository.findById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Categoria> findByNombre(String nombre) {
        return categoriaRepository.findByNombre(nombre);
    }

    @Override
    @Transactional
    public Categoria save(Categoria categoria) {
        return categoriaRepository.save(categoria);
    }

    @Transactional
    @Override
    public void deleteById(Long id) {
        categoriaRepository.findById(id).ifPresent(categoria -> {
            categoria.setActiva(false);
            categoriaRepository.save(categoria);
        });
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByNombre(String nombre) {
        return categoriaRepository.existsByNombre(nombre);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Categoria> findCategoriasActivasOrdenadas() {
        return categoriaRepository.findCategoriasActivasOrdenadas();
    }

    @Override
    @Transactional(readOnly = true)
    public Long countProductosActivosByCategoriaId(Long categoriaId) {
        return categoriaRepository.countProductosActivosByCategoriaId(categoriaId);
    }

    @Override
    @Transactional
    public void cambiarEstadoCategoria(Long id, boolean activa) {
        categoriaRepository.findById(id).ifPresent(categoria -> {
            categoria.setActiva(activa);
            categoriaRepository.save(categoria);
        });
    }
}