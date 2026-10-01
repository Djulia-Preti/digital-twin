package br.senai.twin.model;

import java.time.Instant;

public class Snapshot {

    private Status status = Status.OFFLINE;
    private Telemetry telemetry;
    private Instant updatedAt;

    public Status getStatus() { return status; }
    public void setStatus(Status status) { this.status = status; }

    public Telemetry getTelemetry() { return telemetry; }
    public void setTelemetry(Telemetry telemetry) { this.telemetry = telemetry; }

    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}
