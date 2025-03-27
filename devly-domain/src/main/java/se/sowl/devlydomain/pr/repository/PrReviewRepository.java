package se.sowl.devlydomain.pr.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import se.sowl.devlydomain.pr.domain.PrReview;

import java.util.Optional;

public interface PrReviewRepository extends JpaRepository<PrReview, Long> {
    Optional<PrReview> findByPrCommentId(Long prCommentId);
}
