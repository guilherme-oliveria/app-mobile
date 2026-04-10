package com.delivery.motoboy.dto;

import com.delivery.shared.Enums.StatusMotoboy;

public record MotoboyResponse(
        Long id,
        String nome,
        String cpf,
        String email,
        String telefone,
        String smartPosSerial,
        StatusMotoboy status,
        Double latitudeAtual,
        Double longitudeAtual,
        boolean ativo
) {}
