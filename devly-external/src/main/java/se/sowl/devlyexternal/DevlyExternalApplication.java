package se.sowl.devlyexternal;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients
public class DevlyExternalApplication {

	public static void main(String[] args) {
		SpringApplication.run(DevlyExternalApplication.class, args);
	}

}
