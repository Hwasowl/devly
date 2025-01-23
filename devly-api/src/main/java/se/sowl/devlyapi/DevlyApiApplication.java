package se.sowl.devlyapi;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@EntityScan(basePackages = {"se.sowl.devlydomain"})
@ComponentScan(basePackages = {"se.sowl.devlyapi", "se.sowl.devlydomain"})
@EnableCaching
public class DevlyApiApplication {

    public static void main(String[] args) {
        SpringApplication.run(DevlyApiApplication.class, args);
    }

}
