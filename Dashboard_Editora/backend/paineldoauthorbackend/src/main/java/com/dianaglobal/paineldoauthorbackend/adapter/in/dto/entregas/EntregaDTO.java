package com.dianaglobal.paineldoauthorbackend.adapter.in.dto.entregas;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

/**
 * DTO para informações completas de uma entrega.
 * Inclui dados do pedido e do cliente para envio.
 */
public record EntregaDTO(
        // Dados do Pedido
        Long pedidoId,
        Instant dataPedido,
        BigDecimal valorTotal,
        String statusPedido,
        
        // Dados do Cliente (para envio)
        String nomeCompleto,
        String email,
        String telefone,
        String cpf,
        
        // Endereço Completo
        String rua,
        String numero,
        String complemento,
        String bairro,
        String cidade,
        String estado,
        String cep,
        String enderecoCompleto,  // Endereço formatado completo
        
        // Itens do Pedido
        List<ItemEntregaDTO> itens,
        
        // Status de Envio
        Boolean enviado,                    // Se foi enviado (sim/não)
        String statusEnvio,                  // ENVIADO, AGUARDANDO, RECUSADO, ENTREGUE
        String codigoRastreamento,          // Código de rastreamento
        Instant enviadoAt,                  // Quando foi marcado como enviado
        Instant updatedAt                   // Última atualização do status
) {}

