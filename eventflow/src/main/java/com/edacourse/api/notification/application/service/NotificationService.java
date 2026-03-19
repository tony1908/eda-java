package com.edacourse.api.notification.application.service;

public class NotificationService {

    public void notify(String type, String orderId, String details) {
        System.out.println("[NOTIFY] " + type + " | Orden: " + orderId + " | " + details);
    }
}
