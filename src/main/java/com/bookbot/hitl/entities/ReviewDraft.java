package com.bookbot.hitl.entities;
import com.bookbot.hitl.models.TransactionSplit;
import io.hypersistence.utils.hibernate.type.json.JsonBinaryType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Type;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
@Setter
@Getter
@Entity
@Table(name = "reviews")
public class ReviewDraft {
    // Getters and Setters
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;
    private LocalDate transactionDate;
    @Enumerated(EnumType.STRING)
    private TransactionType type;
    private String category;
    private BigDecimal amount;
    private String person;
    @Type(JsonBinaryType.class)
    @Column(columnDefinition = "jsonb")
    private List<TransactionSplit> splits;
    @Enumerated(EnumType.STRING)
    private ReviewStatus status;
    private boolean isDuplicate;
    private UUID duplicateOfId;
    private String mediaUrl;
    @Enumerated(EnumType.STRING)
    private MediaType mediaType;
    @Column(columnDefinition = "text")
    private String rawOcrOrTranscript;
    private OffsetDateTime createdAt = OffsetDateTime.now();
    private OffsetDateTime updatedAt = OffsetDateTime.now();

}
