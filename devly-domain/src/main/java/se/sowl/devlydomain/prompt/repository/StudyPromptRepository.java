package se.sowl.devlydomain.prompt.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import se.sowl.devlydomain.prompt.domain.StudyPrompt;

import java.util.Optional;

public interface StudyPromptRepository extends JpaRepository<StudyPrompt, Long> {
    Optional<StudyPrompt> findByDeveloperTypeIdAndStudyTypeId(Long developerTypeId, Long studyTypeId);
}