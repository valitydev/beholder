package dev.vality.beholder.security;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.util.Collections;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class TokenRequest {
    public static class Builder {
        MultiValueMap<String, String> data;

        public Builder(String clientId, String grantType) {
            data = new LinkedMultiValueMap<>();
            data.put("client_id", Collections.singletonList(clientId));
            data.put("grant_type", Collections.singletonList(grantType));
        }

        public Builder add(String key, String value) {
            data.put(key, Collections.singletonList(value));
            return this;
        }

        public HttpEntity<MultiValueMap<String, String>> build() {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
            return new HttpEntity<>(data, headers);
        }
    }

}
