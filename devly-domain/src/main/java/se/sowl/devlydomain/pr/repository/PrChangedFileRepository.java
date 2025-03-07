package se.sowl.devlydomain.pr.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import se.sowl.devlydomain.pr.domain.PrChangedFile;

import java.util.List;

public interface PrChangedFileRepository extends JpaRepository<PrChangedFile, Long> {
    List<PrChangedFile> findByPrId(Long pullRequestId);
}
