package se.sowl.devlybatch;

import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EntityScan(basePackages = {"se.sowl.devlydomain"})
@ComponentScan(basePackages = {"se.sowl.devlybatch", "se.sowl.devlydomain"})
@EnableJpaRepositories(basePackages = {"se.sowl.devlydomain"})
//@ConfigurationPropertiesScan(basePackages = {"se.sowl.devlydomain"})
@EnableFeignClients(basePackages = {"se.sowl.devlyexternal"})
@EnableBatchProcessing
@EnableScheduling
@EnableJpaAuditing
public class DevlyBatchApplication {

    public static void main(String[] args) {
        SpringApplication.run(DevlyBatchApplication.class, args);
    }

}
