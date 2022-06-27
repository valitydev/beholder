package dev.vality.beholder;

import dev.vality.beholder.config.properties.PaymentsProperties;
import dev.vality.beholder.config.properties.SeleniumProperties;
import dev.vality.beholder.security.KeycloakService;
import dev.vality.beholder.service.BeholderService;
import dev.vality.beholder.service.SeleniumService;
import dev.vality.beholder.testutil.ResponseUtil;
import dev.vality.swag.payments.ApiClient;
import dev.vality.swag.payments.api.ClaimsApi;
import dev.vality.swag.payments.api.InvoicesApi;
import dev.vality.swag.payments.api.PartiesApi;
import dev.vality.swag.payments.api.ShopsApi;
import dev.vality.swag.payments.model.Party;
import org.junit.jupiter.api.*;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.actuate.metrics.AutoConfigureMetrics;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.client.HttpClientErrorException;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

@AutoConfigureMetrics
@AutoConfigureMockMvc
@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = {"beholder.cron=-", //disables scheduled execution
                "payments.request.shop-id=test",
                "payments.request.create-shop-if-not-found=true",
                "payments.request.payment-institution-id=1",
                "payments.request.category-id=1",
                "selenium.regions=BR",
                "management.server.port="})
public class BeholderFullTest {

    public static final String TEST_USER_TOKEN = "test_token";

    @MockBean
    private KeycloakService keycloakService;
    @MockBean
    private ApiClient apiClient;
    @MockBean
    private PartiesApi partiesApi;
    @MockBean
    private ShopsApi shopsApi;
    @MockBean
    private InvoicesApi invoicesApi;
    @MockBean
    private ClaimsApi claimsApi;
    @MockBean
    private SeleniumService seleniumService;

    @Autowired
    public BeholderService beholderService;

    @Autowired
    public SeleniumProperties seleniumProperties;

    @Autowired
    public PaymentsProperties paymentsProperties;

    @Autowired
    private MockMvc mockMvc;

    private AutoCloseable mocks;

    private Object[] preparedMocks;

    @BeforeEach
    public void init() {
        mocks = MockitoAnnotations.openMocks(this);
        preparedMocks = new Object[] {keycloakService, apiClient, partiesApi, shopsApi, invoicesApi, claimsApi,
                seleniumService};
    }

    @AfterEach
    public void clean() throws Exception {
        verifyNoMoreInteractions(preparedMocks);
        mocks.close();
    }

    @Test
    public void beholdWithExistingShop() throws Exception {
        Party party = ResponseUtil.getMyPartyResponse();
        String shopId = paymentsProperties.getRequest().getShopId();

        when(keycloakService.getUserToken()).thenReturn(TEST_USER_TOKEN);
        when(partiesApi.getMyParty(anyString(), anyString())).thenReturn(party);
        when(shopsApi.getShopByIDForParty(anyString(), eq(shopId),
                eq(party.getId()), anyString()))
                .thenReturn(ResponseUtil.getShopResponse(shopId));
        when(invoicesApi.createInvoice(anyString(), any(), anyString()))
                .thenReturn(ResponseUtil.getInvoiceAndToken());
        when(seleniumService.executePaymentRequest(any(), any()))
                .thenReturn(ResponseUtil.getFormDataResponse());

        beholderService.behold();


        verify(keycloakService, times(1)).getUserToken();
        verify(apiClient, times(1)).setApiKey(TEST_USER_TOKEN);
        verify(partiesApi, times(1)).getMyParty(anyString(), anyString());
        verify(shopsApi, times(1)).getShopByIDForParty(anyString(), eq(shopId),
                eq(party.getId()), anyString());
        verify(invoicesApi, times(1)).createInvoice(anyString(), any(), anyString());
        verify(seleniumService, times(1)).executePaymentRequest(any(), any());


        var mvcResult = mockMvc.perform(get("/actuator/prometheus"))
                .andReturn();
        String prometheusResponse = mvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8);
        List<String> metrics = Arrays.stream(prometheusResponse.split("\n"))
                .filter(row -> row.startsWith("beholder_")).collect(Collectors.toList());
        Assertions.assertFalse(metrics.isEmpty());
    }


    @Test
    public void beholdWithShopCreation() throws Exception {
        when(keycloakService.getUserToken()).thenReturn(TEST_USER_TOKEN);
        Party party = ResponseUtil.getMyPartyResponse();
        when(partiesApi.getMyParty(anyString(), anyString())).thenReturn(party);
        String shopId = paymentsProperties.getRequest().getShopId();
        when(shopsApi.getShopByIDForParty(anyString(), eq(shopId),
                eq(party.getId()), anyString()))
                .thenThrow(new HttpClientErrorException(HttpStatus.NOT_FOUND));
        when(claimsApi.createClaim(anyString(), any(), anyString()))
                .thenReturn(ResponseUtil.getClaim());
        when(invoicesApi.createInvoice(anyString(), any(), anyString()))
                .thenReturn(ResponseUtil.getInvoiceAndToken());
        when(seleniumService.executePaymentRequest(any(), any()))
                .thenReturn(ResponseUtil.getFormDataResponse());

        beholderService.behold();


        verify(keycloakService, times(1)).getUserToken();
        verify(apiClient, times(1)).setApiKey(TEST_USER_TOKEN);
        verify(partiesApi, times(1)).getMyParty(anyString(), anyString());
        verify(shopsApi, times(1)).getShopByIDForParty(anyString(), eq(shopId),
                eq(party.getId()), anyString());
        verify(claimsApi, times(1)).createClaim(anyString(), any(), anyString());
        verify(invoicesApi, times(1)).createInvoice(anyString(), any(), anyString());
        verify(seleniumService, times(1)).executePaymentRequest(any(), any());


        var mvcResult = mockMvc.perform(get("/actuator/prometheus"))
                .andReturn();
        String prometheusResponse = mvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8);
        List<String> metrics = Arrays.stream(prometheusResponse.split("\n"))
                .filter(row -> row.startsWith("beholder_")).collect(Collectors.toList());
        Assertions.assertFalse(metrics.isEmpty());
    }

    @Test
    public void beholdWithGetShopError() throws Exception {
        when(keycloakService.getUserToken()).thenReturn(TEST_USER_TOKEN);
        Party party = ResponseUtil.getMyPartyResponse();
        when(partiesApi.getMyParty(anyString(), anyString())).thenReturn(party);
        String shopId = paymentsProperties.getRequest().getShopId();
        when(shopsApi.getShopByIDForParty(anyString(), eq(shopId),
                eq(party.getId()), anyString()))
                .thenThrow(new HttpClientErrorException(HttpStatus.I_AM_A_TEAPOT));

        beholderService.behold();


        verify(keycloakService, times(1)).getUserToken();
        verify(apiClient, times(1)).setApiKey(TEST_USER_TOKEN);
        verify(partiesApi, times(1)).getMyParty(anyString(), anyString());
        verify(shopsApi, times(1)).getShopByIDForParty(anyString(), eq(shopId),
                eq(party.getId()), anyString());

        var mvcResult = mockMvc.perform(get("/actuator/prometheus"))
                .andReturn();
        String prometheusResponse = mvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8);
        List<String> metrics = Arrays.stream(prometheusResponse.split("\n"))
                .filter(row -> row.startsWith("beholder_")).collect(Collectors.toList());
        Assertions.assertFalse(metrics.isEmpty());
    }


}
