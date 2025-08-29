package se.sowl.devlydomain.user.repository;

import com.querydsl.jpa.impl.JPAQueryFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import se.sowl.devlydomain.user.domain.UserStudy;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public class UserStudyCustomRepositoryImpl implements UserStudyCustomRepository {
    private final EntityManager entityManager;

    @Autowired
    public UserStudyCustomRepositoryImpl(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @Override
    public Page<UserStudy> findCompletedStudiesWithoutNext(
        LocalDateTime yesterday,
        LocalDateTime todayStart,
        LocalDateTime today,
        Pageable pageable
    ) {
        String jpql = """
            SELECT us FROM UserStudy us 
            WHERE us.completedAt BETWEEN :yesterday AND :todayStart 
            AND us.isCompleted = true 
            AND NOT EXISTS (
                SELECT 1 FROM UserStudy nextStudy 
                WHERE nextStudy.user.id = us.user.id 
                AND nextStudy.scheduledAt = :today
            )
            ORDER BY us.id ASC
            """;

        TypedQuery<UserStudy> query = entityManager.createQuery(jpql, UserStudy.class)
            .setParameter("yesterday", yesterday)
            .setParameter("todayStart", todayStart)
            .setParameter("today", today)
            .setFirstResult((int) pageable.getOffset())
            .setMaxResults(pageable.getPageSize());

        List<UserStudy> content = query.getResultList();

        String countJpql = """
            SELECT COUNT(us) FROM UserStudy us 
            WHERE us.completedAt BETWEEN :yesterday AND :todayStart 
            AND us.isCompleted = true 
            AND NOT EXISTS (
                SELECT 1 FROM UserStudy nextStudy 
                WHERE nextStudy.user.id = us.user.id 
                AND nextStudy.scheduledAt = :today
            )
            """;

        TypedQuery<Long> countQuery = entityManager.createQuery(countJpql, Long.class)
            .setParameter("yesterday", yesterday)
            .setParameter("todayStart", todayStart)
            .setParameter("today", today);

        long total = countQuery.getSingleResult();

        return new PageImpl<>(content, pageable, total);
    }
}
