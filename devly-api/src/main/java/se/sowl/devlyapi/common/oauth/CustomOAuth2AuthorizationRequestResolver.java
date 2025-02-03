package se.sowl.devlyapi.common.oauth;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.DefaultOAuth2AuthorizationRequestResolver;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizationRequestResolver;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

@Component
public class CustomOAuth2AuthorizationRequestResolver implements OAuth2AuthorizationRequestResolver {
    private final ObjectMapper objectMapper;
    private final DefaultOAuth2AuthorizationRequestResolver defaultResolver;

    public CustomOAuth2AuthorizationRequestResolver (ClientRegistrationRepository clientRegistrationRepository) {
        this.objectMapper = new ObjectMapper();
        this.defaultResolver = new DefaultOAuth2AuthorizationRequestResolver(
            clientRegistrationRepository,
            "/oauth2/authorization"
        );
    }

    @Override
    public OAuth2AuthorizationRequest resolve(HttpServletRequest request) {
        OAuth2AuthorizationRequest auth2Request = defaultResolver.resolve(request);
        return customizeAuthRequest(request, auth2Request);
    }

    @Override
    public OAuth2AuthorizationRequest resolve(HttpServletRequest request, String clientRegistrationId) {
        OAuth2AuthorizationRequest auth2Request = defaultResolver.resolve(request, clientRegistrationId);
        return customizeAuthRequest(request, auth2Request);
    }

    private OAuth2AuthorizationRequest customizeAuthRequest(
        HttpServletRequest request,
        OAuth2AuthorizationRequest authRequest
    ) {
        if (authRequest == null) {
            return null;
        }

        String developerType = validateAndGetDeveloperType(request);
        String encodedState = createEncodedState(authRequest, developerType);
        Map<String, Object> additionalParameters = createAdditionalParameters(authRequest, developerType);

        return OAuth2AuthorizationRequest.from(authRequest)
            .state(encodedState)
            .additionalParameters(additionalParameters)
            .build();
    }

    private String validateAndGetDeveloperType(HttpServletRequest request) {
        String developerType = request.getParameter("developerType");
        if (developerType == null) {
            throw new OAuth2AuthenticationException("Developer type is required");
        }
        return developerType;
    }

    private String createEncodedState(OAuth2AuthorizationRequest authRequest, String developerType) {
        try {
            Map<String, String> stateData = new HashMap<>();
            stateData.put("developerType", developerType);
            stateData.put("originalState", authRequest.getState());

            return Base64.getUrlEncoder().encodeToString(
                objectMapper.writeValueAsString(stateData).getBytes(StandardCharsets.UTF_8)
            );
        } catch (JsonProcessingException e) {
            throw new OAuth2AuthenticationException("Failed to encode state");
        }
    }

    private Map<String, Object> createAdditionalParameters(
        OAuth2AuthorizationRequest authRequest,
        String developerType
    ) {
        Map<String, Object> additionalParameters = new HashMap<>(authRequest.getAdditionalParameters());
        additionalParameters.put("developerType", developerType);
        return additionalParameters;
    }
}
