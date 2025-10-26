package com.example.tiendaperfericos.Repostory;



import com.example.tiendaperfericos.entity.Rol;
import com.example.tiendaperfericos.entity.Usuarios;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuarios, Long> {

    Optional<Usuarios> findByEmail(String email);

    Boolean existsByEmail(String email);

    List<Usuarios> findByRol(Rol rol);

    List<Usuarios> findByActivoTrue();

    List<Usuarios> findByRolNombre(String rolNombre);

    @Query("SELECT u FROM Usuarios u WHERE u.nombre LIKE %:nombre% OR u.apellido LIKE %:nombre%")
    List<Usuarios> findByNombreOrApellidoContaining(@Param("nombre") String nombre);

    @Query("SELECT COUNT(u) FROM Usuarios u WHERE u.rol.nombre = :rolNombre")
    Long countByRolNombre(@Param("rolNombre") String rolNombre);
}