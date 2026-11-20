package com.hemovia.api.service.config;

import java.time.Duration;
import jakarta.validation.constraints.Min;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@ConfigurationProperties(prefix = "hemovia.routing")
@Validated
public class RoutingProperties {
    private String baseUrl = "https://router.project-osrm.org";
    private Duration connectTimeout = Duration.ofSeconds(5);
    private Duration requestTimeout = Duration.ofSeconds(15);
    private Duration minimumInterval = Duration.ofSeconds(1);
    @Min(1)
    private long plateletsMaxSeconds = 3600;
    public String getBaseUrl() {
        return baseUrl;
    }

    public void setBaseUrl(String v) {
        baseUrl = v;
    }

    public Duration getConnectTimeout() {
        return connectTimeout;
    }

    public void setConnectTimeout(Duration v) {
        connectTimeout = v;
    }

    public Duration getRequestTimeout() {
        return requestTimeout;
    }

    public void setRequestTimeout(Duration v) {
        requestTimeout = v;
    }

    public Duration getMinimumInterval() {
        return minimumInterval;
    }

    public void setMinimumInterval(Duration v) {
        minimumInterval = v;
    }

    public long getPlateletsMaxSeconds() {
        return plateletsMaxSeconds;
    }

    public void setPlateletsMaxSeconds(long plateletsMaxSeconds) {
        this.plateletsMaxSeconds = plateletsMaxSeconds;
    }
}
