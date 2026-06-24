package com.bookbot.hitl.consumers;

import com.bookbot.hitl.entities.ReviewDraft;
import com.bookbot.hitl.entities.ReviewStatus;
import com.bookbot.hitl.models.DraftPayload;
import com.bookbot.hitl.repositories.ReviewDraftRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class DraftConsumer {

    private final ReviewDraftRepository draftRepository;
    private final ObjectMapper objectMapper;
    private final JdbcTemplate jdbcTemplate;

    public DraftConsumer(ReviewDraftRepository draftRepository, ObjectMapper objectMapper, JdbcTemplate jdbcTemplate) {
        this.draftRepository = draftRepository;
        this.objectMapper = objectMapper;
        this.jdbcTemplate = jdbcTemplate;
    }

    @Scheduled(fixedDelay = 1000) // Polls every 1 second
    @Transactional
    public void consumeDraftsFromDbQueue() {
        // Query next pending messages, locking them to prevent concurrent instances from double-processing
        String selectSql = "SELECT id, payload FROM db_queue_messages " +
                "WHERE topic = 'DRAFT-TRANSACTION' AND status = 'PENDING' " +
                "ORDER BY id ASC LIMIT 10 " +
                "FOR UPDATE SKIP LOCKED";

        List<Map<String, Object>> messages = jdbcTemplate.queryForList(selectSql);

        for (Map<String, Object> message : messages) {
            Long messageId = ((Number) message.get("id")).longValue();
            String payloadJson = (String) message.get("payload");

            try {
                DraftPayload payload = objectMapper.readValue(payloadJson, DraftPayload.class);

                // 1. Check for duplicate transactions within last 24h
                List<ReviewDraft> duplicateCheck = draftRepository.findDuplicateInLast24Hours(
                        payload.amount(), payload.person(), OffsetDateTime.now().minusHours(24)
                );
                boolean isDuplicate = !duplicateCheck.isEmpty();
                UUID duplicateOfId = isDuplicate ? duplicateCheck.getFirst().getId() : null;

                // 2. Map payload straight to PENDING_REVIEW
                ReviewDraft draft = new ReviewDraft();
                draft.setTransactionDate(payload.transactionDate());
                draft.setType(payload.type());
                draft.setCategory(payload.category());
                draft.setAmount(payload.amount());
                draft.setPerson(payload.person());
                draft.setSplits(payload.splits());
                draft.setStatus(ReviewStatus.PENDING_REVIEW);
                draft.setDuplicate(isDuplicate);
                draft.setDuplicateOfId(duplicateOfId);
                draft.setMediaUrl(payload.mediaUrl());
                draft.setMediaType(payload.mediaType());
                draft.setRawOcrOrTranscript(payload.rawOcrOrTranscript());

                draftRepository.save(draft);

                // 3. Mark message as PROCESSED
                jdbcTemplate.update(
                        "UPDATE db_queue_messages SET status = 'PROCESSED', attempts = attempts + 1, updated_at = NOW() WHERE id = ?",
                        messageId
                );

            } catch (Exception e) {
                System.err.println("Error processing incoming draft payload [ID=" + messageId + "]: " + e.getMessage());
                // Mark message as FAILED and record error message
                jdbcTemplate.update(
                        "UPDATE db_queue_messages SET status = 'FAILED', attempts = attempts + 1, error_message = ?, updated_at = NOW() WHERE id = ?",
                        e.getMessage(),
                        messageId
                );
            }
        }
    }
}
