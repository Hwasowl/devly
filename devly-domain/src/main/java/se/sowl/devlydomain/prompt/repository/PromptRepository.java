package se.sowl.devlydomain.prompt.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import se.sowl.devlydomain.prompt.domain.Prompt;

import java.util.Optional;

public interface PromptRepository extends JpaRepository<Prompt, Long> {

    Optional<Prompt> findFirstByDeveloperTypeIdAndStudyTypeId(Long developerTypeId, Long studyTypeId);
}
