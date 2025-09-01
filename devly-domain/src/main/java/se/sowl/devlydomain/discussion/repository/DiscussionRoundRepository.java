package se.sowl.devlydomain.discussion.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import se.sowl.devlydomain.discussion.domain.DiscussionRound;

import java.util.List;
import java.util.Optional;

public interface DiscussionRoundRepository extends JpaRepository<DiscussionRound, Long> {
    List<DiscussionRound> findByDiscussionIdOrderByRoundNumberAsc(Long discussionId);
    
    @Query("SELECT dr FROM DiscussionRound dr WHERE dr.discussion.id = :discussionId AND dr.roundNumber = :roundNumber")
    Optional<DiscussionRound> findByDiscussionIdAndRoundNumber(@Param("discussionId") Long discussionId, @Param("roundNumber") Integer roundNumber);
    
    @Query("SELECT dr FROM DiscussionRound dr WHERE dr.discussion.id = :discussionId ORDER BY dr.roundNumber DESC LIMIT 1")
    Optional<DiscussionRound> findLatestByDiscussionId(@Param("discussionId") Long discussionId);
}