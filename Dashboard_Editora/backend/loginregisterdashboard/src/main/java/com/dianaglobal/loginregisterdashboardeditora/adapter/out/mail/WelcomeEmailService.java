// src/main/java/com/dianaglobal/loginregister/adapter/out/mail/WelcomeEmailService.java
package com.dianaglobal.loginregisterdashboardeditora.adapter.out.mail;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
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
public class WelcomeEmailService {

    private final JavaMailSender mailSender;
    private final MailBranding branding;

    @Value("${mail.username}") private String fromAddress; // só o remetente

    public void send(String toEmail, String name, String plaintextPassword) {
        log.info("[WELCOME EMAIL SERVICE] Starting email send process - To: {}, Name: {}, From: {}, HasPassword: {}",
                toEmail, name, fromAddress, plaintextPassword != null && !plaintextPassword.isBlank());
        try {
            String subject = "🎉 Bem-vindo ao " + branding.brandName() + "!";
            log.debug("[WELCOME EMAIL SERVICE] Subject: {}", subject);
            
            String html = buildHtml(name, plaintextPassword);
            log.debug("[WELCOME EMAIL SERVICE] HTML content generated (length: {} chars)", html.length());

            MimeMessagePreparator preparator = MailConfig.createPreparator(toEmail, subject, html, fromAddress, branding.brandName());
            log.debug("[WELCOME EMAIL SERVICE] MimeMessagePreparator created, sending email...");
            
            mailSender.send(preparator);
            
            log.info("[WELCOME EMAIL SERVICE] ✅ Welcome email successfully sent to {} (name: {})", toEmail, name);
        } catch (MailException e) {
            log.error("[WELCOME EMAIL SERVICE] ❌ Error sending welcome email to {} (name: {}): {}", toEmail, name, e.getMessage(), e);
        }
    }

    private String buildHtml(String name, String plaintextPassword) {
        String safeName = (name == null || name.isBlank()) ? "você" : escapeHtml(name);
        String logoUrl = branding.safeLogoUrl();
        String passwordSection = buildPasswordSection(plaintextPassword);

        return """
            <!doctype html>
            <html lang="pt-BR">
            <head>
              <meta charset="utf-8">
              <meta name="viewport" content="width=device-width, initial-scale=1.0"/>
              <title>Bem-vindo ao %s</title>
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
                        <div style="opacity:.9;font-size:12px;line-height:1.2;margin-top:4px;">Bem-vindo à nossa plataforma</div>
                      </td>
                    </tr>
                  </table>
                </div>

                <div style="padding:24px">
                  <p style="font-size:16px;margin:0 0 12px">Olá, <strong>%s</strong>!</p>
                  <p style="margin:0 0 12px;line-height:1.55">
                    Estamos felizes em tê-lo a bordo. Sua conta foi criada com sucesso em <strong>%s</strong>.
                  </p>
                  %s
                  <p style="margin:20px 0">
                    <a href="%s/login" target="_blank" rel="noopener noreferrer"
                       style="display:inline-block;padding:12px 18px;border-radius:6px;text-decoration:none;
                              background:#111827;color:#fff;font-weight:600">
                      Acessar sua conta
                    </a>
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
                passwordSection,
                branding.frontendUrl(),
                EmailFooter.generate()
        );
    }

    private static String buildPasswordSection(String plaintextPassword) {
        if (plaintextPassword == null || plaintextPassword.isBlank()) {
            return """
                  <p style="margin:0 0 12px;line-height:1.55">
                    Caso tenha escolhido sua senha durante o cadastro, você já pode acessar o painel normalmente.
                  </p>
            """;
        }

        String safePassword = escapeHtml(plaintextPassword);
        return """
                  <div style="margin:16px 0;padding:16px;border:1px dashed #d1d5db;border-radius:8px;background:#f9fafb;">
                    <p style="margin:0 0 8px;font-size:15px;font-weight:600;color:#111827;">Sua senha é:</p>
                    <p style="margin:0 0 12px;font-size:18px;font-weight:700;letter-spacing:1px;">%s</p>
                    <p style="margin:0;line-height:1.55;color:#374151;font-size:14px;">
                      Altere imediatamente após o primeiro acesso: abra <strong>Perfil &gt; Alterar senha</strong> e defina uma senha segura.
                    </p>
                  </div>
            """.formatted(safePassword);
    }

    private static String escapeHtml(String s) {
        return s.replace("&","&amp;")
                .replace("<","&lt;")
                .replace(">","&gt;")
                .replace("\"","&quot;")
                .replace("'","&#x27;");
    }
}
