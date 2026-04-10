package com.delivery.pagamento.repository;

import com.delivery.pagamento.entity.LiquidacaoDia;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface LiquidacaoRepository extends JpaRepository<LiquidacaoDia, Long> {
    List<LiquidacaoDia> findByDataReferencia(LocalDate data);
    List<LiquidacaoDia> findByLojaIdOrderByDataReferenciaDesc(Long lojaId);
}
