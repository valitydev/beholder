package dev.vality.beholder.util;

import lombok.experimental.UtilityClass;
import org.openqa.selenium.logging.LogType;
import org.openqa.selenium.logging.LoggingPreferences;
import org.openqa.selenium.remote.CapabilityType;
import org.openqa.selenium.remote.DesiredCapabilities;

import java.util.logging.Level;

@UtilityClass
public class SeleniumUtil {

    public static final String PERFORMANCE_SCRIPT = """
            var map = new Map();
            var navigation = window.performance.getEntriesByType("navigation")[0];
            map.set("requestStart", navigation.requestStart);
            map.set("responseStart", navigation.responseStart);
            map.set("responseEnd", navigation.responseEnd);
            map.set("domComplete", navigation.domComplete);
            return map;""";

    public static DesiredCapabilities getCommonCapabilities() {
        var capabilities = new DesiredCapabilities();
        capabilities.setCapability("build", "Simple payment test");
        var logPrefs = new LoggingPreferences();
        logPrefs.enable(LogType.PERFORMANCE, Level.ALL);
        capabilities.setCapability(CapabilityType.LOGGING_PREFS, logPrefs);
        return capabilities;
    }

    public static DesiredCapabilities getLambdaTestCapabilities() {
        var capabilities = getCommonCapabilities();
        capabilities.setCapability("network", false);
        capabilities.setCapability("visual", false);
        capabilities.setCapability("video", false);
        capabilities.setCapability("console", false);
        return capabilities;
    }
}
