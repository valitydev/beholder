package dev.vality.beholder.config;

import dev.vality.beholder.config.properties.PaymentsProperties;
import dev.vality.beholder.config.properties.SeleniumProperties;
import dev.vality.beholder.converter.LogEntriesToNetworkLogsConverter;
import dev.vality.beholder.model.Region;
import dev.vality.beholder.service.SeleniumService;
import dev.vality.beholder.util.FileUtil;
import dev.vality.beholder.util.SeleniumUtil;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.stream.Collectors;

@Configuration
public class SeleniumConfig {

    @Value("${dictionary.regions}")
    private Resource dictionaryRegions;

    @Bean(name = "remoteFormService")
    @ConditionalOnProperty(name = "selenium.use-external-provider", havingValue = "true")
    public SeleniumService lambdaTestRemoteFormService(SeleniumProperties seleniumProperties,
                                                       PaymentsProperties paymentsProperties,
                                                       LogEntriesToNetworkLogsConverter converter)
            throws MalformedURLException {
        URL lambdaTestUrl = buildLambdaTestUrl(seleniumProperties.getLambdaTest());
        String formUrl = paymentsProperties.getFormUrl();
        Long formTimeoutSec = paymentsProperties.getFormTimeoutSec();
        DesiredCapabilities desiredCapabilities =
                SeleniumUtil.getLambdaTestCapabilities(seleniumProperties.getLambdaTest());
        return new SeleniumService(lambdaTestUrl, formUrl, formTimeoutSec, desiredCapabilities, converter);
    }

    @Bean(name = "remoteFormService")
    @ConditionalOnProperty(name = "selenium.use-external-provider", havingValue = "false")
    public SeleniumService seleniumRemoteFormService(SeleniumProperties seleniumProperties,
                                                     PaymentsProperties paymentsProperties,
                                                     LogEntriesToNetworkLogsConverter converter)
            throws MalformedURLException {
        URL seleniumUrl = new URL(seleniumProperties.getUrl() + ":" + seleniumProperties.getPort());
        String formUrl = paymentsProperties.getFormUrl();
        Long formTimeoutSec = paymentsProperties.getFormTimeoutSec();
        DesiredCapabilities desiredCapabilities = SeleniumUtil.getCommonCapabilities();
        return new SeleniumService(seleniumUrl, formUrl, formTimeoutSec, desiredCapabilities, converter);
    }

    @Bean
    public List<Region> regions(SeleniumProperties seleniumProperties) throws IOException {
        return FileUtil.readRegions(dictionaryRegions)
                .stream().filter(region -> seleniumProperties.getRegions().contains(region.getCode()))
                .collect(Collectors.toList());
    }

    private URL buildLambdaTestUrl(SeleniumProperties.LambdaTestProperties lambdaTestProperties)
            throws MalformedURLException {
        String url = "https://" + lambdaTestProperties.getUser() + ":" + lambdaTestProperties.getToken() +
                "@hub.lambdatest.com/wd/hub";
        return new URL(url);
    }
}
