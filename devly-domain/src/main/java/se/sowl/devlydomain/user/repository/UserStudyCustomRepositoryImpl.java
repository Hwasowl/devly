package se.sowl.devlydomain.user.repository;

import com.querydsl.jpa.impl.JPAQueryFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import se.sowl.devlydomain.user.domain.QUserStudy;
import se.sowl.devlydomain.user.domain.UserStudy;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public class UserStudyCustomRepositoryImpl implements UserStudyCustomRepository {
    private final JPAQueryFactory queryFactory;

    @Autowired
    public UserStudyCustomRepositoryImpl(JPAQueryFactory queryFactory) {
        this.queryFactory = queryFactory;
    }

    @Override
    public Page<UserStudy> findCompletedStudiesWithoutNext(LocalDateTime yesterday, LocalDateTime todayStart, LocalDate today, Pageable pageable) {
        QUserStudy userStudy = QUserStudy.userStudy;
        QUserStudy nextStudy = new QUserStudy("nextStudy");

        List<UserStudy> content = queryFactory
            .selectFrom(userStudy)
            .leftJoin(nextStudy)
            .on(
                nextStudy.userId.eq(userStudy.userId),
                nextStudy.scheduledAt.eq(today)
            )
            .where(
                userStudy.completedAt.between(yesterday, todayStart),
                userStudy.isCompleted.isTrue(),
                nextStudy.id.isNull()
            )
            .orderBy(userStudy.id.asc())
            .offset(pageable.getOffset())
            .limit(pageable.getPageSize())
            .fetch();

        long total = queryFactory
            .selectFrom(userStudy)
            .leftJoin(nextStudy)
            .on(
                nextStudy.userId.eq(userStudy.userId),
                nextStudy.scheduledAt.eq(today)
            )
            .where(
                userStudy.completedAt.between(yesterday, todayStart),
                userStudy.isCompleted.isTrue(),
                nextStudy.id.isNull()
            )
            .fetch().size();

        return new PageImpl<>(content, pageable, total);
    }
}
