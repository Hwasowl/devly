package se.sowl.devlydomain.pr.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import se.sowl.devlydomain.pr.domain.PrComment;

import java.util.List;

public interface PrCommentRepository extends JpaRepository<PrComment, Long> {
    List<PrComment> findByPrId(Long pullRequestId);
}
