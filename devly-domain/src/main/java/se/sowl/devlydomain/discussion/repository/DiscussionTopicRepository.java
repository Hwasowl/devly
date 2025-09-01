package se.sowl.devlydomain.discussion.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import se.sowl.devlydomain.discussion.domain.DiscussionTopic;

import java.util.List;

public interface DiscussionTopicRepository extends JpaRepository<DiscussionTopic, Long> {
    List<DiscussionTopic> findByCategory(String category);
    
    List<DiscussionTopic> findByDifficulty(String difficulty);
    
    List<DiscussionTopic> findByCategoryAndDifficulty(String category, String difficulty);
}