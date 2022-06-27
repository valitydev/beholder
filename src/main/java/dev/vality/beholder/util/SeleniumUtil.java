package dev.vality.beholder.util;

import dev.vality.beholder.config.properties.SeleniumProperties;
import lombok.experimental.UtilityClass;
import org.openqa.selenium.logging.LogType;
import org.openqa.selenium.logging.LoggingPreferences;
import org.openqa.selenium.remote.CapabilityType;
import org.openqa.selenium.remote.DesiredCapabilities;

import java.util.logging.Level;

@UtilityClass
public class SeleniumUtil {

    public static final String PERFORMANCE_SCRIPT = """
            var data = new Array();
            var navigation = window.performance.getEntriesByType("navigation")[0];
            data[0] = navigation.requestStart;
            data[1] = navigation.responseStart;
            data[2] = navigation.responseEnd;
            data[3] = navigation.domComplete;
            return data;""";

    public static DesiredCapabilities getCommonCapabilities() {
        var capabilities = new DesiredCapabilities();
        capabilities.setCapability("build", "Simple payment test");
        var logPrefs = new LoggingPreferences();
        logPrefs.enable(LogType.PERFORMANCE, Level.ALL);
        capabilities.setCapability(CapabilityType.LOGGING_PREFS, logPrefs);
        return capabilities;
    }

    public static DesiredCapabilities getLambdaTestCapabilities(SeleniumProperties.LambdaTestProperties lambdaTest) {
        var capabilities = getCommonCapabilities();
        capabilities.setCapability("network", lambdaTest.getNetwork());
        capabilities.setCapability("visual", lambdaTest.getVisual());
        capabilities.setCapability("video", lambdaTest.getVideo());
        capabilities.setCapability("console", lambdaTest.getConsole());
        capabilities.setCapability("user", lambdaTest.getUser());
        capabilities.setCapability("accessKey", lambdaTest.getToken());
        return capabilities;
    }
}
