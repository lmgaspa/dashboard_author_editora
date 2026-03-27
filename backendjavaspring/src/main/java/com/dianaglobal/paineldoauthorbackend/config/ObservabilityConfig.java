package com.dianaglobal.paineldoauthorbackend.config;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Custom business metrics exposed via /actuator/prometheus.
 * Scraped by Prometheus and forwarded to Grafana Cloud.
 */
@Configuration
public class ObservabilityConfig {

    // ── Tickets ───────────────────────────────────────────────────────────────

    @Bean
    public Counter ticketsCreatedCounter(MeterRegistry registry) {
        return Counter.builder("tickets.created.total")
                .description("Total support tickets created by authors")
                .register(registry);
    }

    @Bean
    public Counter ticketsResolvedCounter(MeterRegistry registry) {
        return Counter.builder("tickets.resolved.total")
                .description("Total support tickets resolved")
                .register(registry);
    }

    // ── Monthly Charges ───────────────────────────────────────────────────────

    @Bean
    public Counter chargesCreatedCounter(MeterRegistry registry) {
        return Counter.builder("charges.created.total")
                .description("Total monthly charges created by admins")
                .register(registry);
    }

    @Bean
    public Counter paymentsConfirmedCounter(MeterRegistry registry) {
        return Counter.builder("payments.confirmed.total")
                .description("Total charge payments confirmed by admins")
                .register(registry);
    }

    // ── Auth ──────────────────────────────────────────────────────────────────

    @Bean
    public Counter loginSuccessCounter(MeterRegistry registry) {
        return Counter.builder("auth.login.success.total")
                .description("Total successful logins")
                .register(registry);
    }

    @Bean
    public Counter loginFailureCounter(MeterRegistry registry) {
        return Counter.builder("auth.login.failure.total")
                .description("Total failed login attempts")
                .register(registry);
    }

    @Bean
    public Counter passwordResetRequestedCounter(MeterRegistry registry) {
        return Counter.builder("auth.password_reset.requested.total")
                .description("Total password reset requests")
                .register(registry);
    }

    // ── Users ─────────────────────────────────────────────────────────────────

    @Bean
    public Counter usersCreatedCounter(MeterRegistry registry) {
        return Counter.builder("users.created.total")
                .description("Total user accounts created by admins")
                .register(registry);
    }

    // ── Exports ───────────────────────────────────────────────────────────────

    @Bean
    public Counter exportsRequestedCounter(MeterRegistry registry) {
        return Counter.builder("exports.requested.total")
                .description("Total export requests across all modules")
                .tag("format", "all")
                .register(registry);
    }
}
