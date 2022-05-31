package dev.vality.beholder.service;

import dev.vality.beholder.model.FormDataResponse;
import dev.vality.beholder.util.MetricUtil;
import io.micrometer.core.instrument.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

import static java.util.stream.Collectors.toList;

@Service
@Slf4j
public class MetricsService {

    private final MeterRegistry meterRegistry;
    private final MultiGauge resourcesLoadingTimings;
    private final MultiGauge formDataWaitingDurationGauges;
    private final MultiGauge formDataReceivingDuration;
    private final MultiGauge formDomCompleteDuration;
    private final Map<String, Counter> formLoadingCounters;
    private final Map<String, Counter> formLoadingFailedCounters;


    public MetricsService(MeterRegistry meterRegistry) {

        this.meterRegistry = meterRegistry;

        this.resourcesLoadingTimings = MultiGauge.builder("beholder_form_resource_loading_duration")
                .description("Resources uploading time")
                .baseUnit("millis")
                .register(meterRegistry);

        this.formDataWaitingDurationGauges = MultiGauge.builder("beholder_form_waiting_response_duration")
                .description("Time between sending request and first received byte of data")
                .baseUnit("millis")
                .register(meterRegistry);

        this.formDataReceivingDuration = MultiGauge.builder("beholder_form_receiving_response_duration")
                .description("Time between receiving first and last byte of data")
                .baseUnit("millis")
                .register(meterRegistry);

        this.formDomCompleteDuration = MultiGauge.builder("beholder_form_dom_complete_duration")
                .description("Time between sending request and fully rendered DOM")
                .baseUnit("millis")
                .register(meterRegistry);

        this.formLoadingCounters = new HashMap<>();

        this.formLoadingFailedCounters = new HashMap<>();

    }

    public void updateMetrics(List<FormDataResponse> formDataResponses) {
        log.debug("Updating beholder metrics started");
        updateWaitingResponseDuration(formDataResponses);
        updateFormDataReceivingDuration(formDataResponses);
        updateFormDomCompleteDuration(formDataResponses);
        updateResourceLoadingDuration(formDataResponses);
        updateFormLoadingRequestsTotal(formDataResponses);
        updateFormLoadingFailedRequestsTotal(formDataResponses);
        log.debug("Updating beholder metrics finished");
    }

    private void updateWaitingResponseDuration(List<FormDataResponse> formDataResponses) {
        formDataWaitingDurationGauges.register(
                formDataResponses.stream()
                        .filter(Predicate.not(FormDataResponse::isFailed))
                        .map(formDataResponse -> MultiGauge.Row.of(
                                MetricUtil.createCommonTags(formDataResponse),
                                MetricUtil.calculateWaitingResponseDuration(formDataResponse.getFormPerformance())))
                        .collect(toList()),
                true
        );
    }

    private void updateFormDataReceivingDuration(List<FormDataResponse> formDataResponses) {
        formDataReceivingDuration.register(
                formDataResponses.stream()
                        .filter(Predicate.not(FormDataResponse::isFailed))
                        .map(formDataResponse -> MultiGauge.Row.of(
                                MetricUtil.createCommonTags(formDataResponse),
                                MetricUtil.calculateDataReceivingDuration(formDataResponse.getFormPerformance())))
                        .collect(toList()),
                true
        );
    }

    private void updateFormDomCompleteDuration(List<FormDataResponse> formDataResponses) {
        formDomCompleteDuration.register(
                formDataResponses.stream()
                        .filter(Predicate.not(FormDataResponse::isFailed))
                        .map(formDataResponse -> MultiGauge.Row.of(
                                MetricUtil.createCommonTags(formDataResponse),
                                MetricUtil.calculateDomCompleteDuration(formDataResponse.getFormPerformance())))
                        .collect(toList()),
                true
        );
    }

    private void updateFormLoadingRequestsTotal(List<FormDataResponse> formDataResponses) {
        for (FormDataResponse response : formDataResponses) {
            String id = MetricUtil.getCounterId(response);
            Counter counter = formLoadingCounters.getOrDefault(id,
                    Counter.builder("beholder_form_loading_requests")
                            .description("Total requests for form upload")
                            .tags(MetricUtil.createCommonTags(response))
                            .baseUnit("total")
                            .register(meterRegistry));
            counter.increment();
            formLoadingCounters.put(id, counter);
        }
    }

    private void updateFormLoadingFailedRequestsTotal(List<FormDataResponse> formDataResponses) {
        for (FormDataResponse response : formDataResponses) {
            if (response.isFailed()) {
                String id = MetricUtil.getCounterId(response);
                Counter counter = formLoadingFailedCounters.getOrDefault(id,
                        Counter.builder("beholder_form_loading_failed")
                                .description("Total failed requests for form upload")
                                .tags(MetricUtil.createCommonTags(response))
                                .baseUnit("total")
                                .register(meterRegistry));
                counter.increment();
                formLoadingFailedCounters.put(id, counter);
            }
        }
    }

    private void updateResourceLoadingDuration(List<FormDataResponse> formDataResponses) {
        formDataResponses.stream()
                .filter(Predicate.not(FormDataResponse::isFailed))
                .forEach(
                        formDataResponse ->
                                resourcesLoadingTimings.register(
                                        formDataResponse.getNetworkLogs().stream()
                                                .map(networkLog -> MultiGauge.Row.of(
                                                        MetricUtil.createCommonTags(formDataResponse)
                                                                .and("resource",
                                                                        MetricUtil.getNormalisedPath(networkLog,
                                                                                formDataResponse.getRequest()
                                                                                        .getInvoiceId(),
                                                                                formDataResponse.getRequest()
                                                                                        .getInvoiceAccessToken())),
                                                        MetricUtil.calculateRequestDuration(networkLog)))
                                                .collect(toList()),
                                        true
                                )
                );
    }

}
