package se.sowl.devlydomain.word.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import se.sowl.devlydomain.word.domain.WordReview;

public interface WordReviewRepository extends JpaRepository<WordReview, Long> {
}
