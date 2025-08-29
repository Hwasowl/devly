package se.sowl.devlydomain.study.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import se.sowl.devlydomain.study.domain.Study;
import se.sowl.devlydomain.study.domain.StudyStatus;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface StudyRepository extends JpaRepository<Study, Long> {
    List<Study> findByCreatedAtBetweenAndStudyTypeIdAndStatus(LocalDateTime startOfDay, LocalDateTime endOfDay, Long typeId, StudyStatus status);

    List<Study> findByDeveloperTypeId(Long developerTypeId);

    Optional<Study> findFirstByStudyTypeId(Long typeId);

    // TODO: Consider Pageable
    List<Study> findAllByStatusOrderById(StudyStatus status);
}
