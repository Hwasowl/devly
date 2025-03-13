package se.sowl.devlyexternal.client.config;

import feign.Client;
import feign.Request;
import feign.RequestInterceptor;
import feign.codec.ErrorDecoder;
import feign.codec.ErrorDecoder.Default;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import se.sowl.devlyexternal.client.gpt.exception.GPTClientException;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
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
            60000,
            TimeUnit.MILLISECONDS,
            60000,
            TimeUnit.MILLISECONDS,
            true
        );
    }

    @Bean
    public Client feignClient() throws Exception {
        TrustManager[] trustAllCerts = new TrustManager[] {
            new X509TrustManager() {
                public X509Certificate[] getAcceptedIssuers() { return null; }
                public void checkClientTrusted(X509Certificate[] certs, String authType) { }
                public void checkServerTrusted(X509Certificate[] certs, String authType) { }
            }
        };

        SSLContext sslContext = SSLContext.getInstance("TLS");
        sslContext.init(null, trustAllCerts, new SecureRandom());

        return new Client.Default(
            sslContext.getSocketFactory(),
            (hostname, session) -> true
        );
    }

    @Bean
    public ErrorDecoder errorDecoder() {
        return (methodKey, response) -> {
            if (response.status() >= 400) {
                return new GPTClientException("GPT API error: " + response.reason());
            }
            return new Default().decode(methodKey, response);
        };
    }
}
