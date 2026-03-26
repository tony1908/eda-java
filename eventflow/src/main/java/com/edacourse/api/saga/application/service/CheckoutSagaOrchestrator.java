package com.edacourse.api.saga.application.service;

import com.edacourse.api.shared.infrastructure.messaging.EventBus;
import com.edacourse.api.saga.domain.model.SagaState;
import com.edacourse.api.saga.domain.event.*;
import com.edacourse.api.saga.infrastructure.persistence.SagaStateRepository;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Orquestador de la Saga de Checkout.
 * Coordina los pasos: Inventory → Payment → Shipping
 * Si falla un paso, ejecuta compensaciones en orden inverso.
 */
public class CheckoutSagaOrchestrator {

    private final EventBus eventBus;
    private final SagaStateRepository sagaRepo;

    public CheckoutSagaOrchestrator(EventBus eventBus, SagaStateRepository sagaRepo) {
        this.eventBus = eventBus;
        this.sagaRepo = sagaRepo;

        // Escuchar respuestas de cada servicio
        eventBus.subscribe("inventory.reserved", Object.class,
            this::onInventoryReserved, "saga-orchestrator");
        eventBus.subscribe("inventory.insufficient", Object.class,
            this::onInventoryFailed, "saga-orchestrator");
        eventBus.subscribe("payment.completed", Object.class,
            this::onPaymentCompleted, "saga-orchestrator");
        eventBus.subscribe("payment.failed", Object.class,
            this::onPaymentFailed, "saga-orchestrator");
        eventBus.subscribe("shipping.shipped", Object.class,
            this::onShippingCompleted, "saga-orchestrator");

        System.out.println("[SAGA] Orquestador de Checkout inicializado — 5 topics suscritos");
    }

    /**
     * Inicia una nueva Saga de checkout para un pedido.
     */
    public String startCheckout(String orderId) {
        String sagaId = "saga-" + UUID.randomUUID().toString().substring(0, 8);
        SagaState saga = new SagaState(sagaId, orderId);
        sagaRepo.save(saga);

        System.out.println("[SAGA] === Checkout iniciado ===");
        System.out.println("[SAGA] Saga ID: " + sagaId + " | Order: " + orderId);
        System.out.println("[SAGA] Paso 1/3: Reservar inventario...");

        eventBus.publish("saga.step.completed",
            new SagaStepCompletedEvent(sagaId, orderId, "CHECKOUT_STARTED", "STARTED", Instant.now()));

        return sagaId;
    }

    // === Step responses ===

    private void onInventoryReserved(Object event) {
        String json = event.toString();
        String orderId = extractField(json, "orderId");
        SagaState saga = findSagaByOrder(orderId);
        if (saga == null || !"STARTED".equals(saga.getStatus())) return;

        saga.setStatus("INVENTORY_RESERVED");
        saga.advanceTo("PROCESS_PAYMENT");
        sagaRepo.save(saga);

        System.out.println("[SAGA] ✓ Inventario reservado para " + orderId);
        System.out.println("[SAGA] Paso 2/3: Procesar pago...");

        eventBus.publish("saga.step.completed",
            new SagaStepCompletedEvent(saga.getSagaId(), orderId, "RESERVE_INVENTORY", "COMPLETED", Instant.now()));
    }

    private void onInventoryFailed(Object event) {
        String json = event.toString();
        String orderId = extractField(json, "orderId");
        SagaState saga = findSagaByOrder(orderId);
        if (saga == null || !"STARTED".equals(saga.getStatus())) return;

        System.out.println("[SAGA] ✗ Inventario insuficiente para " + orderId);
        saga.fail("Inventario insuficiente");
        saga.compensated(); // No hay pasos completados que compensar
        sagaRepo.save(saga);

        eventBus.publish("saga.failed",
            new SagaFailedEvent(saga.getSagaId(), orderId, "RESERVE_INVENTORY", "Inventario insuficiente", List.of(), Instant.now()));
    }

    private void onPaymentCompleted(Object event) {
        String json = event.toString();
        String orderId = extractField(json, "orderId");
        SagaState saga = findSagaByOrder(orderId);
        if (saga == null || !"INVENTORY_RESERVED".equals(saga.getStatus())) return;

        saga.setStatus("PAYMENT_COMPLETED");
        saga.advanceTo("ARRANGE_SHIPPING");
        sagaRepo.save(saga);

        System.out.println("[SAGA] ✓ Pago completado para " + orderId);
        System.out.println("[SAGA] Paso 3/3: Organizar envio...");

        eventBus.publish("saga.step.completed",
            new SagaStepCompletedEvent(saga.getSagaId(), orderId, "PROCESS_PAYMENT", "COMPLETED", Instant.now()));
    }

    private void onPaymentFailed(Object event) {
        String json = event.toString();
        String orderId = extractField(json, "orderId");
        SagaState saga = findSagaByOrder(orderId);
        if (saga == null || !"INVENTORY_RESERVED".equals(saga.getStatus())) return;

        System.out.println("[SAGA] ✗ Pago fallido para " + orderId);
        System.out.println("[SAGA] Compensando: liberar inventario...");

        saga.fail("Pago rechazado");
        sagaRepo.save(saga);

        // Compensar: liberar inventario
        eventBus.publish("inventory.release.requested",
            new InventoryReleasedEvent(saga.getSagaId(), orderId, Instant.now()));

        saga.compensated();
        sagaRepo.save(saga);

        eventBus.publish("saga.failed",
            new SagaFailedEvent(saga.getSagaId(), orderId, "PROCESS_PAYMENT", "Pago rechazado",
                List.of("RELEASE_INVENTORY"), Instant.now()));

        System.out.println("[SAGA] Compensacion completada: inventario liberado");
    }

    private void onShippingCompleted(Object event) {
        String json = event.toString();
        String orderId = extractField(json, "orderId");
        SagaState saga = findSagaByOrder(orderId);
        if (saga == null || !"PAYMENT_COMPLETED".equals(saga.getStatus())) return;

        long duration = System.currentTimeMillis() - saga.getStartedAt().toEpochMilli();
        saga.complete();
        sagaRepo.save(saga);

        System.out.println("[SAGA] ✓ Envio organizado para " + orderId);
        System.out.println("[SAGA] === Checkout COMPLETADO en " + duration + "ms ===");

        eventBus.publish("saga.completed",
            new SagaCompletedEvent(saga.getSagaId(), orderId, duration, Instant.now()));
    }

    // === Queries ===

    public SagaState getSagaState(String sagaId) {
        return sagaRepo.findById(sagaId);
    }

    public List<SagaState> getRecentSagas(int limit) {
        return sagaRepo.findRecent(limit);
    }

    // === Helpers ===

    private SagaState findSagaByOrder(String orderId) {
        if (orderId == null) return null;
        // Buscar saga activa para este orderId
        List<SagaState> recent = sagaRepo.findRecent(100);
        return recent.stream()
            .filter(s -> orderId.equals(s.getOrderId()))
            .filter(s -> !"COMPLETED".equals(s.getStatus()) && !"FAILED".equals(s.getStatus()))
            .findFirst()
            .orElse(null);
    }

    private String extractField(String json, String field) {
        String pattern = "\"" + field + "\"";
        int idx = json.indexOf(pattern);
        if (idx < 0) return null;
        int colonIdx = json.indexOf(":", idx);
        if (colonIdx < 0) return null;
        int start = json.indexOf("\"", colonIdx + 1);
        if (start < 0) return null;
        int end = json.indexOf("\"", start + 1);
        return end > start ? json.substring(start + 1, end) : null;
    }
}
