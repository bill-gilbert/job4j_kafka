package ru.job4j.kafka.reqreply.v2;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class ReqReply {
    private static final Logger logger = LoggerFactory.getLogger(ReqReply.class);

    private final long timeout;
    private final ConcurrentMap<String, Entry> responses = new ConcurrentHashMap<>();

    public ReqReply(long timeout) {
        this.timeout = timeout;
    }

    public String send(String correlationId) {
        Entry entry = responses.computeIfAbsent(correlationId, id -> new Entry());

        synchronized (entry.monitor) {
            if (!entry.received) {
                try {
                    entry.monitor.wait(timeout);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    return "Interrupted while waiting";
                }
            }

            if (entry.received) {
                return entry.message;
            } else {
                return "Happened timeout " + timeout;
            }
        }
    }

    public void receive(String correlationId, String message) {
        Entry entry = responses.computeIfAbsent(correlationId, id -> new Entry());

        synchronized (entry.monitor) {
            entry.received = true;
            entry.message = message;
            entry.monitor.notifyAll();
        }
    }

    private static class Entry {
        final Object monitor = new Object();
        volatile boolean received = false;
        volatile String message = "";
    }
}