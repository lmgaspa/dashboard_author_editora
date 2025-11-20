package com.dianaglobal.paineldoauthorbackend.application.service;

import com.dianaglobal.paineldoauthorbackend.adapter.in.dto.entregas.AtualizarStatusEnvioRequest;
import com.dianaglobal.paineldoauthorbackend.adapter.in.dto.entregas.EntregaDTO;

import java.util.List;

/**
 * Service para gerenciar entregas de pedidos.
 */
public interface EntregasService {
    
    /**
     * Lista todos os pedidos confirmados do autor com informações para envio.
     */
    List<EntregaDTO> listarEntregas(Long authorId, String dbUrl, String dbUsername, String dbPassword);
    
    /**
     * Busca uma entrega específica por order_id.
     */
    EntregaDTO buscarEntrega(Long orderId, Long authorId, String dbUrl, String dbUsername, String dbPassword);
    
    /**
     * Atualiza o status de envio de um pedido.
     */
    EntregaDTO atualizarStatusEnvio(
        Long orderId, 
        String authorId, 
        AtualizarStatusEnvioRequest request
    );
}

