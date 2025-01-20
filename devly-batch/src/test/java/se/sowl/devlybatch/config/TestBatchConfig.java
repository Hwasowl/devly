package se.sowl.devlybatch.config;

import org.springframework.boot.autoconfigure.batch.BatchAutoConfiguration;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Import;

@TestConfiguration
@Import(BatchAutoConfiguration.class)
public class TestBatchConfig {
}
