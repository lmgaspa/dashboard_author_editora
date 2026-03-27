package email

import (
	"bytes"
	"fmt"
	"html/template"
	"time"
)

const (
	brandName = "Painel do Autor da Editora Via Litterarum"
	logoURL   = "https://www.andescoresoftware.com.br/AndesCore.jpg"
)

// emailLayout wraps the body content with the standard header and footer used across all emails.
func emailLayout(subtitle, body string) string {
	year := time.Now().Year()
	return fmt.Sprintf(`<!doctype html>
<html lang="pt-BR">
<head>
  <meta charset="utf-8">
  <meta name="viewport" content="width=device-width,initial-scale=1.0"/>
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

    <!-- Header -->
    <div style="background:linear-gradient(135deg,#0a2239,#0e4b68);color:#fff;padding:16px 20px;">
      <table width="100%%" cellspacing="0" cellpadding="0" style="border-collapse:collapse">
        <tr>
          <td style="width:64px;vertical-align:middle;">
            <img src="%s" alt="%s" width="56" style="display:block;border-radius:6px;">
          </td>
          <td style="text-align:right;vertical-align:middle;">
            <div style="font-weight:700;font-size:18px;line-height:1;"><strong>%s</strong></div>
            <div style="height:6px;line-height:6px;font-size:0;">&nbsp;</div>
            <div style="opacity:.9;font-size:12px;line-height:1.2;margin-top:4px;">%s</div>
          </td>
        </tr>
      </table>
    </div>

    <!-- Body -->
    <div style="padding:24px">
      %s
    </div>

    <!-- Footer -->
    <div style="background:linear-gradient(135deg,#0a2239,#0e4b68);color:#fff;padding:16px 18px;text-align:center;font-size:14px;line-height:1;">
      &copy; %d &middot; Powered by <strong>AndesCore Software</strong>
    </div>

  </div>
</body>
</html>`, logoURL, brandName, brandName, subtitle, body, year)
}

// ─── Billing templates ────────────────────────────────────────────────────────

var billingChargeCreatedTpl = template.Must(template.New("billing.charge.created").Parse(`
{{- define "body" -}}
<p style="font-size:16px;margin:0 0 12px">Olá, <strong>{{.UserName}}</strong>!</p>
<p style="margin:0 0 12px;line-height:1.55">
  Uma nova cobrança foi gerada para o mês de <strong>{{.ChargeMonth}}/{{.ChargeYear}}</strong>.
</p>
<table width="100%" cellspacing="0" cellpadding="0" style="border-collapse:collapse;margin:16px 0;border:1px solid #e5e7eb;border-radius:8px;overflow:hidden">
  <tr style="background:#f9fafb;">
    <td style="padding:10px 14px;font-size:14px;color:#374151;font-weight:600;border-bottom:1px solid #e5e7eb;">Valor</td>
    <td style="padding:10px 14px;font-size:14px;color:#111827;border-bottom:1px solid #e5e7eb;">R$ {{printf "%.2f" .Amount}}</td>
  </tr>
  <tr>
    <td style="padding:10px 14px;font-size:14px;color:#374151;font-weight:600;">Vencimento</td>
    <td style="padding:10px 14px;font-size:14px;color:#111827;">{{.DueDate}}</td>
  </tr>
</table>
{{if .PixCode}}
<div style="margin:16px 0;padding:16px;border:1px dashed #d1d5db;border-radius:8px;background:#f9fafb;">
  <p style="margin:0 0 8px;font-size:14px;font-weight:600;color:#111827;">Código PIX (copia e cola):</p>
  <p style="margin:0;font-size:13px;word-break:break-all;color:#374151;font-family:monospace;">{{.PixCode}}</p>
</div>
{{end}}
{{if .PixImageURL}}
<p style="margin:16px 0 0;text-align:center;">
  <img src="{{.PixImageURL}}" alt="QR Code PIX" style="max-width:200px;border-radius:8px;border:1px solid #e5e7eb;">
</p>
{{end}}
<p style="margin:20px 0 0;line-height:1.55;color:#374151;">Em caso de dúvidas, entre em contato com nossa equipe.</p>
{{- end -}}
`))

