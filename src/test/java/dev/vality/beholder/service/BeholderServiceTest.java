package dev.vality.beholder.service;

import dev.vality.beholder.model.FormDataRequest;
import dev.vality.beholder.model.FormDataResponse;
import dev.vality.beholder.model.Region;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.web.client.HttpClientErrorException;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@SpringBootTest(properties = {"beholder.cron=-",
        "selenium.regions=AL,AD,AR"})
class BeholderServiceTest {

    @Autowired
    BeholderService beholderService;

    @Autowired
    List<Region> regions;

    @MockBean
    PaymentsService paymentsService;

    @MockBean
    SeleniumService seleniumService;

    @MockBean
    MetricsService metricsService;

    private AutoCloseable mocks;

    private Object[] preparedMocks;

    @BeforeEach
    void setUp() {
        mocks = MockitoAnnotations.openMocks(this);
        preparedMocks = new Object[] {paymentsService, seleniumService, metricsService};
    }

    @AfterEach
    void tearDown() throws Exception {
        verifyNoMoreInteractions(preparedMocks);
        mocks.close();
    }

    @Test
    void behold() {
        var firstRequest = new FormDataRequest();
        var secondRequest = new FormDataRequest();
        when(paymentsService.prepareFormData())
                .thenReturn(new FormDataRequest())
                .thenReturn(new FormDataRequest())
                .thenThrow(HttpClientErrorException.class);

        var firstResponse = FormDataResponse.builder().build();
        var secondResponse = FormDataResponse.builder().build();
        when(seleniumService.executePaymentRequest(firstRequest, regions.get(0)))
                .thenReturn(firstResponse);
        when(seleniumService.executePaymentRequest(secondRequest, regions.get(1)))
                .thenReturn(secondResponse);

        beholderService.behold();

        verify(paymentsService, times(3)).prepareFormData();
        verify(seleniumService, times(2)).executePaymentRequest(any(), any());
        verify(metricsService, times(1)).updateMetrics(List.of(firstResponse, secondResponse));
    }
}