package se.sowl.devlyexternal.client.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FeignConfig {
    @Bean
    public FeignClientConfig feignClientConfig() {
        return new FeignClientConfig();
    }
}