var billingPaymentConfirmedTpl = template.Must(template.New("billing.payment.confirmed").Parse(`
{{- define "body" -}}
<p style="font-size:16px;margin:0 0 12px">Olá, <strong>{{.UserName}}</strong>!</p>
<p style="margin:0 0 12px;line-height:1.55">
  Seu pagamento referente ao mês de <strong>{{.ChargeMonth}}/{{.ChargeYear}}</strong> foi confirmado com sucesso. ✅
</p>
<table width="100%" cellspacing="0" cellpadding="0" style="border-collapse:collapse;margin:16px 0;border:1px solid #e5e7eb;border-radius:8px;overflow:hidden">
  <tr style="background:#f9fafb;">
    <td style="padding:10px 14px;font-size:14px;color:#374151;font-weight:600;border-bottom:1px solid #e5e7eb;">Valor pago</td>
    <td style="padding:10px 14px;font-size:14px;color:#111827;border-bottom:1px solid #e5e7eb;">R$ {{printf "%.2f" .Amount}}</td>
  </tr>
  {{if .ConfirmedAt}}
  <tr>
    <td style="padding:10px 14px;font-size:14px;color:#374151;font-weight:600;">Data de confirmação</td>
    <td style="padding:10px 14px;font-size:14px;color:#111827;">{{.ConfirmedAt}}</td>
  </tr>
  {{end}}
</table>
<p style="margin:16px 0 0;line-height:1.55;color:#374151;">Obrigado por manter seus pagamentos em dia!</p>
{{- end -}}
`))

var billingChargeOverdueTpl = template.Must(template.New("billing.charge.overdue").Parse(`
{{- define "body" -}}
<p style="font-size:16px;margin:0 0 12px">Atenção, <strong>{{.UserName}}</strong>!</p>
<p style="margin:0 0 12px;line-height:1.55">
  Identificamos que sua cobrança do mês de <strong>{{.ChargeMonth}}/{{.ChargeYear}}</strong> está em atraso.
</p>
<table width="100%" cellspacing="0" cellpadding="0" style="border-collapse:collapse;margin:16px 0;border:1px solid #fca5a5;border-radius:8px;overflow:hidden">
  <tr style="background:#fef2f2;">
    <td style="padding:10px 14px;font-size:14px;color:#374151;font-weight:600;border-bottom:1px solid #fca5a5;">Valor</td>
    <td style="padding:10px 14px;font-size:14px;color:#111827;border-bottom:1px solid #fca5a5;">R$ {{printf "%.2f" .Amount}}</td>
  </tr>
  <tr style="background:#fef2f2;">
    <td style="padding:10px 14px;font-size:14px;color:#374151;font-weight:600;border-bottom:1px solid #fca5a5;">Vencimento</td>
    <td style="padding:10px 14px;font-size:14px;color:#111827;border-bottom:1px solid #fca5a5;">{{.DueDate}}</td>
  </tr>
  <tr style="background:#fef2f2;">
    <td style="padding:10px 14px;font-size:14px;color:#dc2626;font-weight:600;">Dias em atraso</td>
    <td style="padding:10px 14px;font-size:14px;color:#dc2626;font-weight:700;">{{.DaysOverdue}} dias</td>
  </tr>
</table>
<p style="margin:16px 0 0;line-height:1.55;color:#374151;">
  Por favor, regularize sua situação o quanto antes para evitar a suspensão do acesso à plataforma.
</p>
{{- end -}}
`))

// ─── Auth templates ───────────────────────────────────────────────────────────

var authPasswordResetTpl = template.Must(template.New("auth.password.reset").Parse(`
{{- define "body" -}}
<p style="font-size:16px;margin:0 0 12px">Olá, <strong>{{.UserName}}</strong>!</p>
<p style="margin:0 0 16px;line-height:1.55">
  Recebemos uma solicitação para redefinir a senha da sua conta. Clique no botão abaixo para criar uma nova senha:
</p>
<p style="margin:0 0 16px;">
  <a href="{{.ActionURL}}"
     style="display:inline-block;padding:12px 18px;border-radius:6px;text-decoration:none;background:#111827;color:#fff;font-weight:600;">
    Redefinir Senha
  </a>
</p>
<p style="margin:0;line-height:1.55;color:#6b7280;font-size:14px;">
  O link é válido por <strong>1 hora</strong>. Se você não solicitou a redefinição, ignore este e-mail.
</p>
{{- end -}}
`))

