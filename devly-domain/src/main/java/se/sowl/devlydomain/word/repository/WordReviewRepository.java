package se.sowl.devlydomain.word.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import se.sowl.devlydomain.word.domain.WordReview;

import java.util.List;

public interface WordReviewRepository extends JpaRepository<WordReview, Long> {
    boolean existsByStudyIdAndUserId(Long studyId, Long userId);

    List<WordReview> findAllByStudyIdAndUserId(Long studyId, Long userId);

    long countByCorrectAndStudyIdAndUserId(boolean correct, Long studyId, Long userId);

    Long countByUserIdAndStudyIdAndCorrectIsFalse(Long userId, Long studyId);

    List<WordReview> findByStudyIdAndUserId(Long studyId, Long userId);
}
