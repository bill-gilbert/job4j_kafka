package ru.job4j.kafka.producer;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Properties;

/**
 * Простой продюсер
 *
 * @author Павел Глухов
 */
public class SimpleProducer {
    private static final Logger logger = LoggerFactory.getLogger(SimpleProducer.class);

    public static void main(String[] args) throws InterruptedException {
        var props = new Properties();
        props.put("bootstrap.servers", "localhost:9092");
        props.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        props.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");

        try (KafkaProducer<String, String> producer = new KafkaProducer<>(props)) {
            var topic = "demo-topic";
            var key = "key1"; // Что-бы сообщения попадало в одну партицию и у нас была очередность
            for (int i = 1; i <= 10000; i++) {
                var value = "Message number " + i;
                var record = new ProducerRecord<>(topic, key, value);

                // Асинхронная отправка сообщения с коллбеком
                producer.send(record, (RecordMetadata metadata, Exception e) -> {
                    if (e == null) {
                        logger.info("Message sent to topic {}, partition {}, offset {}",
                                metadata.topic(), metadata.partition(), metadata.offset());
                    } else {
                        logger.error("Error while sending message", e);
                    }
                });

                Thread.sleep(1000);
            }


            producer.flush();
        }
    }
}
