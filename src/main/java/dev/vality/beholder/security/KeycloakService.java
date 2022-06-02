package dev.vality.beholder.security;

import dev.vality.beholder.config.properties.KeycloakProperties;
import dev.vality.beholder.exception.BadFormatException;
import dev.vality.beholder.exception.BadResponseException;
import lombok.RequiredArgsConstructor;
import org.json.JSONException;
import org.json.JSONObject;
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

        if (!response.getStatusCode().is2xxSuccessful()) {
            throw new BadResponseException(
                    String.format("Keycloak response: code: %s, body: %s", response.getStatusCode(),
                            response.getBody()));
        }
        return getAccessToken(response.getBody());
    }

    private String getAccessToken(String body) {
        try {
            JSONObject json = new JSONObject(body);
            return json.getString("access_token");
        } catch (JSONException e) {
            throw new BadFormatException(e);
        }
    }

}
