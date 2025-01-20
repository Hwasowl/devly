package se.sowl.devlyexternal.client.config;

import feign.Request;
import feign.RequestInterceptor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

@Configuration
public class FeignClientConfig {
    @Value("${openai.api-key}")
    private String apiKey;

    @Bean
    public RequestInterceptor bearerTokenRequestInterceptor() {
        return requestTemplate -> requestTemplate.header("Authorization", "Bearer " + apiKey);
    }

    @Bean
    public Request.Options feignOptions() {
        return new Request.Options(
            60000, // connectTimeout: 30초
            TimeUnit.MILLISECONDS,
            60000, // readTimeout: 30초
            TimeUnit.MILLISECONDS,
            true   // followRedirects
        );
    }
}
