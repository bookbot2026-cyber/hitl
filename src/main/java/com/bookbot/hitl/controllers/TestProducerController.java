package com.bookbot.hitl.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/test/kafka")
public class TestProducerController {

    private final KafkaTemplate<String, String> kafkaTemplate;

    public TestProducerController(KafkaTemplate<String, String> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    @PostMapping("/publish")
    public ResponseEntity<String> publishMockTransaction(@RequestBody String payload) {
        // Publishes your raw JSON straight to the topic
        kafkaTemplate.send("DRAFT-TRANSACTION", payload);
        return ResponseEntity.ok("Successfully sent message to Kafka topic: DRAFT-TRANSACTION");
    }
}
