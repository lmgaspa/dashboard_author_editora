package com.dianaglobal.paineldoauthorbackend.adapter.out.grpc;

import com.dianaglobal.paineldoauthorbackend.grpc.export.ExportRequest;
import com.dianaglobal.paineldoauthorbackend.grpc.export.ExportResponse;
import com.dianaglobal.paineldoauthorbackend.grpc.export.ExportServiceGrpc;
import com.dianaglobal.paineldoauthorbackend.grpc.export.Record;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
@ConditionalOnProperty(name = "export.grpc.enabled", havingValue = "true")
public class GrpcExportClient {

    private final ManagedChannel channel;
    private final ExportServiceGrpc.ExportServiceBlockingStub stub;

    public GrpcExportClient(
            @Value("${export.grpc.host:localhost}") String host,
            @Value("${export.grpc.port:50051}") int port) {
        this.channel = ManagedChannelBuilder.forAddress(host, port)
                .usePlaintext()
                .build();
        this.stub = ExportServiceGrpc.newBlockingStub(channel);
        log.info("[GRPC EXPORT] Connected to export-service at {}:{}", host, port);
    }

    public ExportResponse exportCharges(String format, String authorId, List<Map<String, String>> rows) {
        ExportRequest req = buildRequest(format, authorId, rows);
        return stub.exportCharges(req);
    }

    public ExportResponse exportTickets(String format, String authorId, List<Map<String, String>> rows) {
        ExportRequest req = buildRequest(format, authorId, rows);
        return stub.exportTickets(req);
    }

    public ExportResponse exportDeliveries(String format, String authorId, List<Map<String, String>> rows) {
        ExportRequest req = buildRequest(format, authorId, rows);
        return stub.exportDeliveries(req);
    }

    private ExportRequest buildRequest(String format, String authorId, List<Map<String, String>> rows) {
        ExportRequest.Builder builder = ExportRequest.newBuilder()
                .setFormat(format)
                .setAuthorId(authorId != null ? authorId : "");
        for (Map<String, String> row : rows) {
            builder.addRecords(Record.newBuilder().putAllFields(row).build());
        }
        return builder.build();
    }

    @PreDestroy
    public void shutdown() {
        try {
            channel.shutdown().awaitTermination(5, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
