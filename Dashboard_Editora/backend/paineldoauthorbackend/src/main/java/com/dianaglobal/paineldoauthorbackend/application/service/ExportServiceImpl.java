package com.dianaglobal.paineldoauthorbackend.application.service;

import com.dianaglobal.paineldoauthorbackend.adapter.in.dto.cobrancas.MonthlyChargeDTO;
import com.dianaglobal.paineldoauthorbackend.adapter.in.dto.entregas.EntregaDTO;
import com.dianaglobal.paineldoauthorbackend.adapter.in.dto.entregas.ItemEntregaDTO;
import com.dianaglobal.paineldoauthorbackend.adapter.in.dto.emails.PainelEmailsAutorDTO;
import com.dianaglobal.paineldoauthorbackend.adapter.in.dto.emails.ResumoEmailClienteDTO;
import com.dianaglobal.paineldoauthorbackend.adapter.in.dto.emails.ResumoEmailRepasseDTO;
import com.dianaglobal.paineldoauthorbackend.adapter.in.dto.pagamentos.PainelPagamentosAutorDTO;
import com.dianaglobal.paineldoauthorbackend.adapter.in.dto.pagamentos.VendaRecenteDTO;
import com.dianaglobal.paineldoauthorbackend.adapter.in.dto.tickets.MessageDTO;
import com.dianaglobal.paineldoauthorbackend.adapter.in.dto.tickets.TicketDTO;
import com.dianaglobal.paineldoauthorbackend.adapter.in.web.AdminController.AuthorStatsResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Implementação do serviço de exportação.
 */
