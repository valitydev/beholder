package dev.vality.beholder.model;

import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
@Builder
public class FormDataResponse {

    private FormDataRequest request;

    private Map<String, Object> performanceMetrics;

    private Browser browser;

    private Region region;

    private List<NetworkLog> networkLogs;

    private boolean failed;

}