var authAccountConfirmTpl = template.Must(template.New("auth.account.confirm").Parse(`
{{- define "body" -}}
<p style="font-size:16px;margin:0 0 12px">Olá, <strong>{{.UserName}}</strong>!</p>
<p style="margin:0 0 16px;line-height:1.55">
  Obrigado por se cadastrar. Confirme sua conta clicando no botão abaixo:
</p>
<p style="margin:0 0 16px;">
  <a href="{{.ActionURL}}"
     style="display:inline-block;padding:12px 18px;border-radius:6px;text-decoration:none;background:#111827;color:#fff;font-weight:600;">
    Confirmar Conta
  </a>
</p>
<p style="margin:0;line-height:1.55;color:#6b7280;font-size:14px;">
  O link é válido por <strong>24 horas</strong>.
</p>
{{- end -}}
`))

var authWelcomeTpl = template.Must(template.New("auth.welcome").Parse(`
{{- define "body" -}}
<p style="font-size:16px;margin:0 0 12px">Bem-vindo, <strong>{{.UserName}}</strong>!</p>
<p style="margin:0 0 16px;line-height:1.55">
  Sua conta foi criada com sucesso na plataforma <strong>Andes Editora</strong>.
</p>
<p style="margin:0 0 16px;">
  <a href="{{.ActionURL}}"
     style="display:inline-block;padding:12px 18px;border-radius:6px;text-decoration:none;background:#111827;color:#fff;font-weight:600;">
    Acessar sua conta
  </a>
</p>
<p style="margin:0;line-height:1.55;color:#374151;">
  Se precisar de ajuda, nossa equipe está à disposição.
</p>
{{- end -}}
`))

// ─── Renderers ────────────────────────────────────────────────────────────────

func renderChargeEmail(eventType string, data *ChargeEmailEvent) (string, string, error) {
	var tpl *template.Template
	var subject, subtitle string

	switch eventType {
	case "billing.charge.created":
		tpl = billingChargeCreatedTpl
		subject = fmt.Sprintf("Nova cobrança - %02d/%d", data.ChargeMonth, data.ChargeYear)
		subtitle = "Notificação de cobrança"
	case "billing.payment.confirmed":
		tpl = billingPaymentConfirmedTpl
		subject = fmt.Sprintf("Pagamento confirmado - %02d/%d", data.ChargeMonth, data.ChargeYear)
		subtitle = "Confirmação de pagamento"
	case "billing.charge.overdue":
		tpl = billingChargeOverdueTpl
		subject = fmt.Sprintf("Cobrança em atraso - %02d/%d", data.ChargeMonth, data.ChargeYear)
		subtitle = "Aviso de cobrança em atraso"
	default:
		return "", "", fmt.Errorf("unknown charge event type: %s", eventType)
	}

	var buf bytes.Buffer
	if err := tpl.ExecuteTemplate(&buf, "body", data); err != nil {
		return "", "", fmt.Errorf("render template %s: %w", eventType, err)
	}
	return subject, emailLayout(subtitle, buf.String()), nil
}

func renderAuthEmail(eventType string, data *AuthEmailEvent) (string, string, error) {
	var tpl *template.Template
	var subject, subtitle string

	switch eventType {
	case "auth.password.reset":
		tpl = authPasswordResetTpl
		subject = "Redefinição de senha"
		subtitle = "Segurança da conta"
	case "auth.account.confirm":
		tpl = authAccountConfirmTpl
		subject = "Confirme sua conta"
		subtitle = "Verificação de e-mail"
	case "auth.welcome":
		tpl = authWelcomeTpl
		subject = "Bem-vindo ao Painel do Autor!"
		subtitle = "Bem-vindo à nossa plataforma"
	default:
		return "", "", fmt.Errorf("unknown auth event type: %s", eventType)
	}

	var buf bytes.Buffer
	if err := tpl.ExecuteTemplate(&buf, "body", data); err != nil {
		return "", "", fmt.Errorf("render template %s: %w", eventType, err)
	}
	return subject, emailLayout(subtitle, buf.String()), nil
}
