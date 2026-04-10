package com.delivery.motoboy.repository;

import com.delivery.motoboy.entity.Motoboy;
import com.delivery.shared.Enums.StatusMotoboy;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface MotoboyRepository extends JpaRepository<Motoboy, Long> {
    Optional<Motoboy> findByCpf(String cpf);
    Optional<Motoboy> findBySmartPosSerial(String serial);
    List<Motoboy> findByStatusAndAtivoTrue(StatusMotoboy status);
}
