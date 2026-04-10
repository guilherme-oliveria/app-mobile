package com.delivery.pedido.repository;

import com.delivery.pedido.entity.Pedido;
import com.delivery.shared.Enums.StatusPedido;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface PedidoRepository extends JpaRepository<Pedido, Long> {
    List<Pedido> findByLojaIdOrderByCriadoEmDesc(Long lojaId);
    List<Pedido> findByStatus(StatusPedido status);
    List<Pedido> findByLojaIdAndStatus(Long lojaId, StatusPedido status);
}
