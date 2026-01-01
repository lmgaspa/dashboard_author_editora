package com.dianaglobal.paineldoauthorbackend.adapter.out.mail;

import com.dianaglobal.paineldoauthorbackend.domain.model.MonthlyCharge;
import com.dianaglobal.paineldoauthorbackend.domain.model.User;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;

@Slf4j
@Service
@RequiredArgsConstructor
public class BillingEmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    @Async
    public void sendBillingEmail(User user, MonthlyCharge charge) {
        try {
            log.info("[BILLING EMAIL] Sending email to {}", user.getEmail());

            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(user.getEmail());

            String subject = String.format("Cobrança Mensal - %s/%d - Painel do Autor",
                    getMonthName(charge.getChargeMonth()), charge.getChargeYear());
            helper.setSubject(subject);

            String body = buildEmailBody(user, charge);
            helper.setText(body, true); // true = html

            mailSender.send(message);
            log.info("[BILLING EMAIL] Sent successfully to {}", user.getEmail());

        } catch (MessagingException e) {
            log.error("[BILLING EMAIL] Failed to send email to {}: {}", user.getEmail(), e.getMessage());
        }
    }

    private String buildEmailBody(User user, MonthlyCharge charge) {
        String paymentLink = "https://painel.andeseditora.com.br/user/charges";
        String pixCode = charge.getPixCode() != null ? charge.getPixCode() : "Código PIX indisponível no momento.";
        String qrCodeImg = charge.getPixImageUrl() != null ? charge.getPixImageUrl() : "";
        String dueDate = charge.getDueDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
        String amount = String.format("%.2f", charge.getAmount());

        return String.format(
                """
                        <html>
                        <body style="font-family: 'Helvetica Neue', Helvetica, Arial, sans-serif; color: #333; background-color: #f4f4f4; margin: 0; padding: 40px 0;">
                            <div style="max-width: 600px; margin: 0 auto; background-color: #ffffff; padding: 40px; border-radius: 12px; box-shadow: 0 4px 6px rgba(0,0,0,0.05);">

                                <div style="text-align: center; margin-bottom: 30px;">
                                    <h2 style="color: #1e293b; margin: 0; font-size: 24px;">Cobrança Mensal</h2>
                                    <p style="color: #64748b; margin-top: 5px;">Painel do Autor</p>
                                </div>

                                <p style="font-size: 16px; line-height: 1.5;">Olá, <strong>%s</strong>,</p>
                                <p style="font-size: 16px; line-height: 1.5;">Sua fatura referente a <strong>%s de %d</strong> já está fechada e disponível para pagamento.</p>

                                <div style="background-color: #f8fafc; padding: 20px; border-radius: 8px; margin: 25px 0; border: 1px solid #e2e8f0;">
                                    <table style="width: 100%%; border-collapse: collapse;">
                                        <tr>
                                            <td style="padding-bottom: 8px; color: #64748b;">Valor a pagar:</td>
                                            <td style="padding-bottom: 8px; text-align: right; font-weight: bold; font-size: 18px; color: #0f172a;">R$ %s</td>
                                        </tr>
                                        <tr>
                                            <td style="color: #64748b;">Vencimento:</td>
                                            <td style="text-align: right; font-weight: bold; color: #0f172a;">%s</td>
                                        </tr>
                                    </table>
                                </div>

                                <div style="text-align: center; margin-top: 30px; border-top: 1px dashed #cbd5e1; padding-top: 30px;">
                                    <p style="font-weight: bold; margin-bottom: 20px; color: #334155; font-size: 18px;">Escaneie o QR Code com seu app do banco:</p>

                                    <!-- QR Code Image -->
                                    <div style="margin-bottom: 25px;">
                                        <img src="%s" alt="QR Code PIX" style="width: 200px; height: 200px; border: 1px solid #e2e8f0; padding: 10px; border-radius: 8px;" />
                                    </div>

                                    <p style="margin-bottom: 10px; color: #334155; font-weight: 500;">Ou copie e cole no seu app:</p>

                                    <!-- Copy Paste Box -->
                                    <div style="background-color: #f1f5f9; padding: 15px; border-radius: 6px; border: 1px solid #cbd5e1; word-break: break-all; font-family: monospace; color: #475569; font-size: 13px; text-align: left; margin-bottom: 10px;">
                                        %s
                                    </div>
                                    <p style="font-size: 12px; color: #94a3b8; margin-top: 5px;">Este código é válido até o vencimento.</p>
                                </div>

                                <div style="text-align: center; margin-top: 40px;">
                                    <a href="%s" style="background-color: #2563eb; color: white; padding: 14px 28px; text-decoration: none; border-radius: 6px; font-weight: bold; font-size: 16px; display: inline-block;">
                                        Acessar Painel
                                    </a>
                                </div>

                                <p style="font-size: 12px; color: #94a3b8; text-align: center; margin-top: 40px;">
                                    Caso já tenha efetuado o pagamento, por favor desconsidere este e-mail.
                                </p>
                            </div>
                        </body>
                        </html>
                        """,
                user.getName(),
                getMonthName(charge.getChargeMonth()),
                charge.getChargeYear(),
                amount,
                dueDate,
                qrCodeImg,
                pixCode,
                paymentLink);
    }

    private String getMonthName(int month) {
        String[] months = {
                "", "Janeiro", "Fevereiro", "Março", "Abril", "Maio", "Junho",
                "Julho", "Agosto", "Setembro", "Outubro", "Novembro", "Dezembro"
        };
        return (month >= 1 && month <= 12) ? months[month] : "";
    }
}
