package dev.vality.beholder.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum Metric {

    RESOURCE_LOADING_DURATION(
            "beholder_form_resource_loading_duration",
            "Resources uploading time",
            MetricUnit.MILLIS.getUnit()
    ),

    PERFORMANCE_TIMING_TEMPLATE(
            "beholder_form_performance_timing_%s",
            "%s timing, received from JS navigation and resource timings",
            MetricUnit.MILLIS.getUnit()
    ),

    FORM_LOADING_REQUESTS(
            "beholder_form_loading_requests",
            "Total requests for form upload",
            MetricUnit.TOTAL.getUnit()
    );


    @Getter
    private final String name;
    @Getter
    private final String description;
    @Getter
    private final String unit;
}
