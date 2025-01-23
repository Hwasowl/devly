package se.sowl.devlybatch.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.context.annotation.Configuration;

@Getter
@Setter
@Configuration
public class StudyBatchProperties {
    private int chunkSize = 100;
    private int retryLimit = 3;
    private int skipLimit = 10;
}
