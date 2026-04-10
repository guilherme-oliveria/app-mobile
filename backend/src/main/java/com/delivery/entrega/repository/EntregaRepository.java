package com.delivery.entrega.repository;

import com.delivery.entrega.entity.Entrega;
import com.delivery.shared.Enums.StatusEntrega;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface EntregaRepository extends JpaRepository<Entrega, Long> {
    List<Entrega> findByMotoboyIdAndStatus(Long motoboyId, StatusEntrega status);
    List<Entrega> findByStatus(StatusEntrega status);
    Optional<Entrega> findByPedidoId(Long pedidoId);

    @Query("SELECT e FROM Entrega e WHERE e.motoboy.id = :motoboyId ORDER BY e.criadoEm DESC")
    List<Entrega> findByMotoboyIdOrderByDataDesc(Long motoboyId);
}
