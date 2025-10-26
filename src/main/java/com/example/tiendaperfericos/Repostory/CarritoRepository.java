package com.example.tiendaperfericos.Repostory;




import com.example.tiendaperfericos.entity.Carrito;
import com.example.tiendaperfericos.entity.Usuarios;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CarritoRepository extends JpaRepository<Carrito, Long> {

    Optional<Carrito> findByUsuarioAndId(Usuarios usuario, Long carritoId);

    Optional<Carrito> findByUsuario(Usuarios usuario);

    @Query("SELECT c FROM Carrito c WHERE c.usuario.id = :usuarioId")
    Optional<Carrito> findByUsuarioId(@Param("usuarioId") Long usuarioId);

    Boolean existsByUsuario(Usuarios usuario);

    @Query("SELECT COUNT(c) FROM Carrito c WHERE c.usuario.id = :usuarioId")
    Long countByUsuarioId(@Param("usuarioId") Long usuarioId);

    void deleteByUsuario(Usuarios usuario);
}