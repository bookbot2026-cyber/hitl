package com.bookbot.hitl.repositories;
import com.bookbot.hitl.entities.ReviewDraft;
import com.bookbot.hitl.entities.ReviewStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
public interface ReviewDraftRepository extends JpaRepository<ReviewDraft, UUID> {

    @Query("SELECT r FROM ReviewDraft r WHERE r.amount = :amount AND r.person = :person AND r.createdAt >= :since")
    List<ReviewDraft> findDuplicateInLast24Hours(
            @Param("amount") BigDecimal amount,
            @Param("person") String person,
            @Param("since") OffsetDateTime since
    );
    List<ReviewDraft> findByStatus(ReviewStatus status);
}
