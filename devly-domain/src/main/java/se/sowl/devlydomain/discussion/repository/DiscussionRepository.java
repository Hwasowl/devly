package se.sowl.devlydomain.discussion.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import se.sowl.devlydomain.discussion.domain.Discussion;
import se.sowl.devlydomain.discussion.domain.DiscussionStatus;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface DiscussionRepository extends JpaRepository<Discussion, Long> {
    List<Discussion> findByUserIdOrderByCreatedAtDesc(Long userId);
    
    List<Discussion> findByUserIdAndStatus(Long userId, DiscussionStatus status);
    
    @Query("SELECT d FROM Discussion d WHERE d.user.id = :userId AND d.status = :status ORDER BY d.createdAt DESC")
    Optional<Discussion> findLatestByUserIdAndStatus(@Param("userId") Long userId, @Param("status") DiscussionStatus status);
    
    @Query("SELECT d FROM Discussion d WHERE d.createdAt BETWEEN :startDate AND :endDate")
    List<Discussion> findByCreatedAtBetween(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);
    
    @Query("SELECT d FROM Discussion d WHERE d.user.id = :userId AND d.status = 'COMPLETED' ORDER BY d.completedAt DESC")
    List<Discussion> findCompletedByUserId(@Param("userId") Long userId);
}