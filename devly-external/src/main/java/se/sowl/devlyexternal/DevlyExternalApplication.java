package se.sowl.devlyexternal;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Import;
import se.sowl.devlydomain.config.JpaConfig;
import se.sowl.devlyexternal.client.config.FeignConfig;

@SpringBootApplication
@EntityScan(basePackages = {"se.sowl.devlydomain"})
@ComponentScan(basePackages = {"se.sowl.devlydomain", "se.sowl.devlyexternal"})
@Import({FeignConfig.class, JpaConfig.class})
public class DevlyExternalApplication {

	public static void main(String[] args) {
		SpringApplication.run(DevlyExternalApplication.class, args);
	}

}
