package se.sowl.devlybatch;

import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Import;
import org.springframework.scheduling.annotation.EnableScheduling;
import se.sowl.devlydomain.config.JpaConfig;

@SpringBootApplication
@Import(JpaConfig.class)
@EntityScan(basePackages = {"se.sowl.devlydomain"})
@ComponentScan(basePackages = {"se.sowl.devlybatch", "se.sowl.devlydomain", "se.sowl.devlyexternal"})
@EnableFeignClients(basePackages = {"se.sowl.devlyexternal"})
@EnableBatchProcessing
@EnableScheduling
public class DevlyBatchApplication {

    public static void main(String[] args) {
        SpringApplication.run(DevlyBatchApplication.class, args);
    }

}
