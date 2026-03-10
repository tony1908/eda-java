package com.edacourse.solid;

import java.util.logging.Logger;

public class LogNotification implements NotificationService {
    private static final Logger logger = Logger.getLogger(LogNotification.class.getName());
    
    @Override
    public void notify(String message) {
        logger.info("[LogNotification]: " + message);
    }
}
