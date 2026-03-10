package com.edacourse.solid;

import java.util.List;

public class CompositeNotification implements NotificationService {
    private final List<NotificationService> notificationServices;
    
    public CompositeNotification(List<NotificationService> notificationServices) {
        this.notificationServices = notificationServices;
    }
    
    @Override
    public void notify(String message) {
        notificationServices.forEach(service -> service.notify(message));
    }
}
