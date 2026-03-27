package com.dianaglobal.paineldoauthorbackend.application.service;

import com.dianaglobal.paineldoauthorbackend.adapter.in.dto.cobrancas.MonthlyChargeDTO;
import com.dianaglobal.paineldoauthorbackend.adapter.in.dto.emails.PainelEmailsAutorDTO;
import com.dianaglobal.paineldoauthorbackend.adapter.in.dto.entregas.EntregaDTO;
import com.dianaglobal.paineldoauthorbackend.adapter.in.dto.pagamentos.PainelPagamentosAutorDTO;
import com.dianaglobal.paineldoauthorbackend.adapter.in.dto.tickets.TicketDTO;
import com.dianaglobal.paineldoauthorbackend.adapter.in.web.AdminController.AuthorStatsResponse;
import com.dianaglobal.paineldoauthorbackend.adapter.out.grpc.GrpcExportClient;
import com.dianaglobal.paineldoauthorbackend.grpc.export.ExportResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Implementação do ExportService que delega a geração de PDF/CSV
 * ao export-service Go via gRPC.
 *
 * Ativado quando export.grpc.enabled=true.
 */
@Slf4j
@Primary
@Service
@RequiredArgsConstructor
@ConditionalOnProperty(name = "export.grpc.enabled", havingValue = "true")
public class GrpcExportServiceImpl implements ExportService {

    private final GrpcExportClient grpcClient;

    // ─── Cobranças ────────────────────────────────────────────────────────────

    @Override
    public byte[] exportCobrancasToPdf(List<MonthlyChargeDTO> cobrancas, String authorName) {
        ExportResponse resp = grpcClient.exportCharges("pdf", authorName, toChargeRows(cobrancas));
        log.info("[GRPC EXPORT] PDF cobranças gerado: {} bytes", resp.getData().size());
        return resp.getData().toByteArray();
    }

    @Override
    public byte[] exportCobrancasToCsv(List<MonthlyChargeDTO> cobrancas, String authorName) {
        ExportResponse resp = grpcClient.exportCharges("csv", authorName, toChargeRows(cobrancas));
        return resp.getData().toByteArray();
    }

    // ─── Tickets ──────────────────────────────────────────────────────────────

    @Override
    public byte[] exportTicketsToPdf(List<TicketDTO> tickets, String authorName) {
        ExportResponse resp = grpcClient.exportTickets("pdf", authorName, toTicketRows(tickets));
        return resp.getData().toByteArray();
    }

    @Override
    public byte[] exportTicketsToCsv(List<TicketDTO> tickets, String authorName) {
        ExportResponse resp = grpcClient.exportTickets("csv", authorName, toTicketRows(tickets));
        return resp.getData().toByteArray();
    }

    // ─── Entregas ─────────────────────────────────────────────────────────────

    @Override
    public byte[] exportEntregasToPdf(List<EntregaDTO> entregas, String authorName) {
        ExportResponse resp = grpcClient.exportDeliveries("pdf", authorName, toEntregaRows(entregas));
        return resp.getData().toByteArray();
    }

    @Override
    public byte[] exportEntregasToCsv(List<EntregaDTO> entregas, String authorName) {
        ExportResponse resp = grpcClient.exportDeliveries("csv", authorName, toEntregaRows(entregas));
        return resp.getData().toByteArray();
    }

    // ─── Pagamentos / Emails / Métricas (ainda no PDFBox) ─────────────────────
    // Estes tipos ainda usam a impl local — delegação futura quando Go suportar.

    @Override
    public byte[] exportPaymentsToPdf(PainelPagamentosAutorDTO painel, String authorName) {
        throw new UnsupportedOperationException("exportPaymentsToPdf: use ExportServiceImpl (PDFBox)");
    }

    @Override
    public byte[] exportPaymentsToCsv(PainelPagamentosAutorDTO painel, String authorName) {
        throw new UnsupportedOperationException("exportPaymentsToCsv: use ExportServiceImpl (PDFBox)");
    }

