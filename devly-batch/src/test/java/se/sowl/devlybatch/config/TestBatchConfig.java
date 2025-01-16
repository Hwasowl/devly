package se.sowl.devlybatch.config;

import org.springframework.boot.autoconfigure.batch.BatchAutoConfiguration;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Import;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@TestConfiguration
@Import(BatchAutoConfiguration.class)
@EnableJpaAuditing
public class TestBatchConfig {
}
