package dev.vality.beholder.config.properties;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.AssertTrue;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.List;

@Getter
@Setter
@Component
@Validated
@ConfigurationProperties(prefix = "selenium")
public class SeleniumProperties {

    private String url;

    private Integer port;

    @NotNull
    private Boolean useExternalProvider;

    @NotEmpty
    private List<String> regions;

    private LambdaTestProperties lambdaTest;

    @Data
    public static class LambdaTestProperties {

        private String user;

        private String token;

        private Boolean network;

        private Boolean visual;

        private Boolean video;

        private Boolean console;
    }

    @AssertTrue(message = "Check 'use-external-provider' option and related parameters")
    private boolean isSeleniumConfigurationValid() {
        return useExternalProvider && lambdaTest != null && !ObjectUtils.isEmpty(lambdaTest.token)
                && !ObjectUtils.isEmpty(lambdaTest.user)
                || !useExternalProvider && !ObjectUtils.isEmpty(url);
    }
}
