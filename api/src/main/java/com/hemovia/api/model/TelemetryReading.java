package com.hemovia.api.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/** A received temperature sample. Temperature is Celsius without a clinical range assumption. */
@Entity
@Table(name = "telemetry_readings")
public class TelemetryReading {
    @Id @GeneratedValue(strategy = GenerationType.UUID) private UUID id;

    @NotNull @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "transport_id", nullable = false)
    private Transport transport;

    @NotBlank @Column(name = "sensor_id", nullable = false)
    private String sensorId;

    @NotNull @Column(name = "measured_at", nullable = false)
    private Instant measuredAt;

    @NotNull @Column(name = "received_at", nullable = false)
    private Instant receivedAt;

    @NotNull @Column(name = "temperature_celsius", nullable = false)
    private BigDecimal temperatureCelsius;

    public TelemetryReading() { }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public Transport getTransport() { return transport; }
    public void setTransport(Transport transport) { this.transport = require(transport, "transport"); }
    public String getSensorId() { return sensorId; }
    public void setSensorId(String sensorId) {
        if (sensorId == null || sensorId.isBlank()) throw new IllegalArgumentException("sensorId must not be blank");
        this.sensorId = sensorId;
    }
    public Instant getMeasuredAt() { return measuredAt; }
    public void setMeasuredAt(Instant measuredAt) { this.measuredAt = require(measuredAt, "measuredAt"); }
    public Instant getReceivedAt() { return receivedAt; }
    public void setReceivedAt(Instant receivedAt) { this.receivedAt = require(receivedAt, "receivedAt"); }
    public BigDecimal getTemperatureCelsius() { return temperatureCelsius; }
    public void setTemperatureCelsius(BigDecimal temperatureCelsius) { this.temperatureCelsius = require(temperatureCelsius, "temperatureCelsius"); }

    private static <T> T require(T value, String field) {
        if (value == null) throw new IllegalArgumentException(field + " must not be null");
        return value;
    }
}
