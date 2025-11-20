package com.dianaglobal.paineldoauthorbackend.adapter.in.dto.entregas;

import jakarta.validation.constraints.NotNull;

/**
 * DTO para atualizar status de envio de um pedido.
 */
public record AtualizarStatusEnvioRequest(
        @NotNull(message = "Campo 'enviado' é obrigatório")
        Boolean enviado,                    // Se foi enviado (sim/não)
        
        @NotNull(message = "Campo 'statusEnvio' é obrigatório")
        String statusEnvio,                  // ENVIADO, AGUARDANDO, RECUSADO, ENTREGUE
        
        String codigoRastreamento           // Código de rastreamento (opcional)
) {}

