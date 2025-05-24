package ru.job4j.kafka.reqreply.v2;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

/**
 * попытка решить 4-ое задание
 *
 * @author Павел Глухов
 */
public class ReqReply {
    private static final Logger logger = LoggerFactory.getLogger(ReqReply.class);
    private final long timeout;
    private final ConcurrentMap<UUID, ReplyLock> responses = new ConcurrentHashMap<>();

    public ReqReply(long timeout) {
        this.timeout = timeout;
    }

    public String send(UUID correlationId) {
        ReplyLock replyLock = responses.computeIfAbsent(correlationId, id -> new ReplyLock());

        synchronized (replyLock.monitor) {
            if (!replyLock.received.get()) {
                try {
                    replyLock.monitor.wait(timeout);
                } catch (InterruptedException e) {
                    responses.remove(correlationId);
                    Thread.currentThread().interrupt();
                    return "Interrupted while waiting";
                }
            }

            responses.remove(correlationId);
            if (replyLock.received.get()) {
                return replyLock.message.get();
            } else {
                return "Happened timeout " + timeout;
            }
        }
    }

    public void receive(UUID correlationId, String message) {
        ReplyLock replyLock = responses.computeIfAbsent(correlationId, id -> new ReplyLock());

        synchronized (replyLock.monitor) {
            replyLock.received.set(true);
            replyLock.message.set(message);
            replyLock.monitor.notifyAll();
        }
    }

    public record ReplyLock(Object monitor, AtomicBoolean received, AtomicReference<String> message) {
        public ReplyLock() {
            this(new Object(), new AtomicBoolean(false), new AtomicReference<>(""));
        }
    }
}