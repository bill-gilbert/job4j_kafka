package ru.job4j.kafka.reqreply.v2;

import java.util.UUID;
import java.util.concurrent.FutureTask;

public class Main {
    public static void main(String[] args) throws Exception {
        UUID correlationId = UUID.randomUUID();
        ReqReply reply = new ReqReply(1000);

        FutureTask<String> task = new FutureTask<>(() -> reply.send(correlationId));
        new Thread(task).start();

        new Thread(() -> {
            try {
                Thread.sleep(500);
                reply.receive(correlationId, "Send message");
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }).start();

        System.out.println(task.get());
    }
}