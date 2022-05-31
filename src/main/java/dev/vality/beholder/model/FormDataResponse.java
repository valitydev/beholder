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

        private Double requestStartAt;

        private Double responseStartAt;

        private Double responseEndAt;

        private Double domCompletedAt;

    }

}
