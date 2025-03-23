package se.sowl.devlyapi;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Import;
import se.sowl.devlydomain.config.JpaConfig;

@SpringBootApplication
@Import({JpaConfig.class})
@EntityScan(basePackages = {"se.sowl.devlydomain"})
@ComponentScan(basePackages = {"se.sowl.devlydomain", "se.sowl.devlyapi", "se.sowl.devlyexternal"})
@EnableFeignClients(basePackages = {"se.sowl.devlyexternal"})
@EnableCaching
public class DevlyApiApplication {

    public static void main(String[] args) {
        SpringApplication.run(DevlyApiApplication.class, args);
    }

}
