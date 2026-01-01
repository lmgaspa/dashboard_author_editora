package com.dianaglobal.paineldoauthorbackend.adapter.out.efi;

import com.dianaglobal.paineldoauthorbackend.domain.model.MonthlyCharge;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.security.KeyStore;
import java.time.Duration;
import java.util.Base64;
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
    private final ObjectMapper objectMapper;
    private HttpClient httpClient;
    private String accessToken;
    private long tokenExpiresAt;

    public EfiPayService(ResourceLoader resourceLoader, ObjectMapper objectMapper) {
        this.resourceLoader = resourceLoader;
        this.objectMapper = objectMapper;
    }

    private synchronized HttpClient getHttpClient() {
        if (httpClient == null) {
            try {
                KeyStore keyStore = KeyStore.getInstance("PKCS12");
                Resource resource = resourceLoader.getResource(certPath);

                try (InputStream is = resource.getInputStream()) {
                    // Assuming empty password for the p12 if not provided, or we might need config
                    // The previous turn had 'cert-password: ""' in yaml
                    keyStore.load(is, new char[0]);
                }

                KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
                kmf.init(keyStore, new char[0]);

                SSLContext sslContext = SSLContext.getInstance("TLS");
                sslContext.init(kmf.getKeyManagers(), null, null);

                httpClient = HttpClient.newBuilder()
                        .sslContext(sslContext)
                        .connectTimeout(Duration.ofSeconds(30))
                        .build();

            } catch (Exception e) {
                log.error("Failed to initialize HttpClient with SSL: {}", e.getMessage(), e);
                throw new RuntimeException("Failed to initialize EFI Payment Service", e);
            }
        }
        return httpClient;
    }

    private String getBaseUrl() {
        return sandbox ? "https://pix-h.api.efipay.com.br" : "https://pix.api.efipay.com.br";
    }

    private String getAccessToken() {
        if (accessToken != null && System.currentTimeMillis() < tokenExpiresAt) {
            return accessToken;
        }

        try {
            String credentials = Base64.getEncoder()
                    .encodeToString((clientId + ":" + clientSecret).getBytes(StandardCharsets.UTF_8));

            Map<String, String> body = new HashMap<>();
            body.put("grant_type", "client_credentials");

            String requestBody = objectMapper.writeValueAsString(body);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(getBaseUrl() + "/oauth/token"))
                    .header("Authorization", "Basic " + credentials)
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                    .build();

            HttpResponse<String> response = getHttpClient().send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                JsonNode json = objectMapper.readTree(response.body());
                accessToken = json.get("access_token").asText();
                int expiresIn = json.get("expires_in").asInt();
                tokenExpiresAt = System.currentTimeMillis() + (expiresIn * 1000L) - 60000; // Buffer 1 min
                return accessToken;
            } else {
                throw new RuntimeException("Auth failed: " + response.body());
            }

        } catch (Exception e) {
            log.error("Failed to authenticate with EFI: {}", e.getMessage(), e);
            throw new RuntimeException("EFI Authentication failed", e);
        }
    }

    public void generatePixCharge(MonthlyCharge charge) {
        try {
            String token = getAccessToken();

            // 1. Create Cobrança Imediata
            Map<String, Object> body = new HashMap<>();

            Map<String, Object> calendario = new HashMap<>();
            calendario.put("expiracao", 3600 * 24 * 5); // 5 dias
            body.put("calendario", calendario);

            Map<String, Object> valor = new HashMap<>();
            valor.put("original", String.format("%.2f", charge.getAmount()).replace(",", "."));
            body.put("valor", valor);

            body.put("chave", pixKey);
            body.put("solicitacaoPagador", "Cobrança Mensal - Painel do Autor");

            String jsonBody = objectMapper.writeValueAsString(body);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(getBaseUrl() + "/v2/cob"))
                    .header("Authorization", "Bearer " + token)
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                    .build();

            HttpResponse<String> response = getHttpClient().send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 201) {
                throw new RuntimeException("Failed to create charge: " + response.body());
            }

            JsonNode responseJson = objectMapper.readTree(response.body());
            String txid = responseJson.get("txid").asText();
            charge.setTxid(txid);

            int locationId = responseJson.get("loc").get("id").asInt();
            charge.setLocationId(String.valueOf(locationId));

            // 2. Get QR Code
            HttpRequest qrRequest = HttpRequest.newBuilder()
                    .uri(URI.create(getBaseUrl() + "/v2/loc/" + locationId + "/qrcode"))
                    .header("Authorization", "Bearer " + getAccessToken()) // Refresh if needed
                    .GET()
                    .build();

            HttpResponse<String> qrResponse = getHttpClient().send(qrRequest, HttpResponse.BodyHandlers.ofString());

            if (qrResponse.statusCode() == 200) {
                JsonNode qrJson = objectMapper.readTree(qrResponse.body());
                charge.setPixCode(qrJson.get("qrcode").asText());
                if (qrJson.has("imagemQrcode")) {
                    charge.setPixImageUrl(qrJson.get("imagemQrcode").asText());
                }
            } else {
                throw new RuntimeException("Failed to get QR Code: " + qrResponse.body());
            }

            log.info("[EFI] PIX Charge generated successfully. TxId: {}", txid);

        } catch (Exception e) {
            log.error("[EFI] Error generating PIX charge: {}", e.getMessage(), e);
            throw new RuntimeException("Error generating PIX charge via EFI", e);
        }
    }
}
