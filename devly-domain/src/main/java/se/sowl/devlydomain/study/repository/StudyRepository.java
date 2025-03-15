package se.sowl.devlydomain.study.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import se.sowl.devlydomain.study.domain.Study;
import se.sowl.devlydomain.study.domain.StudyStatusEnum;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface StudyRepository extends JpaRepository<Study, Long> {
    List<Study> findByCreatedAtBetweenAndTypeIdAndStatus(LocalDateTime startOfDay, LocalDateTime endOfDay, Long typeId, StudyStatusEnum status);

    List<Study> findByDeveloperTypeId(Long developerTypeId);

    Optional<Study> findFirstByTypeId(Long typeId);

    // TODO: Consider Pageable
    List<Study> findAllByStatusOrderById(StudyStatusEnum status);
}
