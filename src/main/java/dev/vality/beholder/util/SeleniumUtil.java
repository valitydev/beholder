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
            data[0] = navigation.redirectStart;
            data[1] = navigation.redirectEnd;
            data[2] = navigation.fetchStart;
            data[3] = navigation.domainLookupStart;
            data[4] = navigation.domainLookupEnd;
            data[5] = navigation.connectStart;
            data[6] = navigation.secureConnectionStart;
            data[7] = navigation.connectEnd;
            data[8] = navigation.requestStart;
            data[9] = navigation.responseStart;
            data[10] = navigation.responseEnd;
            data[11] = navigation.domInteractive;
            data[12] = navigation.domContentLoadedEventStart;
            data[13] = navigation.domContentLoadedEventEnd;
            data[14] = navigation.domComplete;
            data[15] = navigation.loadEventStart;
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
        capabilities.setCapability("network", lambdaTest.getEnableNetwork());
        capabilities.setCapability("visual", lambdaTest.getEnableVisual());
        capabilities.setCapability("video", lambdaTest.getEnableVideo());
        capabilities.setCapability("console", lambdaTest.getEnableConsole());
        capabilities.setCapability("user", lambdaTest.getUser());
        capabilities.setCapability("accessKey", lambdaTest.getToken());
        return capabilities;
    }
}
