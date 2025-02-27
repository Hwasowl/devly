package se.sowl.devlydomain.user.repository;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import se.sowl.devlydomain.user.domain.UserStudy;

import java.util.List;
import java.util.Optional;

public interface UserStudyRepository extends JpaRepository<UserStudy, Long>, UserStudyCustomRepository {
    Optional<UserStudy> findByUserIdAndStudyId(Long userId, Long studyId);

    @Query("SELECT us FROM UserStudy us JOIN FETCH us.study WHERE us.userId = :userId")
    List<UserStudy> findAllWithStudyByUserId(@Param("userId") Long userId);

    @Query("""
    SELECT us FROM UserStudy us
    JOIN FETCH us.study s
    WHERE us.userId = :userId
    AND us.id IN (
        SELECT MAX(us2.id)
        FROM UserStudy us2
        JOIN us2.study s2
        WHERE us2.userId = :userId
        GROUP BY s2.typeId
    )
    """)
    List<UserStudy> findLatestByUserIdWithStudyType(@Param("userId") Long userId);

    List<UserStudy> findAllByUserId(Long userId);

    boolean existsByUserId(Long userId);
}
