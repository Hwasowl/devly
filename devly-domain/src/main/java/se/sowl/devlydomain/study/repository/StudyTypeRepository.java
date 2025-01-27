package se.sowl.devlydomain.study.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import se.sowl.devlydomain.study.domain.StudyType;

public interface StudyTypeRepository extends JpaRepository<StudyType, Long> {
}
