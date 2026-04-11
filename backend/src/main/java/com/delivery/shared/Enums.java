package com.delivery.shared;

public class Enums {

    public enum RoleUsuario {
        ADMIN,
        SUPORTE,   // Acesso operacional — sem exclusão e sem dados financeiros
        LOJA,
        MOTOBOY
    }

    public enum StatusPedido {
        AGUARDANDO_ACEITE,
        ACEITO,
        MOTOBOY_A_CAMINHO_LOJA,
        COLETADO,
        EM_ENTREGA,
        ENTREGUE,
        CANCELADO
    }

    public enum StatusEntrega {
        DISPONIVEL,
        ATRIBUIDA,
        COLETADA,
        FINALIZADA,
        CANCELADA
    }

    public enum StatusMotoboy {
        DISPONIVEL,
        EM_ENTREGA,
        INATIVO
    }

    public enum StatusTransacao {
        PENDENTE,
        APROVADA,
        RECUSADA,
        ESTORNADA
    }

    public enum StatusLiquidacao {
        PENDENTE,
        PROCESSADA,
        FALHA
    }
}
