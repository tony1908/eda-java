package com.edacourse.api.shared.infrastructure.sse;

import jakarta.ws.rs.sse.Sse;
import jakarta.ws.rs.sse.SseEventSink;
import jakarta.ws.rs.sse.SseBroadcaster;
import jakarta.ws.rs.sse.OutboundSseEvent;

import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.atomic.AtomicLong;
import jakarta.inject.Singleton;

@Singleton
public class EventSseBroadcaster {
    SseBroadcaster broadcaster;
    private Sse sse;
    private final AtomicLong eventCounter = new AtomicLong(0);
    private final ConcurrentLinkedDeque<SseEventRecord> eventHistory = new ConcurrentLinkedDeque<>();
    private static final int MAX_HISTORY_SIZE = 100;

    public void initialize(Sse sse) {
        this.sse = sse;
        this.broadcaster = sse.newBroadcaster();
        this.broadcaster.onClose(sink -> {
            System.out.println("Broadcaster closed");
        });
        System.out.println("Broadcaster initialized");  
    }

    public boolean isReady() {
        return broadcaster != null && sse != null;
    }

    public void register(SseEventSink sink, String lastEventId) {
        if (!isReady()) return;

        if (lastEventId != null && !lastEventId.isBlank()) {
            replayEvents(sink, lastEventId);
        }

        broadcaster.register(sink);
        System.out.println("Broadcaster registered");
    }

    public void broadcast(String eventType, String topic, String data) {
        if (!isReady()) return;

        long id = eventCounter.incrementAndGet();
        String eventId = String.valueOf(id);

        OutboundSseEvent event = sse.newEventBuilder()
            .id(eventId)
            .name(eventType)
            .data(String.class, "{\"topic\":\"" + topic + "\",\"data\":" + data + "}")
            .comment("topic: " + topic)
            .build();

        eventHistory.add(new SseEventRecord(eventId, eventType, topic, data));
        while (eventHistory.size() > MAX_HISTORY_SIZE) {
            eventHistory.pollFirst();
        }

        broadcaster.broadcast(event);
        System.out.println("Broadcaster broadcasted");
    }

    private void replayEvents(SseEventSink sink, String lastEventId) {
        long lastId;
        try {
            lastId = Long.parseLong(lastEventId);
        } catch (NumberFormatException e) {
            System.err.println("Invalid lastEventId: " + lastEventId);
            return;
        }

        int replayed = 0;
        for (SseEventRecord record : eventHistory) {
            long recordId = Long.parseLong(record.id());
            if (recordId > lastId) {
                OutboundSseEvent event = sse.newEventBuilder()
                    .id(record.id())
                    .name(record.eventType())
                    .data(String.class, "{\"topic\":\"" + record.topic() + "\",\"data\":" + record.data() + "}")
                    .comment("topic: " + record.topic())
                    .build();
                sink.send(event);
                replayed++;
            }
        }
        if (replayed > 0) {
            System.out.println("Replayed " + replayed + " events");
        }
    }

    public long getEventCount() {
        return eventCounter.get();
    }

    public int getHistorySize() {
        return eventHistory.size();
    }

    public record SseEventRecord(String id, String eventType, String topic, String data) {}
}
