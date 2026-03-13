# Event Chain Services Implementation Plan

> **For Claude:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task.

**Goal:** Create a chain of event-driven services: InventoryService emits `InventoryReservedEvent`, new PaymentService/Subscriber listens to `orders.created` and emits `PaymentCompletedEvent`, new NotificationService/Subscriber listens to both `inventory.reserved` and `payment.completed` and logs which event finished.

**Architecture:** Extend the existing pub/sub event chain. Each service subscribes to upstream events, does its work (logging for now), and publishes a downstream event. The notification service is the terminal consumer that logs completions from both branches.

**Tech Stack:** Java 21, Jersey/Grizzly, Kafka (via existing EventBus abstraction), Jackson

---

### Task 1: Create InventoryReservedEvent

**Files:**
- Create: `src/main/java/com/edacourse/api/domain/event/InventoryReservedEvent.java`

**Step 1: Create the event record**

```java
package com.edacourse.api.domain.event;

public record InventoryReservedEvent(String product, int quantity) {}
```

**Step 2: Commit**

```bash
git add src/main/java/com/edacourse/api/domain/event/InventoryReservedEvent.java
git commit -m "feat: add InventoryReservedEvent"
```

---

### Task 2: Modify InventoryService to accept EventBus and publish InventoryReservedEvent

**Files:**
- Modify: `src/main/java/com/edacourse/api/service/InventoryService.java`

**Step 1: Update InventoryService**

The current `InventoryService` is a plain class with no dependencies. It needs to receive the `EventBus` and publish `InventoryReservedEvent` after updating inventory.

```java
package com.edacourse.api.service;

import com.edacourse.api.infrastructure.messaging.EventBus;
import com.edacourse.api.domain.event.InventoryReservedEvent;

public class InventoryService {
    private final EventBus eventBus;

    public InventoryService(EventBus eventBus) {
        this.eventBus = eventBus;
    }

    public void updateInventory(String product, int quantity) {
        System.out.println("Actualizando inventario para: " + product + ", cantidad: " + quantity);
        eventBus.publish("inventory.reserved", new InventoryReservedEvent(product, quantity));
    }
}
```

**Step 2: Update Application.main() to pass eventBus to InventoryService**

In `src/main/java/com/edacourse/api/Application.java`, change:

```java
// FROM:
InventoryService inventoryService = new InventoryService();

// TO:
InventoryService inventoryService = new InventoryService(eventBus);
```

**Step 3: Commit**

```bash
git add src/main/java/com/edacourse/api/service/InventoryService.java
git add src/main/java/com/edacourse/api/Application.java
git commit -m "feat: InventoryService emits InventoryReservedEvent"
```

---

### Task 3: Create PaymentCompletedEvent

**Files:**
- Create: `src/main/java/com/edacourse/api/domain/event/PaymentCompletedEvent.java`

**Step 1: Create the event record**

```java
package com.edacourse.api.domain.event;

public record PaymentCompletedEvent(String product, double price) {}
```

**Step 2: Commit**

```bash
git add src/main/java/com/edacourse/api/domain/event/PaymentCompletedEvent.java
git commit -m "feat: add PaymentCompletedEvent"
```

---

### Task 4: Create PaymentService

**Files:**
- Create: `src/main/java/com/edacourse/api/service/PaymentService.java`

**Step 1: Create PaymentService**

Follows the same pattern as `InventoryService` — receives EventBus, logs, and publishes downstream event.

```java
package com.edacourse.api.service;

import com.edacourse.api.infrastructure.messaging.EventBus;
import com.edacourse.api.domain.event.PaymentCompletedEvent;

public class PaymentService {
    private final EventBus eventBus;

    public PaymentService(EventBus eventBus) {
        this.eventBus = eventBus;
    }

    public void processPayment(String product, double price) {
        System.out.println("Procesando pago para: " + product + ", precio: " + price);
        eventBus.publish("payment.completed", new PaymentCompletedEvent(product, price));
    }
}
```

**Step 2: Commit**

```bash
git add src/main/java/com/edacourse/api/service/PaymentService.java
git commit -m "feat: add PaymentService"
```

---

### Task 5: Create PaymentSubscriber

**Files:**
- Create: `src/main/java/com/edacourse/api/subscriber/PaymentSubscriber.java`

**Step 1: Create PaymentSubscriber**

Subscribes to `orders.created`, calls `PaymentService.processPayment()`. Follows the same pattern as `InventorySubscriber`.

```java
package com.edacourse.api.subscriber;

import com.edacourse.api.infrastructure.messaging.EventBus;
import com.edacourse.api.domain.event.OrderCreatedEvent;
import com.edacourse.api.service.PaymentService;

public class PaymentSubscriber {
    private final PaymentService paymentService;

    public PaymentSubscriber(EventBus eventBus, PaymentService paymentService) {
        this.paymentService = paymentService;
        eventBus.subscribe("orders.created", OrderCreatedEvent.class, this::onOrderCreated);
    }

    private void onOrderCreated(OrderCreatedEvent event) {
        paymentService.processPayment(event.product(), event.price());
    }
}
```