    @Override
    public byte[] exportEmailsToPdf(PainelEmailsAutorDTO painel, String authorName) {
        throw new UnsupportedOperationException("exportEmailsToPdf: use ExportServiceImpl (PDFBox)");
    }

    @Override
    public byte[] exportEmailsToCsv(PainelEmailsAutorDTO painel, String authorName) {
        throw new UnsupportedOperationException("exportEmailsToCsv: use ExportServiceImpl (PDFBox)");
    }

    @Override
    public byte[] exportMetricasToPdf(AuthorStatsResponse metricas, String authorName) {
        throw new UnsupportedOperationException("exportMetricasToPdf: use ExportServiceImpl (PDFBox)");
    }

    @Override
    public byte[] exportMetricasToCsv(AuthorStatsResponse metricas, String authorName) {
        throw new UnsupportedOperationException("exportMetricasToCsv: use ExportServiceImpl (PDFBox)");
    }

    // ─── Conversores DTO → Record rows ────────────────────────────────────────

    private List<Map<String, String>> toChargeRows(List<MonthlyChargeDTO> list) {
        List<Map<String, String>> rows = new ArrayList<>();
        for (MonthlyChargeDTO c : list) {
            Map<String, String> row = new LinkedHashMap<>();
            row.put("autor", safe(c.authorName()));
            row.put("mes", c.chargeMonth() != null ? c.chargeMonth().toString() : "");
            row.put("ano", c.chargeYear() != null ? c.chargeYear().toString() : "");
            row.put("valor", c.amount() != null ? c.amount().toPlainString() : "");
            row.put("status", safe(c.status()));
            row.put("vencimento", c.dueDate() != null ? c.dueDate().toString() : "");
            row.put("pago_em", c.paidAt() != null ? c.paidAt().toString() : "");
            row.put("dias_atraso", String.valueOf(c.daysOverdue()));
            row.put("ticket_aberto", c.hasOpenTicket() ? "Sim" : "Não");
            rows.add(row);
        }
        return rows;
    }

    private List<Map<String, String>> toTicketRows(List<TicketDTO> list) {
        List<Map<String, String>> rows = new ArrayList<>();
        for (TicketDTO t : list) {
            Map<String, String> row = new LinkedHashMap<>();
            row.put("numero", safe(t.ticketNumber()));
            row.put("titulo", safe(t.title()));
            row.put("categoria", safe(t.category()));
            row.put("status", safe(t.status()));
            row.put("criado_em", t.createdAt() != null ? t.createdAt().toString() : "");
            row.put("atualizado_em", t.updatedAt() != null ? t.updatedAt().toString() : "");
            row.put("mensagens", t.messages() != null ? String.valueOf(t.messages().size()) : "0");
            rows.add(row);
        }
        return rows;
    }

    private List<Map<String, String>> toEntregaRows(List<EntregaDTO> list) {
        List<Map<String, String>> rows = new ArrayList<>();
        for (EntregaDTO e : list) {
            Map<String, String> row = new LinkedHashMap<>();
            row.put("pedido_id", e.pedidoId() != null ? e.pedidoId().toString() : "");
            row.put("cliente", safe(e.nomeCompleto()));
            row.put("email", safe(e.email()));
            row.put("cidade", safe(e.cidade()));
            row.put("estado", safe(e.estado()));
            row.put("status_pedido", safe(e.statusPedido()));
            row.put("status_envio", safe(e.statusEnvio()));
            row.put("rastreamento", safe(e.codigoRastreamento()));
            row.put("enviado_em", e.enviadoAt() != null ? e.enviadoAt().toString() : "");
            row.put("valor", e.valorTotal() != null ? e.valorTotal().toPlainString() : "");
            rows.add(row);
        }
        return rows;
    }

    private static String safe(String s) {
        return s != null ? s : "";
    }
}
