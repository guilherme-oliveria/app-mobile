package com.delivery.loja.repository;

import com.delivery.loja.entity.Loja;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface LojaRepository extends JpaRepository<Loja, Long> {
    Optional<Loja> findByCnpj(String cnpj);
    Optional<Loja> findByEmail(String email);
}
