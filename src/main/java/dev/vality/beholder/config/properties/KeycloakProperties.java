package dev.vality.beholder.config.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.NotEmpty;

@Getter
@Setter
@Component
@Validated
@ConfigurationProperties(prefix = "keycloak")
public class KeycloakProperties {

    @NotEmpty
    private String user;

    @NotEmpty
    private String password;

    @NotEmpty
    private String url;

    @NotEmpty
    private String resource;

}
