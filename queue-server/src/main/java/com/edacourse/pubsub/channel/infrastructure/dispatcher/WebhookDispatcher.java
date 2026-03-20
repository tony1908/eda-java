package com.edacourse.pubsub.channel.infrastructure.dispatcher;

import com.edacourse.pubsub.channel.infrastructure.security.HmacSigner;
import com.edacourse.pubsub.channel.model.DeliveryRecord;
import com.edacourse.pubsub.channel.model.Subscription;
import com.edacourse.pubsub.channel.repository.SubscriptionRepository;
import com.edacourse.pubsub.channel.repository.DeliveryRespository;

import org.apache.kafka.clients.consumer.KafkaConsumer;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Pattern;

import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import com.edacourse.pubsub.channel.repository.ChannelRepository;
import com.edacourse.pubsub.channel.model.Channel;

public class WebhookDispatcher implements AutoCloseable {
    private final SubscriptionRepository subscriptionRepo;
    private final DeliveryRespository deliveryRepo;
    private final ChannelRepository channelRepo;
    private final HmacSigner hmacService;
    private final KafkaConsumer<String, String> consumer;
    private final HttpClient httpClient;
    private final AtomicBoolean running = new AtomicBoolean(false);
    private Thread consumerThread;
    private final ExecutorService deliveryExecutor;

    private static final int MAX_RETRIES = 3;
    private static final long INITIAL_DELAY_MS = 1000;
    private static final double BACKOFF_MULTIPLIER = 2.0;

    public WebhookDispatcher(SubscriptionRepository subscriptionRepo, DeliveryRespository deliveryRepo, HmacSigner hmacService, ChannelRepository channelRepo) {
        this.subscriptionRepo = subscriptionRepo;
        this.deliveryRepo = deliveryRepo;
        this.hmacService = hmacService;
        this.channelRepo = channelRepo;
        this.httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(5))
            .build();
        this.deliveryExecutor = Executors.newFixedThreadPool(10);

        Properties props = new Properties();
        props.put("bootstrap.servers", System.getenv("KAFKA_BOOTSTRAP_SERVERS"));
        props.put(ConsumerConfig.GROUP_ID_CONFIG, "pubsub-dispatcher");
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "true");
        this.consumer = new KafkaConsumer<>(props);
    }

    public void start() {
        running.set(true);
        consumer.subscribe(Pattern.compile("pubsub\\..*"));
        System.out.println("WebhookDispatcher started");

        consumerThread = new Thread(() -> {
            while (running.get()) {
                try {
                    ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(500));
                    records.forEach(record -> {
                        String channelName = record.topic().replace("pubsub.", "");
                        String payload = record.value();
                        String messageId = String.valueOf(record.offset());

                        System.out.println("Received message for channel " + channelName + " with ID " + messageId);
                        dispatchToSubscribers(channelName, payload, messageId);
                    });
                } catch (Exception e) {
                    if (running.get()) {
                        System.err.println("[DISPATCHER] Error en consumer loop: " + e.getMessage());
                    }
                }
            }
        }, "webhook-dispatcher-thread");
        consumerThread.setDaemon(true);
        consumerThread.start();
    }

    private void dispatchToSubscribers(String channelName, String payload, String messageId){
        Channel channel = channelRepo.findByName(channelName).orElse(null);
        if (channel == null) {
            System.out.println("[DISPATCHER] Canal '" + channelName + "' no encontrado");
            return;
        }
        List<Subscription> subscribers = subscriptionRepo.findActiveByChannelId(channel.getId());
        if (subscribers.isEmpty()) {
            System.out.println("[DISPATCHER] Sin suscriptores activos para canal '" + channelName + "'");
            return;
        }

        System.out.println("[DISPATCHER] Entregando a " + subscribers.size() + " suscriptor(es) en paralelo");
        CompletableFuture<?>[] futures = subscribers.stream()
            .map(sub -> CompletableFuture.runAsync(
                () -> deliverWithRetry(channelName, payload, messageId, sub),
                deliveryExecutor))
            .toArray(CompletableFuture[]::new);
        CompletableFuture.allOf(futures).join();
    }

    private void deliverWithRetry(String channelName, String payload, String messageId, Subscription sub) {
        long delay = INITIAL_DELAY_MS;
        for (int attempt = 1; attempt <= MAX_RETRIES; attempt++) {
            try {
                String signature = hmacService.sign(payload, sub.getSecret());
                String timestamp = Instant.now().toString();

                HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(sub.getWebhookUrl()))
                    .header("Content-Type", "application/json")
                    .header("X-PubSub-Signature", signature)
                    .header("X-PubSub-Channel", channelName)
                    .header("X-PubSub-Timestamp", timestamp)
                    .header("X-PubSub-Subscription-Id", sub.getId())
                    .timeout(Duration.ofSeconds(15))
                    .POST(HttpRequest.BodyPublishers.ofString(payload))
                    .build();

                HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
                int status = response.statusCode();

                if (status >= 200 && status < 300) {
                    System.out.println("[DISPATCHER] Entregado a " + sub.getWebhookUrl() + " (HTTP " + status + ", intento " + attempt + ")");
                    logDelivery(messageId, sub, channelName, "DELIVERED", status, attempt, null);
                    return;
                } else if (status >= 400 && status < 500) {
                    System.err.println("[DISPATCHER] Error cliente " + status + " de " + sub.getWebhookUrl() + " — no reintentando");
                    logDelivery(messageId, sub, channelName, "FAILED", status, attempt, "HTTP " + status + ": " + response.body());
                    return;
                } else {
                    System.err.println("[DISPATCHER] Error servidor " + status + " de " + sub.getWebhookUrl() + " — intento " + attempt + "/" + MAX_RETRIES);
                    logDelivery(messageId, sub, channelName, "RETRYING", status, attempt, "HTTP " + status);
                }

            } catch (Exception e) {
                System.err.println("[DISPATCHER] Error conectando a " + sub.getWebhookUrl() + ": " + e.getMessage() + " — intento " + attempt + "/" + MAX_RETRIES);
                logDelivery(messageId, sub, channelName, "RETRYING", 0, attempt, e.getMessage());
            }

            if (attempt < MAX_RETRIES) {
                try { Thread.sleep(delay); } catch (InterruptedException ie) { Thread.currentThread().interrupt(); return; }
                delay = (long)(delay * BACKOFF_MULTIPLIER);
            }
        }

        System.err.println("[DISPATCHER] FALLIDO — agotados " + MAX_RETRIES + " reintentos para " + sub.getWebhookUrl());
        logDelivery(messageId, sub, channelName, "FAILED", 0, MAX_RETRIES, "Agotados reintentos");
    }

    private void logDelivery(String messageId, Subscription sub, String channelName,
                             String status, int httpStatus, int attempt, String error) {
        try {
            DeliveryRecord delivery = new DeliveryRecord(
                messageId, sub.getId(), channelName, sub.getWebhookUrl(),
                status, httpStatus, attempt, error);

            deliveryRepo.save(delivery);
        } catch (Exception e) {
            System.err.println("[DISPATCHER] Error logueando entrega: " + e.getMessage());
        }
    }

    @Override
    public void close() {
        running.set(false);
        consumer.wakeup();
        deliveryExecutor.shutdown();
        try { deliveryExecutor.awaitTermination(10, TimeUnit.SECONDS); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
        System.out.println("[DISPATCHER] Detenido");
    }
}
