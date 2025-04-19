package se.sowl.devlydomain.word.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import se.sowl.devlydomain.word.domain.WordReview;

import java.util.List;

public interface WordReviewRepository extends JpaRepository<WordReview, Long> {
    boolean existsByUserStudyId(Long userStudyId);

    List<WordReview> findAllByUserStudyId(Long studyId);

    long countByCorrectAndUserStudyId(boolean correct, Long userStudyId);

    Long countByUserStudyIdAndCorrectIsFalse(Long userStudyId);

    List<WordReview> findByUserStudyId(Long userStudyId);
}
