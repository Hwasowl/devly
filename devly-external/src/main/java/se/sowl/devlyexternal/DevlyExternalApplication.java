package se.sowl.devlyexternal;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Import;
import se.sowl.devlydomain.config.JpaConfig;
import se.sowl.devlyexternal.client.config.FeignConfig;

@SpringBootApplication
@Import({FeignConfig.class, JpaConfig.class})
@ComponentScan(basePackages = {"se.sowl.devlydomain", "se.sowl.devlyexternal"})
public class DevlyExternalApplication {

	public static void main(String[] args) {
		SpringApplication.run(DevlyExternalApplication.class, args);
	}

}
