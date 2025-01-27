package se.sowl.devlydomain.user.repository;


import org.springframework.data.jpa.repository.JpaRepository;
import se.sowl.devlydomain.user.domain.UserStudy;

import java.util.List;
import java.util.Optional;

public interface UserStudyRepository extends JpaRepository<UserStudy, Long>, UserStudyCustomRepository {
    List<UserStudy> findAllByUserId(Long userId);

    Optional<UserStudy> findByUserIdAndStudyId(Long userId, Long studyId);
}
