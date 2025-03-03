package se.sowl.devlydomain.pr.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import se.sowl.devlydomain.pr.domain.PrLabel;

import java.util.List;

public interface PrLabelRepository extends JpaRepository<PrLabel, Long> {
    List<PrLabel> findAllByPrId(Long prId);
}
