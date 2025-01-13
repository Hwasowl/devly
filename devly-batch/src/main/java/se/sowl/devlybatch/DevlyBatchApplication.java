package se.sowl.devlybatch;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@EntityScan(basePackages = {"se.sowl.devlydomain"})
@EnableJpaRepositories(basePackages = {"se.sowl.devlydomain"})
public class DevlyBatchApplication {

    public static void main(String[] args) {
        SpringApplication.run(DevlyBatchApplication.class, args);
    }

}
