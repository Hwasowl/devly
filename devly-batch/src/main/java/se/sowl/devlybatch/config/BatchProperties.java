package se.sowl.devlybatch.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Getter
@Setter
@Configuration
public class BatchProperties {
    @Value("${spring.jpa.properties.hibernate.batch_size}")
    private int chunkSize;

    @Value("${spring.batch.retry-limit}")
    private int retryLimit;

    @Value("${spring.batch.skip-limit}")
    private int skipLimit;
}
