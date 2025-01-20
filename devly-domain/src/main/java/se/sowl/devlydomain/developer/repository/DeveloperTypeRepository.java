package se.sowl.devlydomain.developer.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import se.sowl.devlydomain.developer.domain.DeveloperType;

public interface DeveloperTypeRepository extends JpaRepository<DeveloperType, Long>{

}