@Slf4j
@Service
public class ExportServiceImpl implements ExportService {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    @Override
    public byte[] exportPaymentsToPdf(PainelPagamentosAutorDTO painel, String authorName) {
        try (PDDocument document = new PDDocument();
             ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            
            PDPage page = new PDPage(PDRectangle.A4);
            document.addPage(page);
            
            PDPageContentStream contentStream = new PDPageContentStream(document, page);
            try {
                float yPosition = 750;
                float margin = 50;
                float lineHeight = 20;
                
                // Título
                contentStream.beginText();
                contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD), 18);
                contentStream.newLineAtOffset(margin, yPosition);
                contentStream.showText("Relatório de Pagamentos");
                contentStream.endText();
                yPosition -= 30;
                
                // Nome do Autor
                contentStream.beginText();
                contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), 12);
                contentStream.newLineAtOffset(margin, yPosition);
                contentStream.showText("Autor: " + (authorName != null ? authorName : "N/A"));
                contentStream.endText();
                yPosition -= 30;
                
                // Resumo
                if (painel.resumo() != null) {
                    contentStream.beginText();
                    contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD), 14);
                    contentStream.newLineAtOffset(margin, yPosition);
                    contentStream.showText("Resumo Financeiro");
                    contentStream.endText();
                    yPosition -= 25;
                    
                    var resumo = painel.resumo();
                    yPosition = addTextLine(contentStream, "Valor de Vendas Confirmadas: R$ " + 
                        String.format("%.2f", resumo.valorVendasConfirmadas()), margin, yPosition, lineHeight);
                    yPosition = addTextLine(contentStream, "Valor Já Recebido: R$ " + 
                        String.format("%.2f", resumo.valorJaRecebido()), margin, yPosition, lineHeight);
                    yPosition = addTextLine(contentStream, "Valor a Receber: R$ " + 
                        String.format("%.2f", resumo.valorAReceber()), margin, yPosition, lineHeight);
                    yPosition -= 20;
                }
                
                // Vendas Recentes
                if (painel.vendasRecentes() != null && !painel.vendasRecentes().isEmpty()) {
                    contentStream.beginText();
                    contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD), 14);
                    contentStream.newLineAtOffset(margin, yPosition);
                    contentStream.showText("Vendas Recentes");
                    contentStream.endText();
                    yPosition -= 25;
                    
                    for (VendaRecenteDTO venda : painel.vendasRecentes()) {
                        if (yPosition < 100) {
                            // Nova página se necessário
                            contentStream.close();
                            PDPage newPage = new PDPage(PDRectangle.A4);
                            document.addPage(newPage);
                            contentStream = new PDPageContentStream(document, newPage);
                            yPosition = 750;
                        }
                        
                        String linha = String.format("Pedido #%d - %s - %d unidade(s) - R$ %.2f - %s",
                            venda.pedidoId(),
                            venda.tituloLivro(),
                            venda.quantidade(),
                            venda.valorTotal(),
                            venda.statusLegivel());
                        
                        yPosition = addTextLine(contentStream, linha, margin, yPosition, lineHeight);
                    }
                }
            } finally {
                contentStream.close();
            }
            
            document.save(baos);
            return baos.toByteArray();
            
        } catch (IOException e) {
            log.error("Erro ao gerar PDF de pagamentos: {}", e.getMessage(), e);
            throw new RuntimeException("Erro ao gerar PDF", e);
        }
    }

    @Override
    public byte[] exportPaymentsToCsv(PainelPagamentosAutorDTO painel, String authorName) {
        StringBuilder csv = new StringBuilder();
        
        // Cabeçalho
        csv.append("Relatório de Pagamentos\n");
        csv.append("Autor,").append(authorName != null ? authorName : "N/A").append("\n\n");
        
        // Resumo
        if (painel.resumo() != null) {
            csv.append("Resumo Financeiro\n");
            csv.append("Valor de Vendas Confirmadas,").append(String.format("%.2f", painel.resumo().valorVendasConfirmadas())).append("\n");
            csv.append("Valor Já Recebido,").append(String.format("%.2f", painel.resumo().valorJaRecebido())).append("\n");
            csv.append("Valor a Receber,").append(String.format("%.2f", painel.resumo().valorAReceber())).append("\n\n");
        }
        
        // Vendas Recentes
        if (painel.vendasRecentes() != null && !painel.vendasRecentes().isEmpty()) {
            csv.append("Vendas Recentes\n");
            csv.append("Pedido ID,Data,Título Livro,Quantidade,Valor Total,Status\n");
            
            for (VendaRecenteDTO venda : painel.vendasRecentes()) {
                csv.append(venda.pedidoId()).append(",");
                csv.append(venda.dataPedido() != null ? 
                    venda.dataPedido().format(DATE_FORMATTER) : "N/A").append(",");
                csv.append(escapeCsv(venda.tituloLivro())).append(",");
                csv.append(venda.quantidade()).append(",");
                csv.append(String.format("%.2f", venda.valorTotal())).append(",");
                csv.append(escapeCsv(venda.statusLegivel())).append("\n");
            }
        }
        
        return csv.toString().getBytes(StandardCharsets.UTF_8);
    }

    @Override
    public byte[] exportEmailsToPdf(PainelEmailsAutorDTO painel, String authorName) {
        try (PDDocument document = new PDDocument();
             ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            
            PDPage page = new PDPage(PDRectangle.A4);
            document.addPage(page);
            
            PDPageContentStream contentStream = new PDPageContentStream(document, page);
            try {
                float yPosition = 750;
                float margin = 50;
                float lineHeight = 20;
                
                // Título
                contentStream.beginText();
                contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD), 18);
                contentStream.newLineAtOffset(margin, yPosition);
                contentStream.showText("Relatório de E-mails");
                contentStream.endText();
                yPosition -= 30;
                
                // Nome do Autor
                contentStream.beginText();
                contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), 12);
                contentStream.newLineAtOffset(margin, yPosition);
                contentStream.showText("Autor: " + (authorName != null ? authorName : "N/A"));
                contentStream.endText();
                yPosition -= 30;
                
                // E-mails de Clientes
                if (painel.emailsClientes() != null && !painel.emailsClientes().isEmpty()) {
                    contentStream.beginText();
                    contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD), 14);
                    contentStream.newLineAtOffset(margin, yPosition);
                    contentStream.showText("E-mails de Clientes");
                    contentStream.endText();
                    yPosition -= 25;
                    
                    for (ResumoEmailClienteDTO email : painel.emailsClientes()) {
                        if (yPosition < 100) {
                            contentStream.close();
                            PDPage newPage = new PDPage(PDRectangle.A4);
                            document.addPage(newPage);
                            contentStream = new PDPageContentStream(document, newPage);
                            yPosition = 750;
                        }
                        
                        String linha = String.format("%s - %d pedido(s) confirmado(s) - R$ %.2f",
                            email.email(),
                            email.totalPedidosConfirmados(),
                            email.valorRepassado() != null ? email.valorRepassado().doubleValue() : 0.0);
                        
                        yPosition = addTextLine(contentStream, linha, margin, yPosition, lineHeight);
                    }
                    yPosition -= 20;
                }
                
                // E-mails de Repasse
                if (painel.emailsRepasse() != null && !painel.emailsRepasse().isEmpty()) {
                    contentStream.beginText();
                    contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD), 14);
                    contentStream.newLineAtOffset(margin, yPosition);
                    contentStream.showText("E-mails de Repasse");
                    contentStream.endText();
                    yPosition -= 25;
                    
                    for (ResumoEmailRepasseDTO email : painel.emailsRepasse()) {
                        if (yPosition < 100) {
                            contentStream.close();
                            PDPage newPage = new PDPage(PDRectangle.A4);
                            document.addPage(newPage);
                            contentStream = new PDPageContentStream(document, newPage);
                            yPosition = 750;
                        }
                        
                        String cupomInfo = "";
                        if (email.cupom() != null && email.cupom().teveCupom() != null && email.cupom().teveCupom()) {
                            cupomInfo = String.format(" - Cupom: %s (Desconto: R$ %.2f)",
                                email.cupom().codigoCupom() != null ? email.cupom().codigoCupom() : "N/A",
                                email.cupom().valorDesconto() != null ? email.cupom().valorDesconto().doubleValue() : 0.0);
                        }
                        
                        String linha = String.format("%s - Pedido #%d - R$ %.2f - %s%s",
                            email.emailDestinatario() != null ? email.emailDestinatario() : "N/A",
                            email.pedidoId() != null ? email.pedidoId() : "N/A",
                            email.valorRepassado() != null ? email.valorRepassado().doubleValue() : 0.0,
                            email.status() != null ? email.status() : "N/A",
                            cupomInfo);
                        
                        yPosition = addTextLine(contentStream, linha, margin, yPosition, lineHeight);
                    }
                }
            } finally {
                contentStream.close();
            }
            
            document.save(baos);
            return baos.toByteArray();
            
        } catch (IOException e) {
            log.error("Erro ao gerar PDF de emails: {}", e.getMessage(), e);
            throw new RuntimeException("Erro ao gerar PDF", e);
        }
    }

    @Override
    public byte[] exportEmailsToCsv(PainelEmailsAutorDTO painel, String authorName) {
        StringBuilder csv = new StringBuilder();
        
        // Cabeçalho
        csv.append("Relatório de E-mails\n");
        csv.append("Autor,").append(authorName != null ? authorName : "N/A").append("\n\n");
        
        // E-mails de Clientes
        if (painel.emailsClientes() != null && !painel.emailsClientes().isEmpty()) {
            csv.append("E-mails de Clientes\n");
            csv.append("E-mail,Total Pedidos,Pedidos Confirmados,Valor Repassado,Pedidos com Cupom,Total Desconto,Primeiro Pedido,Último Pedido\n");
            
            for (ResumoEmailClienteDTO email : painel.emailsClientes()) {
                csv.append(escapeCsv(email.email())).append(",");
                csv.append(email.totalPedidos()).append(",");
                csv.append(email.totalPedidosConfirmados()).append(",");
                csv.append(email.valorRepassado() != null ? 
                    String.format("%.2f", email.valorRepassado().doubleValue()) : "0.00").append(",");
                
                // Informações de cupom
                if (email.cupom() != null) {
                    csv.append(email.cupom().pedidosComCupom()).append(",");
                    csv.append(email.cupom().totalDesconto() != null ? 
                        String.format("%.2f", email.cupom().totalDesconto().doubleValue()) : "0.00");
                } else {
                    csv.append("0").append(",");
                    csv.append("0.00");
                }
                csv.append(",");
                
                csv.append(email.primeiroPedidoEm() != null ? 
                    email.primeiroPedidoEm().toString() : "N/A").append(",");
                csv.append(email.ultimoPedidoEm() != null ? 
                    email.ultimoPedidoEm().toString() : "N/A").append("\n");
            }
            csv.append("\n");
        }
        
        // E-mails de Repasse
        if (painel.emailsRepasse() != null && !painel.emailsRepasse().isEmpty()) {
            csv.append("E-mails de Repasse\n");
            csv.append("E-mail,Order ID,Valor Repassado,Status,Data Envio,Teve Cupom,Código Cupom,Valor Desconto\n");
            
            for (ResumoEmailRepasseDTO email : painel.emailsRepasse()) {
                csv.append(escapeCsv(email.emailDestinatario() != null ? email.emailDestinatario() : "N/A")).append(",");
                csv.append(email.pedidoId() != null ? email.pedidoId() : "N/A").append(",");
                csv.append(email.valorRepassado() != null ? 
                    String.format("%.2f", email.valorRepassado().doubleValue()) : "0.00").append(",");
                csv.append(escapeCsv(email.status() != null ? email.status() : "N/A")).append(",");
                csv.append(email.enviadoEm() != null ? email.enviadoEm().toString() : "N/A").append(",");
                
                // Informações de cupom
                if (email.cupom() != null) {
                    csv.append(email.cupom().teveCupom() != null && email.cupom().teveCupom() ? "SIM" : "NÃO").append(",");
                    csv.append(escapeCsv(email.cupom().codigoCupom() != null ? email.cupom().codigoCupom() : "")).append(",");
                    csv.append(email.cupom().valorDesconto() != null ? 
                        String.format("%.2f", email.cupom().valorDesconto().doubleValue()) : "0.00");
                } else {
                    csv.append("NÃO").append(",");
                    csv.append("").append(",");
                    csv.append("0.00");
                }
                csv.append("\n");
            }
        }
        
        return csv.toString().getBytes(StandardCharsets.UTF_8);
    }
    
    private float addTextLine(PDPageContentStream contentStream, String text, float x, float y, float lineHeight) throws IOException {
        contentStream.beginText();
        contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), 10);
        contentStream.newLineAtOffset(x, y);
        // Truncar texto se muito longo
        if (text.length() > 80) {
            text = text.substring(0, 77) + "...";
        }
        contentStream.showText(text);
        contentStream.endText();
        return y - lineHeight;
    }
    
    @Override
    public byte[] exportEntregasToPdf(List<EntregaDTO> entregas, String authorName) {
        try (PDDocument document = new PDDocument();
             ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            
            PDPage page = new PDPage(PDRectangle.A4);
            document.addPage(page);
            
            PDPageContentStream contentStream = new PDPageContentStream(document, page);
            try {
                float yPosition = 750;
                float margin = 50;
                float lineHeight = 20;
                
                // Título
                contentStream.beginText();
                contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD), 18);
                contentStream.newLineAtOffset(margin, yPosition);
                contentStream.showText("Relatório de Entregas");
                contentStream.endText();
                yPosition -= 30;
                
                // Nome do Autor
                contentStream.beginText();
                contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), 12);
                contentStream.newLineAtOffset(margin, yPosition);
                contentStream.showText("Autor: " + (authorName != null ? authorName : "N/A"));
                contentStream.endText();
                yPosition -= 30;
                
                // Total de entregas
                contentStream.beginText();
                contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), 10);
                contentStream.newLineAtOffset(margin, yPosition);
                contentStream.showText("Total de Entregas: " + entregas.size());
                contentStream.endText();
                yPosition -= 30;
                
                // Lista de entregas
                for (EntregaDTO entrega : entregas) {
                    if (yPosition < 100) {
                        contentStream.close();
                        PDPage newPage = new PDPage(PDRectangle.A4);
                        document.addPage(newPage);
                        contentStream = new PDPageContentStream(document, newPage);
                        yPosition = 750;
                    }
                    
                    String dataPedido = entrega.dataPedido() != null ? 
                        formatInstant(entrega.dataPedido()) : "N/A";
                    
                    yPosition = addTextLine(contentStream, 
                        "Pedido #" + entrega.pedidoId() + " - " + dataPedido, 
                        margin, yPosition, lineHeight);
                    yPosition = addTextLine(contentStream, 
                        "Cliente: " + (entrega.nomeCompleto() != null ? entrega.nomeCompleto() : "N/A"), 
                        margin, yPosition, lineHeight);
                    yPosition = addTextLine(contentStream, 
                        "Status: " + (entrega.statusEnvio() != null ? entrega.statusEnvio() : "N/A"), 
                        margin, yPosition, lineHeight);
                    if (entrega.codigoRastreamento() != null && !entrega.codigoRastreamento().isEmpty()) {
                        yPosition = addTextLine(contentStream, 
                            "Rastreamento: " + entrega.codigoRastreamento(), 
                            margin, yPosition, lineHeight);
                    }
                    yPosition -= 10;
                }
            } finally {
                contentStream.close();
            }
            
            document.save(baos);
            return baos.toByteArray();
            
        } catch (IOException e) {
            log.error("Erro ao gerar PDF de entregas: {}", e.getMessage(), e);
            throw new RuntimeException("Erro ao gerar PDF", e);
        }
    }

    @Override
    public byte[] exportEntregasToCsv(List<EntregaDTO> entregas, String authorName) {
        StringBuilder csv = new StringBuilder();
        
        csv.append("Relatório de Entregas\n");
        csv.append("Autor,").append(authorName != null ? authorName : "N/A").append("\n\n");
        csv.append("Pedido ID,Data Pedido,Cliente,Email,Telefone,Endereço,Status Envio,Enviado,Código Rastreamento,Valor Total\n");
        
        for (EntregaDTO entrega : entregas) {
            csv.append(entrega.pedidoId()).append(",");
            csv.append(entrega.dataPedido() != null ? formatInstant(entrega.dataPedido()) : "N/A").append(",");
            csv.append(escapeCsv(entrega.nomeCompleto() != null ? entrega.nomeCompleto() : "N/A")).append(",");
            csv.append(escapeCsv(entrega.email() != null ? entrega.email() : "N/A")).append(",");
            csv.append(escapeCsv(entrega.telefone() != null ? entrega.telefone() : "N/A")).append(",");
            csv.append(escapeCsv(entrega.enderecoCompleto() != null ? entrega.enderecoCompleto() : "N/A")).append(",");
            csv.append(escapeCsv(entrega.statusEnvio() != null ? entrega.statusEnvio() : "N/A")).append(",");
            csv.append(entrega.enviado() != null && entrega.enviado() ? "SIM" : "NÃO").append(",");
            csv.append(escapeCsv(entrega.codigoRastreamento() != null ? entrega.codigoRastreamento() : "")).append(",");
            csv.append(entrega.valorTotal() != null ? String.format("%.2f", entrega.valorTotal().doubleValue()) : "0.00").append("\n");
        }
        
        return csv.toString().getBytes(StandardCharsets.UTF_8);
    }

    @Override
    public byte[] exportCobrancasToPdf(List<MonthlyChargeDTO> cobrancas, String authorName) {
        try (PDDocument document = new PDDocument();
             ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            
            PDPage page = new PDPage(PDRectangle.A4);
            document.addPage(page);
            
            PDPageContentStream contentStream = new PDPageContentStream(document, page);
            try {
                float yPosition = 750;
                float margin = 50;
                float lineHeight = 20;
                
                // Título
                contentStream.beginText();
                contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD), 18);
                contentStream.newLineAtOffset(margin, yPosition);
                contentStream.showText("Relatório de Cobranças");
                contentStream.endText();
                yPosition -= 30;
                
                // Nome do Autor
                contentStream.beginText();
                contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), 12);
                contentStream.newLineAtOffset(margin, yPosition);
                contentStream.showText("Autor: " + (authorName != null ? authorName : "N/A"));
                contentStream.endText();
                yPosition -= 30;
                
                // Lista de cobranças
                for (MonthlyChargeDTO cobranca : cobrancas) {
                    if (yPosition < 100) {
                        contentStream.close();
                        PDPage newPage = new PDPage(PDRectangle.A4);
                        document.addPage(newPage);
                        contentStream = new PDPageContentStream(document, newPage);
                        yPosition = 750;
                    }
                    
                    String mesAno = String.format("%02d/%d", cobranca.chargeMonth(), cobranca.chargeYear());
                    yPosition = addTextLine(contentStream, 
                        mesAno + " - R$ " + String.format("%.2f", cobranca.amount().doubleValue()), 
                        margin, yPosition, lineHeight);
                    yPosition = addTextLine(contentStream, 
                        "Status: " + cobranca.status() + " - Vencimento: " + 
                        (cobranca.dueDate() != null ? cobranca.dueDate().toString() : "N/A"), 
                        margin, yPosition, lineHeight);
                    yPosition -= 10;
                }
            } finally {
                contentStream.close();
            }
            
            document.save(baos);
            return baos.toByteArray();
            
        } catch (IOException e) {
            log.error("Erro ao gerar PDF de cobranças: {}", e.getMessage(), e);
            throw new RuntimeException("Erro ao gerar PDF", e);
        }
    }

    @Override
    public byte[] exportCobrancasToCsv(List<MonthlyChargeDTO> cobrancas, String authorName) {
        StringBuilder csv = new StringBuilder();
        
        csv.append("Relatório de Cobranças\n");
        csv.append("Autor,").append(authorName != null ? authorName : "N/A").append("\n\n");
        csv.append("Mês/Ano,Valor,Data Vencimento,Data Cobrança,Status,Dias Atraso,Pago Em\n");
        
        for (MonthlyChargeDTO cobranca : cobrancas) {
            csv.append(String.format("%02d/%d", cobranca.chargeMonth(), cobranca.chargeYear())).append(",");
            csv.append(String.format("%.2f", cobranca.amount().doubleValue())).append(",");
            csv.append(cobranca.dueDate() != null ? cobranca.dueDate().toString() : "N/A").append(",");
            csv.append(cobranca.chargeDate() != null ? cobranca.chargeDate().toString() : "N/A").append(",");
            csv.append(escapeCsv(cobranca.status())).append(",");
            csv.append(cobranca.daysOverdue()).append(",");
            csv.append(cobranca.paidAt() != null ? cobranca.paidAt().toString() : "N/A").append("\n");
        }
        
        return csv.toString().getBytes(StandardCharsets.UTF_8);
    }

    @Override
    public byte[] exportMetricasToPdf(AuthorStatsResponse metricas, String authorName) {
        try (PDDocument document = new PDDocument();
             ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            
            PDPage page = new PDPage(PDRectangle.A4);
            document.addPage(page);
            
            PDPageContentStream contentStream = new PDPageContentStream(document, page);
            try {
                float yPosition = 750;
                float margin = 50;
                float lineHeight = 20;
                
                // Título
                contentStream.beginText();
                contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD), 18);
                contentStream.newLineAtOffset(margin, yPosition);
                contentStream.showText("Relatório de Métricas");
                contentStream.endText();
                yPosition -= 30;
                
                // Nome do Autor
                contentStream.beginText();
                contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), 12);
                contentStream.newLineAtOffset(margin, yPosition);
                contentStream.showText("Autor: " + (authorName != null ? authorName : metricas.authorName()));
                contentStream.endText();
                yPosition -= 40;
                
                // Métricas
                contentStream.beginText();
                contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD), 14);
                contentStream.newLineAtOffset(margin, yPosition);
                contentStream.showText("Estatísticas Gerais");
                contentStream.endText();
                yPosition -= 25;
                
                yPosition = addTextLine(contentStream, 
                    "Total de Livros: " + metricas.totalBooks(), margin, yPosition, lineHeight);
                yPosition = addTextLine(contentStream, 
                    "Pedidos Completados: " + metricas.completedOrders(), margin, yPosition, lineHeight);
                yPosition = addTextLine(contentStream, 
                    "Receita Total: R$ " + String.format("%.2f", metricas.totalRevenue().doubleValue()), 
                    margin, yPosition, lineHeight);
                yPosition = addTextLine(contentStream, 
                    "Total de Pagamentos: " + metricas.totalPayouts(), margin, yPosition, lineHeight);
                yPosition = addTextLine(contentStream, 
                    "Total Pago: R$ " + String.format("%.2f", metricas.totalPaid().doubleValue()), 
                    margin, yPosition, lineHeight);
                yPosition -= 20;
                
                // Vendas Recentes (30 dias)
                contentStream.beginText();
                contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD), 14);
                contentStream.newLineAtOffset(margin, yPosition);
                contentStream.showText("Vendas Recentes (30 dias)");
                contentStream.endText();
                yPosition -= 25;
                
                yPosition = addTextLine(contentStream, 
                    "Pedidos: " + metricas.recentOrders(), margin, yPosition, lineHeight);
                yPosition = addTextLine(contentStream, 
                    "Receita: R$ " + String.format("%.2f", metricas.recentRevenue().doubleValue()), 
                    margin, yPosition, lineHeight);
            } finally {
                contentStream.close();
            }
            
            document.save(baos);
            return baos.toByteArray();
            
        } catch (IOException e) {
            log.error("Erro ao gerar PDF de métricas: {}", e.getMessage(), e);
            throw new RuntimeException("Erro ao gerar PDF", e);
        }
    }

    @Override
    public byte[] exportMetricasToCsv(AuthorStatsResponse metricas, String authorName) {
        StringBuilder csv = new StringBuilder();
        
        csv.append("Relatório de Métricas\n");
        csv.append("Autor,").append(authorName != null ? authorName : metricas.authorName()).append("\n\n");
        csv.append("Métrica,Valor\n");
        csv.append("Total de Livros,").append(metricas.totalBooks()).append("\n");
        csv.append("Pedidos Completados,").append(metricas.completedOrders()).append("\n");
        csv.append("Receita Total,").append(String.format("%.2f", metricas.totalRevenue().doubleValue())).append("\n");
        csv.append("Total de Pagamentos,").append(metricas.totalPayouts()).append("\n");
        csv.append("Total Pago,").append(String.format("%.2f", metricas.totalPaid().doubleValue())).append("\n");
        csv.append("Tem Conta de Pagamento,").append(metricas.hasPaymentAccount() ? "SIM" : "NÃO").append("\n");
        csv.append("Pedidos Recentes (30 dias),").append(metricas.recentOrders()).append("\n");
        csv.append("Receita Recente (30 dias),").append(String.format("%.2f", metricas.recentRevenue().doubleValue())).append("\n");
        
        return csv.toString().getBytes(StandardCharsets.UTF_8);
    }

    @Override
    public byte[] exportTicketsToPdf(List<TicketDTO> tickets, String authorName) {
        try (PDDocument document = new PDDocument();
             ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            
            PDPage page = new PDPage(PDRectangle.A4);
            document.addPage(page);
            
            PDPageContentStream contentStream = new PDPageContentStream(document, page);
            try {
                float yPosition = 750;
                float margin = 50;
                float lineHeight = 20;
                
                // Título
                contentStream.beginText();
                contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD), 18);
                contentStream.newLineAtOffset(margin, yPosition);
                contentStream.showText("Relatório de Tickets");
                contentStream.endText();
                yPosition -= 30;
                
                // Nome do Autor
                contentStream.beginText();
                contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), 12);
                contentStream.newLineAtOffset(margin, yPosition);
                contentStream.showText("Autor: " + (authorName != null ? authorName : "N/A"));
                contentStream.endText();
                yPosition -= 30;
                
                // Lista de tickets
                for (TicketDTO ticket : tickets) {
                    if (yPosition < 100) {
                        contentStream.close();
                        PDPage newPage = new PDPage(PDRectangle.A4);
                        document.addPage(newPage);
                        contentStream = new PDPageContentStream(document, newPage);
                        yPosition = 750;
                    }
                    
                    yPosition = addTextLine(contentStream, 
                        ticket.ticketNumber() + " - " + ticket.title(), margin, yPosition, lineHeight);
                    yPosition = addTextLine(contentStream, 
                        "Categoria: " + ticket.category() + " - Status: " + ticket.status(), 
                        margin, yPosition, lineHeight);
                    yPosition = addTextLine(contentStream, 
                        "Criado em: " + (ticket.createdAt() != null ? ticket.createdAt().toString() : "N/A"), 
                        margin, yPosition, lineHeight);
                    yPosition = addTextLine(contentStream, 
                        "Mensagens: " + (ticket.messages() != null ? ticket.messages().size() : 0), 
                        margin, yPosition, lineHeight);
                    yPosition -= 10;
                }
            } finally {
                contentStream.close();
            }
            
            document.save(baos);
            return baos.toByteArray();
            
        } catch (IOException e) {
            log.error("Erro ao gerar PDF de tickets: {}", e.getMessage(), e);
            throw new RuntimeException("Erro ao gerar PDF", e);
        }
    }

    @Override
    public byte[] exportTicketsToCsv(List<TicketDTO> tickets, String authorName) {
        StringBuilder csv = new StringBuilder();
        
        csv.append("Relatório de Tickets\n");
        csv.append("Autor,").append(authorName != null ? authorName : "N/A").append("\n\n");
        csv.append("Número Ticket,Título,Categoria,Status,Criado Em,Atualizado Em,Total Mensagens\n");
        
        for (TicketDTO ticket : tickets) {
            csv.append(escapeCsv(ticket.ticketNumber())).append(",");
            csv.append(escapeCsv(ticket.title())).append(",");
            csv.append(escapeCsv(ticket.category())).append(",");
            csv.append(escapeCsv(ticket.status())).append(",");
            csv.append(ticket.createdAt() != null ? ticket.createdAt().toString() : "N/A").append(",");
            csv.append(ticket.updatedAt() != null ? ticket.updatedAt().toString() : "N/A").append(",");
            csv.append(ticket.messages() != null ? ticket.messages().size() : 0).append("\n");
        }
        
        return csv.toString().getBytes(StandardCharsets.UTF_8);
    }
    
    private String formatInstant(Instant instant) {
        if (instant == null) return "N/A";
        return instant.atZone(ZoneId.systemDefault())
            .format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
    }
    
    private String escapeCsv(String value) {
        if (value == null) return "";
        // Se contém vírgula, aspas ou quebra de linha, envolver em aspas e escapar aspas
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }
}

