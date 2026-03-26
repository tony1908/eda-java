package com.edacourse.api.saga.domain.model;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * Estado de una Saga de checkout.
 * Persiste el progreso para poder compensar en caso de fallo.
 */
public class SagaState {
    private String sagaId;
    private String orderId;
    private String status; // STARTED, INVENTORY_RESERVED, PAYMENT_COMPLETED, COMPLETED, COMPENSATING, FAILED
    private String currentStep;
    private List<String> completedSteps = new ArrayList<>();
    private String failureReason;
    private Instant startedAt;
    private Instant completedAt;

    public SagaState(String sagaId, String orderId) {
        this.sagaId = sagaId;
        this.orderId = orderId;
        this.status = "STARTED";
        this.currentStep = "RESERVE_INVENTORY";
        this.startedAt = Instant.now();
    }

    // For loading from DB
    public SagaState() {}

    public void advanceTo(String step) {
        completedSteps.add(currentStep);
        currentStep = step;
    }

    public void complete() {
        completedSteps.add(currentStep);
        status = "COMPLETED";
        currentStep = "DONE";
        completedAt = Instant.now();
    }

    public void fail(String reason) {
        status = completedSteps.isEmpty() ? "FAILED" : "COMPENSATING";
        failureReason = reason;
    }

    public void compensated() {
        status = "FAILED";
        completedAt = Instant.now();
    }

    public boolean needsCompensation() {
        return "COMPENSATING".equals(status) && !completedSteps.isEmpty();
    }

    /**
     * Returns completed steps in reverse order for compensation.
     */
    public List<String> getCompensationSteps() {
        List<String> reversed = new ArrayList<>(completedSteps);
        java.util.Collections.reverse(reversed);
        return reversed;
    }

    // Getters and setters
    public String getSagaId() { return sagaId; }
    public void setSagaId(String sagaId) { this.sagaId = sagaId; }
    public String getOrderId() { return orderId; }
    public void setOrderId(String orderId) { this.orderId = orderId; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getCurrentStep() { return currentStep; }
    public void setCurrentStep(String currentStep) { this.currentStep = currentStep; }
    public List<String> getCompletedSteps() { return completedSteps; }
    public void setCompletedSteps(List<String> completedSteps) { this.completedSteps = completedSteps; }
    public String getFailureReason() { return failureReason; }
    public void setFailureReason(String failureReason) { this.failureReason = failureReason; }
    public Instant getStartedAt() { return startedAt; }
    public void setStartedAt(Instant startedAt) { this.startedAt = startedAt; }
    public Instant getCompletedAt() { return completedAt; }
    public void setCompletedAt(Instant completedAt) { this.completedAt = completedAt; }
}
