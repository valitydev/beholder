package dev.vality.beholder.config.properties;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.AssertTrue;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

@Getter
@Setter
@Component
@Validated
@ConfigurationProperties(prefix = "payments")
public class PaymentsProperties {

    @NotEmpty
    private String apiUrl;

    private Long apiTimeoutSec = 10L;

    @NotEmpty
    private String formUrl;

    private Long formTimeoutSec = 30L;

    @NotNull
    private Request request;

    @Data
    public static class Request {
        @NotNull
        private String shopId;
        private Boolean createShopIfNotFound = false;
        private Integer paymentInstitutionId;
        private Integer categoryId;
    }

    @AssertTrue(message = "Check 'create-shop-if-not-found' option and related parameters")
    private boolean isRequestConfigurationValid() {
        return !request.createShopIfNotFound
                || (request.getPaymentInstitutionId() != null && request.getCategoryId() != null);
    }

}
