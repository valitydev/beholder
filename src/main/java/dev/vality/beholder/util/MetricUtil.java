package dev.vality.beholder.util;

import dev.vality.beholder.model.FormDataResponse;
import dev.vality.beholder.model.NetworkLog;
import io.micrometer.core.instrument.Tags;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@UtilityClass
public class MetricUtil {

    public static String getNormalisedPath(NetworkLog networkLog, String invoiceId, String invoiceToken) {
        String path = networkLog.getResource();
        path = path.replaceAll(invoiceId, "{invoice_id}")
                .replaceAll(invoiceToken, "{invoice_token}");
        if (path.contains("nocache=")) {
            path = path.substring(0, path.length() - 13);
        }
        return path;
    }

    public static double calculateRequestDuration(NetworkLog networkLog) {
        return calculateDiff(networkLog.getStart(), networkLog.getEnd());
    }

    public static double calculateWaitingResponseDuration(FormDataResponse.FormPerformance performance) {
        return calculateDiff(performance.getRequestStartAt(), performance.getResponseStartAt());
    }

    public static double calculateDataReceivingDuration(FormDataResponse.FormPerformance performance) {
        return calculateDiff(performance.getResponseStartAt(), performance.getResponseEndAt());
    }

    public static double calculateDomCompleteDuration(FormDataResponse.FormPerformance performance) {
        return calculateDiff(performance.getRequestStartAt(), performance.getDomCompletedAt());
    }

    private static double calculateDiff(Double from, Double to) {
        if (isNumber(from) && isNumber(to)) {
            return Math.round(to - from);
        }
        return 0.0;
    }

    private static boolean isNumber(Double value) {
        return value != null && !value.isNaN();
    }

    public Tags createCommonTags(FormDataResponse formDataResponse) {
        return Tags.of("browser", formDataResponse.getBrowser().getLabel(),
                "region", formDataResponse.getRegion().getCode());
    }

    public String getCounterId(FormDataResponse formDataResponse) {
        return String.join("_", formDataResponse.getBrowser().name(), formDataResponse.getRegion().getCountry());
    }
}