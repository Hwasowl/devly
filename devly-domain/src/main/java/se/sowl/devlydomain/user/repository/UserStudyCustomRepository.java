package se.sowl.devlydomain.user.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import se.sowl.devlydomain.user.domain.UserStudy;

import java.time.LocalDateTime;

public interface UserStudyCustomRepository {
    Page<UserStudy> findCompletedStudiesWithoutNext(
        LocalDateTime fromDate,
        LocalDateTime toDate,
        LocalDateTime today,
        Pageable pageable
    );
}
