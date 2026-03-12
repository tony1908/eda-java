package com.edacourse.api.service;

public class NotificationService {
    public void notifyEvent(String eventName, String details) {
        System.out.println("Notificación: evento '" + eventName + "' completado — " + details);
    }
}
