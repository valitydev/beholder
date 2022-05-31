package dev.vality.beholder.config;

import com.fasterxml.jackson.annotation.JsonInclude;
import dev.vality.beholder.config.properties.PaymentsProperties;
import dev.vality.swag.payments.ApiClient;
import dev.vality.swag.payments.api.ClaimsApi;
import dev.vality.swag.payments.api.InvoicesApi;
import dev.vality.swag.payments.api.PartiesApi;
import dev.vality.swag.payments.api.ShopsApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.RestTemplate;

import java.text.SimpleDateFormat;

@Configuration
public class PaymentsConfig {

    private static final String BEARER_TYPE = "Bearer";
    private static final String DATE_TIME_PATTERN = "yyyy-MM-dd'T'HH:mm:ss.SSSZ";

    @Bean
    public ClientHttpRequestFactory clientHttpRequestFactory(PaymentsProperties paymentsProperties) {
        var requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(paymentsProperties.getApiTimeoutSec().intValue() * 1000);
        return requestFactory;
    }

    @Bean
    public RestTemplate restTemplate(ClientHttpRequestFactory clientHttpRequestFactory) {
        var restTemplate = new RestTemplate(clientHttpRequestFactory);
        restTemplate.getMessageConverters()
                .removeIf(m -> m.getClass().getName().equals(MappingJackson2HttpMessageConverter.class.getName()));
        Jackson2ObjectMapperBuilder builder =
                new Jackson2ObjectMapperBuilder()
                        .serializationInclusion(JsonInclude.Include.NON_NULL)
                        .dateFormat(new SimpleDateFormat(DATE_TIME_PATTERN));
        restTemplate.getMessageConverters().add(new MappingJackson2HttpMessageConverter(builder.build()));
        return restTemplate;
    }

    @Bean
    public ApiClient apiClient(RestTemplate restTemplate, PaymentsProperties paymentsProperties) {
        ApiClient apiClient = new ApiClient(restTemplate);
        apiClient.setBasePath(paymentsProperties.getApiUrl());
        apiClient.setApiKeyPrefix(BEARER_TYPE);
        return apiClient;
    }

    @Bean
    public PartiesApi partiesApi(ApiClient apiClient) {
        return new PartiesApi(apiClient);
    }

    @Bean
    public ClaimsApi claimsApi(ApiClient apiClient) {
        return new ClaimsApi(apiClient);
    }

    @Bean
    public ShopsApi shopsApi(ApiClient apiClient) {
        return new ShopsApi(apiClient);
    }

    @Bean
    public InvoicesApi invoicesApi(ApiClient apiClient) {
        return new InvoicesApi(apiClient);
    }
}
