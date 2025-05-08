package ru.job4j.kafka.producer;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Properties;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Простой продюсер
 *
 * @author Павел Глухов
 */
public class TopicMessageProducer {
    private static final Logger logger = LoggerFactory.getLogger(TopicMessageProducer.class);

    public static void main(String[] args) throws InterruptedException {
        var props = new Properties();
        props.put("bootstrap.servers", "localhost:9092");
        props.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        props.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");

        try (KafkaProducer<String, String> producer = new KafkaProducer<>(props)) {
            var topic = "message";
            String key = null; // Что-бы сообщения попадало в одну партицию и у нас была очередность
            for (int i = 1; i <= 1000; i++) {
                var value = "Message number " + i;
                key = i % 5 == 0 ? null : String.valueOf(ThreadLocalRandom.current().nextInt(1, 4));
                var record = new ProducerRecord<>(topic, key, value);

                // Асинхронная отправка сообщения с коллбеком
                producer.send(record, (RecordMetadata metadata, Exception e) -> {
                    if (e != null) {
                        logger.error("Error while sending message", e);
                        return;
                    }
                    logger.info("Key = {}, value = {},  partition {}, offset {}",
                            record.key(), record.value(), metadata.partition(), metadata.offset());
                });

                Thread.sleep(1000);
            }

            producer.flush();
        }
    }
}
