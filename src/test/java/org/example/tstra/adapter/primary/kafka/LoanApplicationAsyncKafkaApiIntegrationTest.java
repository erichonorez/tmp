package org.example.tstra.adapter.primary.kafka;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.example.tstra.application.usecase.CreateLoanApplication;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.kafka.test.utils.KafkaTestUtils;

import java.util.*;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.DynamicTest.dynamicTest;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@SpringBootTest
@EmbeddedKafka(
    partitions = 1,
    bootstrapServersProperty = "spring.kafka.bootstrap-servers",
    topics = {
        "requests",
        "responses"
    },
    brokerProperties = {
        "log.dir=./kafka-logs"
    }
)
@TestInstance(TestInstance.Lifecycle.PER_METHOD)
class LoanApplicationAsyncKafkaApiIntegrationTest {

    @MockBean
    private CreateLoanApplication createLoanApplication;

    @Autowired
    private EmbeddedKafkaBroker embeddedKafkaBroker;

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    private Consumer<String, String> consumer;

    @BeforeEach
    public void before() {
        consumer = this.createConsumer();
    }


    @Test
    public void itShouldReturnALoanApplicationId() throws CreateLoanApplication.CreateLoanApplicationException {
        CreateLoanApplication.CreateLoanApplicationRequest request = new CreateLoanApplication.CreateLoanApplicationRequest(
            "123",
            "123",
            "fr",
            123
        );

        String loanApplicationId = UUID.randomUUID().toString();
        given(this.createLoanApplication.execute(request)).willReturn(new CreateLoanApplication.CreateLoanApplicationResponse(
            loanApplicationId
        ));

        String correlationId = UUID.randomUUID().toString();
        this.kafkaTemplate.send(new ProducerRecord<>(
            "requests",
            UUID.randomUUID().toString(),
            "{\"correlationId\":\""+correlationId+"\",\"merchantId\":\"123\",\"productId\":\"123\",\"language\":\"fr\",\"purchaseAmount\":123}"
        ));

        Optional<LoanApplicationAsyncKafkaApi.CreateLoanApplicationKafkaResponse> optionalEvent
            = this.findResponseWithCorrelationId(correlationId, this.getConsumerRecords(consumer));

        assertEquals(loanApplicationId, optionalEvent.map(r -> r.getMessage()).orElse(UUID.randomUUID().toString()));
    }

    @TestFactory
    public Stream<DynamicTest> whenRequestSchemaIsNotValid_itShouldReturnAnError() throws CreateLoanApplication.CreateLoanApplicationException {
        return Arrays.asList(
            "",
            "{",
            "{\"\"}",
            "{\"hello\": \"world\"}").stream()
            .map(badJson -> {
                return dynamicTest(String.format("Bad json [%s]", badJson), () -> {
                    this.kafkaTemplate.send(new ProducerRecord<>(
                        "requests",
                        UUID.randomUUID().toString(),
                        ""
                    ));

                    ConsumerRecords<String, String> consumerRecords = this.getConsumerRecords(consumer);
                    assertEquals(1, consumerRecords.count());

                    verify(this.createLoanApplication, never()).execute(any());
                });
            });
    }

    @Test
    public void whenCreateLoanApplicationUseCaseFails_itShouldMapTheError() throws CreateLoanApplication.CreateLoanApplicationException {
        String merchantId = "123";
        String productId = "123";
        String language = "fr";
        int purchaseAmount = 4000;

        given(this.createLoanApplication.execute(new CreateLoanApplication.CreateLoanApplicationRequest(
            merchantId,
            productId,
            language,
            4000
        ))).willThrow(new CreateLoanApplication.MerchantNotFoundException());

        String correlationId = UUID.randomUUID().toString();
        this.kafkaTemplate.send(new ProducerRecord<>(
            "requests",
            UUID.randomUUID().toString(),
            "{\"correlationId\":\""+correlationId+"\",\"merchantId\":\"123\",\"productId\":\"123\",\"language\":\"fr\",\"purchaseAmount\":4000}"
        ));

        Optional<LoanApplicationAsyncKafkaApi.CreateLoanApplicationKafkaResponse> optionalEvent
            = this.findResponseWithCorrelationId(correlationId, this.getConsumerRecords(consumer));

        assertEquals("MerchantNotFoundException", optionalEvent.map(e -> e.getType()).orElse(""));
    }

    private Optional<LoanApplicationAsyncKafkaApi.CreateLoanApplicationKafkaResponse> findResponseWithCorrelationId(String correlationId, ConsumerRecords<String, String> replies) {
        Optional<LoanApplicationAsyncKafkaApi.CreateLoanApplicationKafkaResponse> optionalEvent = Optional.empty();
        Iterator<ConsumerRecord<String, String>> it = replies.records("responses").iterator();
        while(it.hasNext()) {
            ConsumerRecord<String, String> r = it.next();
            String value = r.value();
            try {
                LoanApplicationAsyncKafkaApi.CreateLoanApplicationKafkaResponse response =
                    objectMapper.readValue(value, LoanApplicationAsyncKafkaApi.CreateLoanApplicationKafkaResponse.class);
                if (correlationId.equals(response.getCorrelationId())) {
                    optionalEvent = Optional.of(response);
                }
            } catch (JsonProcessingException e) { }
        }
        return optionalEvent;
    }

    private ConsumerRecords<String, String> getConsumerRecords(Consumer<String, String> consumer) {
        return KafkaTestUtils.getRecords(consumer);
    }

    private Consumer<String, String> createConsumer() {
        String groupId = UUID.randomUUID().toString();
        Consumer<String, String> consumer = createConsumer(groupId);
        this.embeddedKafkaBroker.consumeFromAnEmbeddedTopic(consumer, "responses");
        return consumer;
    }

    private Consumer<String, String> createConsumer(String groupId) {
        Map<String, Object> consumerProps = KafkaTestUtils.consumerProps(groupId, "true", this.embeddedKafkaBroker);
        consumerProps.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "latest");
        consumerProps.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringDeserializer");
        ConsumerFactory<String, String> cf = new DefaultKafkaConsumerFactory<>(consumerProps);
        return cf.createConsumer();
    }

}