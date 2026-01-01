package com.dianaglobal.paineldoauthorbackend.adapter.out.efi;

import br.com.efi.efisdk.EfiPay;
import br.com.efi.efisdk.exceptions.EfiPayException;
import com.dianaglobal.paineldoauthorbackend.domain.model.MonthlyCharge;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
public class EfiPayService {

    @Value("${efi.pix.client-id}")
    private String clientId;

    @Value("${efi.pix.client-secret}")
    private String clientSecret;

    @Value("${efi.pix.cert-path}")
    private String certPath;

    @Value("${efi.pix.chave}")
    private String pixKey;

    @Value("${efi.pix.sandbox:false}")
    private boolean sandbox;

    private final ResourceLoader resourceLoader;
    private EfiPay efiPay;
    private String tempCertPath;

    public EfiPayService(ResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;
    }

    private synchronized EfiPay getEfiPay() {
        if (efiPay == null) {
            try {
                // Prepare certificate file from classpath to temp file if necessary
                if (certPath.startsWith("classpath:")) {
                    tempCertPath = copyCertToTempFile(certPath);
                } else {
                    tempCertPath = certPath;
                }

                JSONObject options = new JSONObject();
                options.put("client_id", clientId);
                options.put("client_secret", clientSecret);
                options.put("certificate", tempCertPath);
                options.put("sandbox", sandbox);
                options.put("debug", true); // Enable for debugging if needed

                efiPay = new EfiPay(options);
            } catch (Exception e) {
                log.error("Failed to initialize EFI SDK: {}", e.getMessage(), e);
                throw new RuntimeException("Failed to initialize EFI Payment Service", e);
            }
        }
        return efiPay;
    }

    private String copyCertToTempFile(String classpathLocation) throws IOException {
        Resource resource = resourceLoader.getResource(classpathLocation);
        try (InputStream inputStream = resource.getInputStream()) {
            File tempFile = Files.createTempFile("efi-cert-", ".p12").toFile();
            tempFile.deleteOnExit();
            try (FileOutputStream out = new FileOutputStream(tempFile)) {
                byte[] buffer = new byte[1024];
                int bytesRead;
                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    out.write(buffer, 0, bytesRead);
                }
            }
            return tempFile.getAbsolutePath();
        }
    }

    public void generatePixCharge(MonthlyCharge charge) {
        try {
            EfiPay efi = getEfiPay();

            // 1. Create Cobrança Imediata (pixCreateImmediateCharge)
            JSONObject body = new JSONObject();

            JSONObject calendario = new JSONObject();
            calendario.put("expiracao", 3600 * 24 * 5); // 5 dias
            body.put("calendario", calendario);

            JSONObject valor = new JSONObject();
            valor.put("original", String.format("%.2f", charge.getAmount()).replace(",", "."));
            body.put("valor", valor);

            body.put("chave", pixKey);
            body.put("solicitacaoPagador", "Cobrança Mensal - Painel do Autor");

            // Execute creation
            // Second argument is path params (null for this endpoint)
            JSONObject response = efi.call("pixCreateImmediateCharge", new HashMap<>(), body);

            if (!response.has("txid")) {
                throw new RuntimeException("Response does not contain txid: " + response.toString());
            }

            String txid = response.getString("txid");
            charge.setTxid(txid);

            int locationId = response.getJSONObject("loc").getInt("id");
            charge.setLocationId(String.valueOf(locationId));

            // 2. Get QR Code (pixGenerateQRCode)
            Map<String, String> params = new HashMap<>();
            params.put("id", String.valueOf(locationId));

            JSONObject qrResponse = efi.call("pixGenerateQRCode", params, new JSONObject());

            if (qrResponse.has("qrcode")) {
                charge.setPixCode(qrResponse.getString("qrcode"));
                if (qrResponse.has("imagemQrcode")) {
                    charge.setPixImageUrl(qrResponse.getString("imagemQrcode"));
                }
            } else {
                throw new RuntimeException("Failed to get QR Code: " + qrResponse.toString());
            }

            log.info("[EFI] PIX Charge generated successfully. TxId: {}", txid);

        } catch (EfiPayException e) {
            log.error("[EFI] API Exception: {} - {} - {}", e.getError(), e.getErrorDescription(), e.getMessage());
            throw new RuntimeException("Error generating PIX charge via EFI: " + e.getErrorDescription(), e);
        } catch (Exception e) {
            log.error("[EFI] Generic Error generating PIX charge: {}", e.getMessage(), e);
            throw new RuntimeException("Error generating PIX charge via EFI", e);
        }
    }
}
