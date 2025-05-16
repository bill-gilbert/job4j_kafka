package ru.job4j.kafka.reqreply.v2;

import org.junit.jupiter.api.Test;

import java.util.UUID;
import java.util.concurrent.FutureTask;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ReqReplyTest {

    @Test
    void testSendAndReceiveSameCorrelationId() throws Exception {
        ReqReply reply = new ReqReply(1000);
        UUID correlationId = UUID.randomUUID();

        FutureTask<String> task = new FutureTask<>(() -> reply.send(correlationId));
        new Thread(task).start();

        Thread.sleep(300);
        reply.receive(correlationId, "Hello World");

        String result = task.get();
        assertEquals("Hello World", result);
    }

    @Test
    void testTimeout() throws Exception {
        ReqReply reply = new ReqReply(300);
        UUID correlationId = UUID.randomUUID();

        FutureTask<String> task = new FutureTask<>(() -> reply.send(correlationId));
        new Thread(task).start();

        String result = task.get();
        assertTrue(result.startsWith("Happened timeout"));
    }

    @Test
    void testSendReceiveDifferentCorrelationIds() throws Exception {
        ReqReply reply = new ReqReply(300);
        UUID sendId = UUID.randomUUID();
        UUID receiveId = UUID.randomUUID();

        FutureTask<String> task = new FutureTask<>(() -> reply.send(sendId));
        new Thread(task).start();

        Thread.sleep(100);
        reply.receive(receiveId, "Wrong correlation");

        String result = task.get();
        assertTrue(result.startsWith("Happened timeout"));
    }
}