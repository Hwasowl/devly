package se.sowl.devlydomain.prompt.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import se.sowl.devlydomain.prompt.domain.GeneratePrompt;

import java.util.Optional;

public interface PromptRepository extends JpaRepository<GeneratePrompt, Long> {

    Optional<GeneratePrompt> findFirstByDeveloperTypeIdAndStudyTypeId(Long developerTypeId, Long studyTypeId);
}