**Step 2: Commit**

```bash
git add src/main/java/com/edacourse/api/subscriber/PaymentSubscriber.java
git commit -m "feat: add PaymentSubscriber"
```

---

### Task 6: Create NotificationService

**Files:**
- Create: `src/main/java/com/edacourse/api/service/NotificationService.java`

**Step 1: Create NotificationService**

Terminal service — no downstream events, just logs which event was received.

```java
package com.edacourse.api.service;

public class NotificationService {
    public void notifyEvent(String eventName, String details) {
        System.out.println("Notificación: evento '" + eventName + "' completado — " + details);
    }
}
```

**Step 2: Commit**

```bash
git add src/main/java/com/edacourse/api/service/NotificationService.java
git commit -m "feat: add NotificationService"
```

---

### Task 7: Create NotificationSubscriber

**Files:**
- Create: `src/main/java/com/edacourse/api/subscriber/NotificationSubscriber.java`

**Step 1: Create NotificationSubscriber**

Subscribes to both `inventory.reserved` and `payment.completed`.

```java
package com.edacourse.api.subscriber;

import com.edacourse.api.infrastructure.messaging.EventBus;
import com.edacourse.api.domain.event.InventoryReservedEvent;
import com.edacourse.api.domain.event.PaymentCompletedEvent;
import com.edacourse.api.service.NotificationService;

public class NotificationSubscriber {
    private final NotificationService notificationService;

    public NotificationSubscriber(EventBus eventBus, NotificationService notificationService) {
        this.notificationService = notificationService;
        eventBus.subscribe("inventory.reserved", InventoryReservedEvent.class, this::onInventoryReserved);
        eventBus.subscribe("payment.completed", PaymentCompletedEvent.class, this::onPaymentCompleted);
    }

    private void onInventoryReserved(InventoryReservedEvent event) {
        notificationService.notifyEvent("inventory.reserved",
            "producto: " + event.product() + ", cantidad: " + event.quantity());
    }

    private void onPaymentCompleted(PaymentCompletedEvent event) {
        notificationService.notifyEvent("payment.completed",
            "producto: " + event.product() + ", precio: " + event.price());
    }
}
```

**Step 2: Commit**

```bash
git add src/main/java/com/edacourse/api/subscriber/NotificationSubscriber.java
git commit -m "feat: add NotificationSubscriber"
```

---

### Task 8: Wire everything in Application.main()

**Files:**
- Modify: `src/main/java/com/edacourse/api/Application.java`

**Step 1: Add imports and instantiate new services/subscribers**

Add after existing subscriber wiring:

```java
import com.edacourse.api.service.PaymentService;
import com.edacourse.api.service.NotificationService;
import com.edacourse.api.subscriber.PaymentSubscriber;
import com.edacourse.api.subscriber.NotificationSubscriber;
```

In `main()`, after `new SseBridgeSubscriber(...)`:

```java
PaymentService paymentService = new PaymentService(eventBus);
NotificationService notificationService = new NotificationService();

new PaymentSubscriber(eventBus, paymentService);
new NotificationSubscriber(eventBus, notificationService);
```

**Step 2: Verify final Application.main() has this order:**

1. Create serializer, eventBus, sseResource
2. Create inventoryService (with eventBus) — already done in Task 2
3. Create paymentService (with eventBus)
4. Create notificationService (no deps)
5. Wire InventorySubscriber, SseBridgeSubscriber, PaymentSubscriber, NotificationSubscriber
6. Start Grizzly server

**Step 3: Commit**

```bash
git add src/main/java/com/edacourse/api/Application.java
git commit -m "feat: wire PaymentService and NotificationService in Application"
```

---

## Expected Event Flow After Implementation

```
POST /api/orders
  → OrderService publishes "orders.created"
      ├─ InventorySubscriber → InventoryService.updateInventory()
      │     → publishes "inventory.reserved"
      │         └─ NotificationSubscriber → logs "inventory.reserved completado"
      ├─ PaymentSubscriber → PaymentService.processPayment()
      │     → publishes "payment.completed"
      │         └─ NotificationSubscriber → logs "payment.completed completado"
      └─ SseBridgeSubscriber → SSE broadcast (unchanged)
```

## New Files Summary

| File | Type |
|------|------|
| `domain/event/InventoryReservedEvent.java` | Event record |
| `domain/event/PaymentCompletedEvent.java` | Event record |
| `service/PaymentService.java` | Service |
| `service/NotificationService.java` | Service |
| `subscriber/PaymentSubscriber.java` | Subscriber |
| `subscriber/NotificationSubscriber.java` | Subscriber |
