package ru.job4j.kafka.consumer;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.errors.WakeupException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.Collections;
import java.util.Properties;

/**
 * Простой консьюмер с обработкой shutdown
 *
 * @author Павел Глухов
 */
public class SimpleConsumerWithShutdown {
    private static final Logger logger = LoggerFactory.getLogger(SimpleConsumerWithShutdown.class);

    public static void main(String[] args) {
        Properties props = new Properties();
        props.put("bootstrap.servers", "localhost:9092");
        props.put("group.id", "demo-consumer-group");
        props.put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        props.put("value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        props.put("auto.offset.reset", "earliest"); // Чтение с начала

        final Thread mainThread = Thread.currentThread();


        try (KafkaConsumer<String, String> consumer = new KafkaConsumer<>(props)) {

            // добавить shotdown hook
            Runtime.getRuntime().addShutdownHook(new Thread() {
                public void run() {
                    logger.info("Shutting down...");
                    consumer.wakeup();

                    try {
                        mainThread.join();
                    } catch (InterruptedException e) {
                        logger.error("Main thread interrupted", e);
                    }
                }
            });


            consumer.subscribe(Collections.singletonList("demo-topic"));

            while (true) {
                ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(1000));
                for (ConsumerRecord<String, String> record : records) {
                    logger.info("Received message: key = {}, value = {}, partition = {}, offset = {}",
                            record.key(), record.value(), record.partition(), record.offset());
                }
            }
        } catch (WakeupException wakeupException) {
            logger.info("wake up exception");
        } catch (Exception e) {
            logger.error("Unexpected exception", e);
        }
    }
}
