import org.junit.jupiter.api.Test;
import ru.job4j.kafka.reqreply.v2.ReqReply;

import java.util.concurrent.FutureTask;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ReqReplyTest {

    @Test
    public void testSendAndReceiveSameCorrelationId() throws Exception {
        ReqReply reply = new ReqReply(1000);
        String correlationId = "1";

        FutureTask<String> task = new FutureTask<>(() -> reply.send(correlationId));
        new Thread(task).start();

        Thread.sleep(300);
        reply.receive(correlationId, "Hello World");

        String result = task.get();
        assertEquals("Hello World", result);
    }

    @Test
    public void testTimeout() throws Exception {
        ReqReply reply = new ReqReply(300);
        String correlationId = "1";

        FutureTask<String> task = new FutureTask<>(() -> reply.send(correlationId));
        new Thread(task).start();

        String result = task.get();
        assertTrue(result.startsWith("Happened timeout"));
    }

    @Test
    public void testSendReceiveDifferentCorrelationIds() throws Exception {
        ReqReply reply = new ReqReply(300);
        String sendId = "1";
        String receiveId = "2";

        FutureTask<String> task = new FutureTask<>(() -> reply.send(sendId));
        new Thread(task).start();

        Thread.sleep(100);
        reply.receive(receiveId, "Wrong correlation");

        String result = task.get();
        assertTrue(result.startsWith("Happened timeout"));
    }
}