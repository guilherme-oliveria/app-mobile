package com.delivery.pagamento.repository;

import com.delivery.pagamento.entity.Transacao;
import com.delivery.shared.Enums.StatusTransacao;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface TransacaoRepository extends JpaRepository<Transacao, Long> {
    Optional<Transacao> findByEntregaId(Long entregaId);
    List<Transacao> findByLiquidadaFalseAndStatus(StatusTransacao status);

    @Query("""
        SELECT COALESCE(SUM(t.valorLiquidoLoja), 0)
        FROM Transacao t
        WHERE t.entrega.pedido.loja.id = :lojaId
        AND t.liquidada = false
        AND t.status = 'APROVADA'
    """)
    BigDecimal somarSaldoPendenteByLoja(Long lojaId);
}
