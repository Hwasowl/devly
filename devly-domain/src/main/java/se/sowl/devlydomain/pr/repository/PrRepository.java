package se.sowl.devlydomain.pr.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import se.sowl.devlydomain.pr.domain.Pr;

import java.time.LocalDateTime;
import java.util.List;

public interface PrRepository extends JpaRepository<Pr, Long> {

    List<Pr> findPrsByCreatedAtAfter(LocalDateTime localDateTime);

    Pr findByStudyId(Long studyId);
}
