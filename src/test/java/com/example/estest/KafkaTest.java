package com.example.estest;


import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.producer.KafkaProducerTest;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.kafka.KafkaAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.kafka.test.utils.KafkaTestUtils;

import java.util.Arrays;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@EmbeddedKafka
@SpringBootTest(
    classes = {
        KafkaAutoConfiguration.class
    }
)
public class KafkaTest
{
    // 잘 실행은 되나, intellij에서 빨간줄이 쳐져있는 이슈가 있따.
    @Autowired
    EmbeddedKafkaBroker embeddedKafkaBroker;

    Producer<String, String> producer;
    Consumer<String, String> consumer;

    private final String TEST_TOPIC = "test_topic";

    @BeforeEach
    public void init()
    {
        producer = configureProducer();
        consumer = configureConsumer();

        consumer.subscribe(Arrays.asList(TEST_TOPIC));
    }

    @Test
    public void test()
    {
        producer.send(new ProducerRecord<>(TEST_TOPIC, "주현태"));
        producer.send(new ProducerRecord<>(TEST_TOPIC, "주현태1"));

        ConsumerRecords<String, String> records = KafkaTestUtils.getRecords(consumer);

        assertThat(records.count()).isEqualTo(2);
    }

    private Producer<String, String> configureProducer()
    {
        Map<String, Object> configs = KafkaTestUtils.producerProps(embeddedKafkaBroker);
        return new DefaultKafkaProducerFactory<String, String>(configs).createProducer();
    }

    private Consumer<String, String> configureConsumer()
    {
        Map<String, Object> configs
            = KafkaTestUtils.consumerProps("test-group", "true", embeddedKafkaBroker);
        return new DefaultKafkaConsumerFactory<String, String>(configs).createConsumer();
    }

}
