package dev.vality.beholder.service;

import dev.vality.beholder.model.FormDataResponse;
import dev.vality.beholder.model.Metric;
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

    public MetricsService(MeterRegistry meterRegistry) {

        this.meterRegistry = meterRegistry;

        this.resourcesLoadingTimings = MultiGauge.builder(Metric.RESOURCE_LOADING_DURATION.getName())
                .description(Metric.RESOURCE_LOADING_DURATION.getDescription())
                .baseUnit(Metric.RESOURCE_LOADING_DURATION.getUnit())
                .register(meterRegistry);

        this.formDataWaitingDurationGauges = MultiGauge.builder(Metric.WAITING_RESPONSE_DURATION.getName())
                .description(Metric.WAITING_RESPONSE_DURATION.getDescription())
                .baseUnit(Metric.WAITING_RESPONSE_DURATION.getUnit())
                .register(meterRegistry);

        this.formDataReceivingDuration = MultiGauge.builder(Metric.RECEIVING_RESPONSE_DURATION.getName())
                .description(Metric.RECEIVING_RESPONSE_DURATION.getDescription())
                .baseUnit(Metric.RECEIVING_RESPONSE_DURATION.getUnit())
                .register(meterRegistry);

        this.formDomCompleteDuration = MultiGauge.builder(Metric.DOM_COMPLETE_DURATION.getName())
                .description(Metric.DOM_COMPLETE_DURATION.getDescription())
                .baseUnit(Metric.DOM_COMPLETE_DURATION.getUnit())
                .register(meterRegistry);

        this.formLoadingCounters = new HashMap<>();

    }

    public void updateMetrics(List<FormDataResponse> formDataResponses) {
        log.debug("Updating beholder metrics started");
        updateWaitingResponseDuration(formDataResponses);
        updateFormDataReceivingDuration(formDataResponses);
        updateFormDomCompleteDuration(formDataResponses);
        updateResourceLoadingDuration(formDataResponses);
        updateFormLoadingRequestsTotal(formDataResponses);
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
            Tags tags = MetricUtil.createCommonTags(response)
                    .and("result", response.isFailed() ? "failure" : "success");
            String id = MetricUtil.getCounterId(response);
            Counter counter = formLoadingCounters.getOrDefault(id,
                    Counter.builder(Metric.FORM_LOADING_REQUESTS.getName())
                            .description(Metric.FORM_LOADING_REQUESTS.getDescription())
                            .tags(tags)
                            .baseUnit(Metric.FORM_LOADING_REQUESTS.getUnit())
                            .register(meterRegistry));
            counter.increment();
            formLoadingCounters.put(id, counter);
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
