package se.sowl.devlydomain.prompt.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import se.sowl.devlydomain.prompt.domain.RolePrompt;

import java.util.Optional;

public interface RolePromptRepository extends JpaRepository<RolePrompt, Long> {

    Optional<RolePrompt> findFirstByDeveloperTypeIdAndStudyTypeId(Long developerTypeId, Long studyTypeId);
}
