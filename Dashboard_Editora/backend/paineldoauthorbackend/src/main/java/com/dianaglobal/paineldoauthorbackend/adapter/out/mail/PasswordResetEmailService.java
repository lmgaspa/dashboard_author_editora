// src/main/java/com/dianaglobal/paineldoauthor/adapter/out/mail/PasswordResetEmailService.java
package com.dianaglobal.paineldoauthorbackend.adapter.out.mail;


import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessagePreparator;
import org.springframework.stereotype.Service;

import com.dianaglobal.paineldoauthorbackend.config.MailConfig;
import com.dianaglobal.paineldoauthorbackend.config.MailConfig.MailBranding;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class PasswordResetEmailService {

    private final JavaMailSender mailSender;
    private final MailBranding branding;

    @Value("${mail.username}") private String fromAddress;

    public void sendPasswordReset(String to, String name, String link, int minutes) {
        try {
            String subject = branding.brandName() + " – Redefinição de Senha";
            String html = buildHtml(name, link, minutes);

            MimeMessagePreparator preparator = MailConfig.createPreparator(to, subject, html, fromAddress, branding.brandName());
            mailSender.send(preparator);
            
            log.info("Password reset e-mail sent to {}", to);
        } catch (MailException e) {
            log.error("Error sending password reset e-mail to {}: {}", to, e.getMessage(), e);
        }
    }

    private String buildHtml(String name, String link, int minutes) {
        String safeName = (name == null || name.isBlank()) ? "cliente" : escapeHtml(name);
        String subtitle = "Redefinição de senha";
        String logoUrl = branding.safeLogoUrl();

        return """
            <!doctype html>
            <html lang="pt-BR">
            <head>
              <meta charset="utf-8">
              <meta name="viewport" content="width=device-width, initial-scale=1.0"/>
              <title>%s – Redefinição de Senha</title>
              <style>
                img{display:block}
                body{margin:0;padding:0;-webkit-text-size-adjust:100%%;-ms-text-size-adjust:100%%;}
                table{border-collapse:collapse;mso-table-lspace:0pt;mso-table-rspace:0pt;}
                td{border-collapse:collapse;}
                p{margin:0;padding:0;}
                a{text-decoration:none;}
              </style>
            </head>
            <body style="font-family:Arial,Helvetica,sans-serif;background:#f6f7f9;padding:24px;margin:0;color:#111827;">
              <div style="max-width:640px;margin:0 auto;background:#fff;border:1px solid #eee;border-radius:12px;overflow:hidden">
                <div style="background:linear-gradient(135deg,#0a2239,#0e4b68);color:#fff;padding:16px 20px;">
                  <table role="presentation" width="100%%" cellspacing="0" cellpadding="0" style="border-collapse:collapse">
                    <tr>
                      <td style="width:64px;vertical-align:middle;">
                        <img src="%s" alt="%s" width="56" height="56" style="display:block;border-radius:6px;width:56px;height:56px;outline:none;border:none;text-decoration:none;-ms-interpolation-mode:bicubic;">
                      </td>
                      <td style="text-align:right;vertical-align:middle;">
                        <div style="font-weight:700;font-size:18px;line-height:1;"><strong>%s</strong></div>
                        <div style="height:6px;line-height:6px;font-size:0;">&nbsp;</div>
                        <div style="opacity:.9;font-size:12px;line-height:1.2;margin-top:4px;">%s</div>
                      </td>
                    </tr>
                  </table>
                </div>
                <div style="padding:24px">
                  <p style="font-size:16px;margin:0 0 12px">Olá, <strong>%s</strong>!</p>
                  <p style="margin:0 0 12px;line-height:1.55">
                    Recebemos uma solicitação para redefinir sua senha em <strong>%s</strong>.
                  </p>
                  <p style="margin:0 0 12px;line-height:1.55">
                    Para continuar, clique no botão abaixo. Este link expira em <strong>%d minutos</strong>.
                  </p>
                  <p style="margin:20px 0">
                    <a href="%s" target="_blank" rel="noopener noreferrer"
                       style="display:inline-block;padding:12px 18px;border-radius:8px;text-decoration:none;background:#111827;color:#fff;font-weight:600;font-size:14px;">
                      Redefinir minha senha
                    </a>
                  </p>
                  <p style="margin:0 0 12px;line-height:1.55">
                    Se você não solicitou esta alteração, pode ignorar este e-mail com segurança.
                  </p>
                  <p style="font-size:12px;color:#6b7280;margin-top:16px;word-break:break-all;white-space:normal;overflow-wrap:break-word;">
                    Se o botão não funcionar, copie e cole este link no seu navegador:<br>%s
                  </p>
                </div>
                %s
              </div>
            </body>
            </html>
            """.formatted(
                branding.brandName(), // title
                logoUrl,              // img src
                branding.brandName(), // alt
                branding.brandName(), // brand text
                subtitle,             // subtitle line
                safeName,
                branding.brandName(),
                minutes,
                link,
                link,
                EmailFooter.generate()
        );
    }

    private static String escapeHtml(String s) {
        return s == null ? "" : s.replace("&","&amp;")
                .replace("<","&lt;").replace(">","&gt;")
                .replace("\"","&quot;").replace("'","&#x27;");
    }
}
