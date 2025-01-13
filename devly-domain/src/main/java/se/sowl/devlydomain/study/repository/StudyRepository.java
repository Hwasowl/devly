package se.sowl.devlydomain.study.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import se.sowl.devlydomain.study.domain.Study;

public interface StudyRepository extends JpaRepository<Study, Long> {
}
