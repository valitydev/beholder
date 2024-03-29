package dev.vality.beholder.service;

import dev.vality.beholder.converter.LogEntriesToNetworkLogsConverter;
import dev.vality.beholder.model.*;
import dev.vality.beholder.util.SeleniumUtil;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.logging.LogEntries;
import org.openqa.selenium.logging.LogType;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URL;
import java.util.List;
import java.util.Map;

@Slf4j
@RequiredArgsConstructor
public class SeleniumService {

    private final URL seleniumUrl;
    private final String formUrl;
    private final Long formTimeoutSec;
    private final DesiredCapabilities desiredCapabilities;
    private final LogEntriesToNetworkLogsConverter logEntriesToNetworkLogsConverter;

    @Getter
    private final Browser browser = Browser.CHROME;

    public FormDataResponse executePaymentRequest(FormDataRequest formDataRequest, Region region) {
        DesiredCapabilities capabilities = new DesiredCapabilities(desiredCapabilities);
        updateCapabilities(capabilities, region);

        RemoteWebDriver driver = null;
        try {
            driver = new RemoteWebDriver(seleniumUrl, capabilities);
            driver.get(prepareParams(formDataRequest));

            Map<String, Object> performanceMetrics =
                    (Map<String, Object>) driver.executeScript(SeleniumUtil.PERFORMANCE_SCRIPT);
            fillAndSendPaymentRequest(driver, formDataRequest.getCardInfo());

            LogEntries les = driver.manage().logs().get(LogType.PERFORMANCE);
            List<NetworkLog> networkLogs = logEntriesToNetworkLogsConverter.convert(les.getAll());

            return FormDataResponse.builder()
                    .networkLogs(networkLogs)
                    .request(formDataRequest)
                    .performanceMetrics(performanceMetrics)
                    .region(region)
                    .browser(browser)
                    .build();
        } catch (Exception e) {
            log.error("Error during sending request from {}:", region.getCountry(), e);
        } finally {
            if (driver != null) {
                driver.quit();
            }
        }
        return FormDataResponse.builder()
                .region(region)
                .browser(browser)
                .failed(true)
                .build();
    }

    private void updateCapabilities(DesiredCapabilities capabilities, Region region) {
        capabilities.setCapability("browser", browser.getLabel());
        capabilities.setCapability("geoLocation", region.getCode());
        capabilities.setCapability("name", String.format("Payment from %s", region.getCountry()));
    }

    private void fillAndSendPaymentRequest(RemoteWebDriver driver, FormDataRequest.Card card) {
        WebElement cardNumInput = new WebDriverWait(driver, formTimeoutSec).until(
                ExpectedConditions.visibilityOfElementLocated(By.ById.id("card-number-input")));
        cardNumInput.sendKeys(card.getPan());
        driver.findElementById("expire-date-input").sendKeys(card.getExpiration());
        driver.findElementById("secure-code-input").sendKeys(card.getCvv());
        driver.findElementById("card-holder-input").sendKeys("Ivan Ivanov");
        driver.findElementById("pay-btn").click();
        new WebDriverWait(driver, formTimeoutSec).until(
                ExpectedConditions.visibilityOfElementLocated(By.ById.id("success-icon")));
    }

    private String prepareParams(FormDataRequest formDataRequest) {
        return UriComponentsBuilder.fromHttpUrl(formUrl)
                .queryParam("invoiceAccessToken", formDataRequest.getInvoiceAccessToken())
                .queryParam("invoiceID", formDataRequest.getInvoiceId())
                .build()
                .toString();
    }
}
