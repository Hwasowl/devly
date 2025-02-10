package se.sowl.devlydomain.word.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import se.sowl.devlydomain.word.domain.Word;

import java.time.LocalDateTime;
import java.util.List;

public interface WordRepository extends JpaRepository<Word, Long> {
    List<Word> findAllByStudyId(Long studyId);

    Word findByStudyId(Long studyId);

    List<Word> findWordsByCreatedAtAfter(LocalDateTime localDateTime);

    // count the number of completed words
}
