package dev.vality.beholder.model;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class FormDataResponse {

    private FormDataRequest request;

    private FormPerformance formPerformance;

    private Browser browser;

    private Region region;

    private List<NetworkLog> networkLogs;

    private boolean failed;

    @Data
    @Builder
    public static class FormPerformance {
        private Double redirectStart;
        private Double redirectEnd;
        private Double fetchStart;
        private Double domainLookupStart;
        private Double domainLookupEnd;
        private Double connectStart;
        private Double secureConnectionStart;
        private Double connectEnd;
        private Double requestStart;
        private Double responseStart;
        private Double responseEnd;
        private Double domInteractive;
        private Double domContentLoadedEventStart;
        private Double domContentLoadedEventEnd;
        private Double domComplete;
        private Double loadEventStart;
    }

}
