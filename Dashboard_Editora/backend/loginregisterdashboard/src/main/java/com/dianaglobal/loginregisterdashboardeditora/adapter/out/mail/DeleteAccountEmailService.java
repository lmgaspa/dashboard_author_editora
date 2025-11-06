// src/main/java/com/dianaglobal/loginregister/adapter/out/mail/DeleteAccountEmailService.java
package com.dianaglobal.loginregisterdashboardeditora.adapter.out.mail;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailSendException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessagePreparator;
import org.springframework.stereotype.Component;

import com.dianaglobal.loginregisterdashboardeditora.config.MailConfig;
import com.dianaglobal.loginregisterdashboardeditora.config.MailConfig.MailBranding;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class DeleteAccountEmailService {

    private final JavaMailSender mailSender;
    private final MailBranding branding;

    @Value("${mail.username}") private String fromAddress; // só o remetente

    public void send(String toEmail, String name) {
        log.info("[DELETE ACCOUNT EMAIL SERVICE] Starting email send process - To: {}, Name: {}, From: {}", toEmail, name, fromAddress);
        try {
            String subject = "Sua conta foi removida do " + branding.brandName();
            log.debug("[DELETE ACCOUNT EMAIL SERVICE] Subject: {}", subject);
            
            String html = buildHtml(name);
            log.debug("[DELETE ACCOUNT EMAIL SERVICE] HTML content generated (length: {} chars)", html.length());

            MimeMessagePreparator preparator = MailConfig.createPreparator(toEmail, subject, html, fromAddress, branding.brandName());
            log.debug("[DELETE ACCOUNT EMAIL SERVICE] MimeMessagePreparator created, sending email...");
            
            mailSender.send(preparator);
            
            log.info("[DELETE ACCOUNT EMAIL SERVICE] ✅ Delete account email successfully sent to {} (name: {})", toEmail, name);
        } catch (MailSendException e) {
            log.error("[DELETE ACCOUNT EMAIL SERVICE] ❌ Error sending delete account email to {} (name: {}): {}", toEmail, name, e.getMessage(), e);
            throw e; // Re-throw para que o controller possa capturar
        } catch (Exception e) {
            log.error("[DELETE ACCOUNT EMAIL SERVICE] ❌ Unexpected error sending delete account email to {} (name: {}): {}", toEmail, name, e.getMessage(), e);
            throw e; // Re-throw para que o controller possa capturar
        }
    }

    private String buildHtml(String name) {
        String safeName = (name == null || name.isBlank()) ? "você" : escapeHtml(name);
        String logoUrl = branding.safeLogoUrl();

        return """
            <!doctype html>
            <html lang="pt-BR">
            <head>
              <meta charset="utf-8">
              <meta name="viewport" content="width=device-width, initial-scale=1.0"/>
              <title>Sua conta foi removida - %s</title>
              <style>
                img{display:block}
                body{margin:0;padding:0;-webkit-text-size-adjust:100%%;-ms-text-size-adjust:100%%;}
                table{border-collapse:collapse;mso-table-lspace:0pt;mso-table-rspace:0pt;}
                td{border-collapse:collapse;}
                p{margin:0;padding:0;}
                a{text-decoration:none;}
              </style>
            </head>
            <body style="font-family:Arial,Helvetica,sans-serif;background:#f6f7f9;padding:24px">
              <div style="max-width:640px;margin:0 auto;background:#fff;border:1px solid #eee;border-radius:12px;overflow:hidden">
                <div style="background:linear-gradient(135deg,#0a2239,#0e4b68);color:#fff;padding:16px 20px;">
                  <table width="100%%" cellspacing="0" cellpadding="0" style="border-collapse:collapse">
                    <tr>
                      <td style="width:64px;vertical-align:middle;">
                        <img src="%s" alt="%s" width="56" style="display:block;border-radius:6px;">
                      </td>
                      <td style="text-align:right;vertical-align:middle;">
                        <div style="font-weight:700;font-size:18px;line-height:1;"><strong>%s</strong></div>
                        <div style="height:6px;line-height:6px;font-size:0;">&nbsp;</div>
                        <div style="opacity:.9;font-size:12px;line-height:1.2;margin-top:4px;">Notificação de conta</div>
                      </td>
                    </tr>
                  </table>
                </div>

                <div style="padding:24px">
                  <p style="font-size:16px;margin:0 0 12px">Olá, <strong>%s</strong>!</p>
                  <p style="margin:0 0 12px;line-height:1.55">
                    Informamos que sua conta no <strong>%s</strong> foi removida permanentemente.
                  </p>
                  <p style="margin:0 0 12px;line-height:1.55;color:#6b7280;">
                    Todos os dados associados à sua conta foram excluídos de nossos sistemas. Caso tenha alguma dúvida ou precise de assistência, entre em contato conosco através do nosso suporte.
                  </p>
                  <p style="margin:20px 0 0;line-height:1.55;color:#6b7280;font-size:14px;">
                    Esta é uma notificação automática. Por favor, não responda a este e-mail.
                  </p>
                </div>

                %s
              </div>
            </body>
            </html>
            """.formatted(
                branding.brandName(),
                logoUrl,
                branding.brandName(),
                branding.brandName(),
                safeName,
                branding.brandName(),
                EmailFooter.generate()
        );
    }

    private static String escapeHtml(String s) {
        return s.replace("&","&amp;")
                .replace("<","&lt;")
                .replace(">","&gt;")
                .replace("\"","&quot;")
                .replace("'","&#x27;");
    }
}

