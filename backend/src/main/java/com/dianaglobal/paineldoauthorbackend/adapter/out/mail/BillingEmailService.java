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
        String paymentLink = "https://painel.andeseditora.com.br/user/charges"; // Adjust domain as needed

        return String.format(
                """
                        <html>
                        <body style="font-family: Arial, sans-serif; color: #333;">
                            <div style="max-width: 600px; margin: 0 auto; padding: 20px; border: 1px solid #ddd; border-radius: 8px;">
                                <h2 style="color: #2563eb;">Cobrança Mensal</h2>
                                <p>Olá, <strong>%s</strong>,</p>
                                <p>Sua cobrança referente aos serviços de nuvem do mês de <strong>%s de %d</strong> já está disponível.</p>

                                <div style="background-color: #f8fafc; padding: 15px; border-radius: 6px; margin: 20px 0;">
                                    <p style="margin: 5px 0;"><strong>Valor:</strong> R$ %.2f</p>
                                    <p style="margin: 5px 0;"><strong>Vencimento:</strong> %s</p>
                                </div>

                                <p>Para realizar o pagamento via PIX, clique no botão abaixo para acessar o painel:</p>

                                <div style="text-align: center; margin: 30px 0;">
                                    <a href="%s" style="background-color: #2563eb; color: white; padding: 12px 24px; text-decoration: none; border-radius: 6px; font-weight: bold;">
                                        Para pagar clique aqui
                                    </a>
                                </div>

                                <p style="font-size: 12px; color: #666;">
                                    Caso já tenha efetuado o pagamento, por favor desconsidere este e-mail.
                                </p>
                            </div>
                        </body>
                        </html>
                        """,
                user.getName(),
                getMonthName(charge.getChargeMonth()),
                charge.getChargeYear(),
                charge.getAmount(),
                charge.getDueDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")),
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
