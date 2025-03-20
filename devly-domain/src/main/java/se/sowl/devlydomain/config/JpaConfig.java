package se.sowl.devlydomain.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@Configuration
@EnableJpaRepositories(basePackages = "se.sowl.devlydomain")
@EnableJpaAuditing
public class JpaConfig {
}
