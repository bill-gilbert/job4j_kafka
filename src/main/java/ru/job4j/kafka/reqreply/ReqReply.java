package ru.job4j.kafka.reqreply;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;
import java.util.concurrent.FutureTask;

/**
 * Имплементация паттерна Request/Reply
 * Переконвертил с Kotlin
 *
 *
 */
public class ReqReply {
    private static final Logger logger = LoggerFactory.getLogger(ReqReply.class);

    private final long timeout;
    private final Object monitor = new Object();
    private String message = "";
    private boolean received = false;

    public ReqReply(long timeout) {
        this.timeout = timeout;
    }

    public String send(UUID uuid) {
        synchronized (monitor) {
            if (!received) {
                try {
                    monitor.wait(timeout);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    return "Interrupted while waiting";
                }
            }
            return received ? message : "Happened timeout " + timeout;
        }
    }

    public void receive(String text) {
        synchronized (monitor) {
            received = true;
            message = text;
            monitor.notifyAll();
        }
    }

    public static void main(String[] args) throws Exception {
        ReqReply reply = new ReqReply(1000);
        FutureTask<String> task = new FutureTask<>(() -> reply.send(UUID.randomUUID()));

        new Thread(task).start();

        new Thread(() -> {
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            reply.receive("Send message");
        }).start();

        logger.info(task.get());
    }
}
