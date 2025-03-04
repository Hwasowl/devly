package se.sowl.devlydomain.study.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import se.sowl.devlydomain.study.domain.Study;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface StudyRepository extends JpaRepository<Study, Long> {
    List<Study> findByCreatedAtBetweenAndTypeId(LocalDateTime startOfDay, LocalDateTime endOfDay, Long typeId);

    List<Study> findByDeveloperTypeId(Long developerTypeId);

    Optional<Study> findFirstByTypeId(Long typeId);

    // TODO: Consider Pageable
    List<Study> findAllByOrderById();
}
