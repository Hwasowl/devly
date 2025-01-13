package se.sowl.devlyexternal.client.gpt;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import se.sowl.devlyexternal.client.gpt.dto.GPTRequest;
import se.sowl.devlyexternal.client.gpt.dto.GPTResponse;

@FeignClient(name = "gpt-client", url = "${openai.api-url}")
public interface GPTClient {
    @PostMapping("/v1/chat/completions")
    GPTResponse generate(@RequestBody GPTRequest request);
}
