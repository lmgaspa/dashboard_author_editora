package com.dianaglobal.paineldoauthorbackend.application.service;

import com.dianaglobal.paineldoauthorbackend.adapter.in.dto.emails.PainelEmailsAutorDTO;
import com.dianaglobal.paineldoauthorbackend.adapter.in.dto.emails.ResumoEmailClienteDTO;
import com.dianaglobal.paineldoauthorbackend.adapter.in.dto.emails.ResumoEmailRepasseDTO;
import com.dianaglobal.paineldoauthorbackend.adapter.in.dto.pagamentos.PainelPagamentosAutorDTO;
import com.dianaglobal.paineldoauthorbackend.adapter.in.dto.pagamentos.VendaRecenteDTO;
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
import java.time.format.DateTimeFormatter;

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
                            email.valorTotalConfirmado() != null ? email.valorTotalConfirmado().doubleValue() : 0.0);
                        
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
                        
                        String linha = String.format("%s - Pedido #%d - R$ %.2f - %s",
                            email.emailDestinatario() != null ? email.emailDestinatario() : "N/A",
                            email.pedidoId() != null ? email.pedidoId() : "N/A",
                            email.valorRepassado() != null ? email.valorRepassado().doubleValue() : 0.0,
                            email.status() != null ? email.status() : "N/A");
                        
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
            csv.append("E-mail,Total Pedidos,Pedidos Confirmados,Valor Total Confirmado,Primeiro Pedido,Último Pedido\n");
            
            for (ResumoEmailClienteDTO email : painel.emailsClientes()) {
                csv.append(escapeCsv(email.email())).append(",");
                csv.append(email.totalPedidos()).append(",");
                csv.append(email.totalPedidosConfirmados()).append(",");
                csv.append(email.valorTotalConfirmado() != null ? 
                    String.format("%.2f", email.valorTotalConfirmado().doubleValue()) : "0.00").append(",");
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
            csv.append("E-mail,Order ID,Valor Repassado,Status,Data Envio\n");
            
            for (ResumoEmailRepasseDTO email : painel.emailsRepasse()) {
                csv.append(escapeCsv(email.emailDestinatario() != null ? email.emailDestinatario() : "N/A")).append(",");
                csv.append(email.pedidoId() != null ? email.pedidoId() : "N/A").append(",");
                csv.append(email.valorRepassado() != null ? 
                    String.format("%.2f", email.valorRepassado().doubleValue()) : "0.00").append(",");
                csv.append(escapeCsv(email.status() != null ? email.status() : "N/A")).append(",");
                csv.append(email.enviadoEm() != null ? email.enviadoEm().toString() : "N/A").append("\n");
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
    
    private String escapeCsv(String value) {
        if (value == null) return "";
        // Se contém vírgula, aspas ou quebra de linha, envolver em aspas e escapar aspas
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }
}

