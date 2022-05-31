package dev.vality.beholder.security;

import dev.vality.beholder.config.properties.KeycloakProperties;
import lombok.RequiredArgsConstructor;
import org.keycloak.OAuth2Constants;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

@Service
@RequiredArgsConstructor
public class KeycloakService {

    private final RestTemplate restTemplate;
    private final KeycloakProperties keycloakProperties;

    public String getUserToken() {
        HttpEntity<MultiValueMap<String, String>> request =
                new TokenRequest.Builder(keycloakProperties.getResource(), OAuth2Constants.PASSWORD)
                        .add("username", keycloakProperties.getUser())
                        .add("password", keycloakProperties.getPassword())
                        .build();
        ResponseEntity<String> response =
                restTemplate.postForEntity(keycloakProperties.getUrl(), request, String.class);
        return response.getBody();
    }

}
