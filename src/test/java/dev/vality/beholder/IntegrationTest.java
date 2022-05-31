package dev.vality.beholder;

import dev.vality.beholder.config.properties.SeleniumProperties;
import dev.vality.beholder.security.KeycloakService;
import dev.vality.beholder.service.BeholderService;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.actuate.metrics.AutoConfigureMetrics;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.HostPortWaitStrategy;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.utility.DockerImageName;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static dev.vality.beholder.testutil.SystemUtil.isArmArchitecture;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

@Disabled("Used only for local testing")
@AutoConfigureMetrics
@AutoConfigureMockMvc
@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = {"beholder.cron=-", //disables scheduled execution
                "payments.api-url=https://api.test.com",
                "payments.form-url=https://checkout.test.com/checkout.html",
                "payments.request.shop-id=test", "management.server.port="})
public class IntegrationTest {

    public static final String TEST_USER_TOKEN = "test_token";

    public static final String SELENIUM_IMAGE_NAME =
            (isArmArchitecture() ? "seleniarm" : "selenium") + "/standalone-chromium";

    public static final String SELENIUM_IMAGE_TAG = "101.0";

    @Container
    public static final GenericContainer SELENIUM_CONTAINER = new GenericContainer(DockerImageName
            .parse(SELENIUM_IMAGE_NAME)
            .withTag(SELENIUM_IMAGE_TAG))
            .withExposedPorts(4444)
            .waitingFor(new HostPortWaitStrategy());

    @DynamicPropertySource
    static void redisProperties(DynamicPropertyRegistry registry) {
        Supplier<Object> seleniumUrlSupplier = () -> "http://" + SELENIUM_CONTAINER.getHost();
        Supplier<Object> seleniumPortSupplier = () -> SELENIUM_CONTAINER.getMappedPort(4444);
        registry.add("selenium.url", seleniumUrlSupplier);
        registry.add("selenium.port", seleniumPortSupplier);
    }

    @Autowired
    public BeholderService beholderService;

    @MockBean
    private KeycloakService keycloakService;

    @Autowired
    public SeleniumProperties seleniumProperties;

    @Autowired
    private MockMvc mockMvc;

    @BeforeAll
    public static void prepare() {
        SELENIUM_CONTAINER.setPortBindings(List.of("4444:4444"));
        SELENIUM_CONTAINER.start();
    }

    @AfterEach
    public void cleanUp() {
        verifyNoMoreInteractions(keycloakService);
    }

    @AfterAll
    public static void stop() {
        SELENIUM_CONTAINER.stop();
    }

    @Test
    public void test() throws Exception {
        when(keycloakService.getUserToken()).thenReturn(TEST_USER_TOKEN);
        beholderService.behold();
        verify(keycloakService, times(1)).getUserToken();


        var mvcResult = mockMvc.perform(get("/actuator/prometheus"))
                .andReturn();
        String prometheusResponse = mvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8);
        List<String> metrics = Arrays.stream(prometheusResponse.split("\n"))
                .filter(row -> row.startsWith("beholder_")).collect(Collectors.toList());
        Assertions.assertFalse(metrics.isEmpty());


        System.out.println("Collected metrics: ");
        metrics.forEach(System.out::println);
    }


}
