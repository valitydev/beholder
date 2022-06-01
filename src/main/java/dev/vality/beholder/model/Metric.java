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

    WAITING_RESPONSE_DURATION(
            "beholder_form_waiting_response_duration",
            "Time between sending request and first received byte of data",
            MetricUnit.MILLIS.getUnit()
    ),

    RECEIVING_RESPONSE_DURATION(
            "beholder_form_receiving_response_duration",
            "Time between receiving first and last byte of data",
            MetricUnit.MILLIS.getUnit()
    ),

    DOM_COMPLETE_DURATION(
            "beholder_form_dom_complete_duration",
            "Time between sending request and fully rendered DOM",
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
