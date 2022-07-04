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

    PERFORMANCE_TIMINGS(
            "beholder_form_performance_timing_duration",
            "Navigation and resource timings received via Javascript",
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
