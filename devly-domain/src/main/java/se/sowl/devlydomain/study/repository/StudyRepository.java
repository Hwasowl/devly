package se.sowl.devlydomain.study.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import se.sowl.devlydomain.study.domain.Study;

import java.time.LocalDateTime;
import java.util.List;

public interface StudyRepository extends JpaRepository<Study, Long> {
    List<Study> findByCreatedAtBetween(LocalDateTime startOfDay, LocalDateTime endOfDay);

    Study findByDeveloperTypeId(Long developerTypeId);

    // TODO: Consider Pageable
    List<Study> findAllByOrderById();
}
