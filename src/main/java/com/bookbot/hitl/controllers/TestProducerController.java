package com.bookbot.hitl.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/test/queue")
public class TestProducerController {

    private final JdbcTemplate jdbcTemplate;

    public TestProducerController(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @PostMapping("/publish")
    public ResponseEntity<String> publishMockTransaction(@RequestBody String payload) {
        // Publishes your raw JSON straight to the DB queue table
        jdbcTemplate.update(
                "INSERT INTO db_queue_messages (topic, payload, status) VALUES (?, ?, 'PENDING')",
                "DRAFT-TRANSACTION",
                payload
        );
        return ResponseEntity.ok("Successfully sent message to DB queue: DRAFT-TRANSACTION");
    }
}
