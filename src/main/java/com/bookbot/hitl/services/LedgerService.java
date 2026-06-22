package com.bookbot.hitl.services;
import com.bookbot.hitl.entities.ReviewDraft;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
@Service
public class LedgerService {
    private final JdbcTemplate jdbcTemplate;
    private final ObjectMapper objectMapper;
    public LedgerService(JdbcTemplate jdbcTemplate, ObjectMapper objectMapper) {
        this.jdbcTemplate = jdbcTemplate;
        this.objectMapper = objectMapper;
    }
    @Transactional
    public void commitToLedger(ReviewDraft draft) {
        try {
            String splitsJson = objectMapper.writeValueAsString(draft.getSplits());

            // Insert into the final production ledger table (transactions)
            String sql = "INSERT INTO transactions (id, transaction_date, type, category, amount, person, splits, media_url, media_type, created_at, updated_at) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?::jsonb, ?, ?, ?, ?)" +
                    "ON CONFLICT (id) DO UPDATE SET " +
                    "transaction_date = EXCLUDED.transaction_date, " +
                    "type = EXCLUDED.type, " +
                    "category = EXCLUDED.category, " +
                    "amount = EXCLUDED.amount, " +
                    "person = EXCLUDED.person, " +
                    "splits = EXCLUDED.splits, " +
                    "media_url = EXCLUDED.media_url, " +
                    "media_type = EXCLUDED.media_type, " +
                    "updated_at = EXCLUDED.updated_at";

            jdbcTemplate.update(sql,
                    draft.getId(),
                    draft.getTransactionDate(),
                    draft.getType() != null ? draft.getType().name() : null,
                    draft.getCategory(),
                    draft.getAmount(),
                    draft.getPerson(),
                    splitsJson,
                    draft.getMediaUrl(),
                    draft.getMediaType() != null ? draft.getMediaType().name() : null,
                    draft.getCreatedAt(),
                    draft.getUpdatedAt()
            );

            System.out.println("Successfully committed transaction " + draft.getId() + " to production ledger.");

        } catch (Exception e) {
            System.err.println("Error committing transaction to production ledger: " + e.getMessage());
            throw new RuntimeException("Failed to commit to production ledger", e);
        }
    }
}
