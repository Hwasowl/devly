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
    JOIN StudyType st ON s.typeId = st.id
    WHERE us.userId = :userId
    AND us.id IN (
        SELECT MAX(us2.id)
        FROM UserStudy us2
        JOIN Study s2 ON us2.study.id = s2.id
        JOIN StudyType st2 ON s2.typeId = st2.id
        WHERE us2.userId = :userId
        GROUP BY st2.id
    )
    """)
    List<UserStudy> findLatestByUserIdWithStudyType(@Param("userId") Long userId);

    List<UserStudy> findAllByUserId(Long userId);

    boolean existsByUserId(Long userId);
}
