package dev.vality.beholder.service;

import dev.vality.beholder.model.FormDataResponse;
import dev.vality.beholder.model.Metric;
import dev.vality.beholder.util.MetricUtil;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.MultiGauge;
import io.micrometer.core.instrument.Tags;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toList;

@Service
@Slf4j
public class MetricsService {

    private final MeterRegistry meterRegistry;
    private final MultiGauge resourcesLoadingTimings;
    private final MultiGauge formDataPerformanceTimings;

    public MetricsService(MeterRegistry meterRegistry) {

        this.meterRegistry = meterRegistry;

        this.resourcesLoadingTimings = MultiGauge.builder(Metric.RESOURCE_LOADING_DURATION.getName())
                .description(Metric.RESOURCE_LOADING_DURATION.getDescription())
                .baseUnit(Metric.RESOURCE_LOADING_DURATION.getUnit())
                .register(meterRegistry);

        this.formDataPerformanceTimings = MultiGauge.builder(Metric.PERFORMANCE_TIMINGS.getName())
                .description(Metric.PERFORMANCE_TIMINGS.getDescription())
                .baseUnit(Metric.PERFORMANCE_TIMINGS.getUnit())
                .register(meterRegistry);

    }

    public void updateMetrics(List<FormDataResponse> formDataResponses) {
        log.debug("Updating beholder metrics started");
        updatePerformanceTimings(formDataResponses);
        updateResourceLoadingDuration(formDataResponses);
        updateFormLoadingRequestsTotal(formDataResponses);
        log.debug("Updating beholder metrics finished");
    }

    private void updatePerformanceTimings(List<FormDataResponse> formDataResponses) {
        Map<Tags, Double> perfTimingsStats = convertToPerformanceTimingsStats(formDataResponses);
        formDataPerformanceTimings.register(
                perfTimingsStats.entrySet().stream().map(networkLogEntry -> MultiGauge.Row.of(
                                networkLogEntry.getKey(),
                                networkLogEntry.getValue()))
                        .collect(toList()),
                true
        );
    }

    private Map<Tags, Double> convertToPerformanceTimingsStats(List<FormDataResponse> formDataResponses) {
        return formDataResponses.stream()
                .filter(Predicate.not(FormDataResponse::isFailed))
                .flatMap(formDataResponse ->
                        Arrays.stream(formDataResponse.getFormPerformance().getClass().getDeclaredFields())
                                .map(field -> {
                                    field.setAccessible(true);
                                    try {
                                        String name = field.getName();
                                        Double value = (Double) field.get(formDataResponse.getFormPerformance());
                                        return Map.entry(MetricUtil.createCommonTags(formDataResponse)
                                                .and("performanceTiming", name), value);
                                    } catch (IllegalAccessException e) {
                                        throw new RuntimeException(e);
                                    }
                                }))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    private void updateFormLoadingRequestsTotal(List<FormDataResponse> formDataResponses) {
        for (FormDataResponse response : formDataResponses) {
            Tags tags = MetricUtil.createCommonTags(response);
            incrementFormLoadingCounter(tags.and("result", response.isFailed() ? "failure" : "success"));
        }
    }

    private void incrementFormLoadingCounter(Tags tags) {
        Counter.builder(Metric.FORM_LOADING_REQUESTS.getName())
                .description(Metric.FORM_LOADING_REQUESTS.getDescription())
                .tags(tags)
                .baseUnit(Metric.FORM_LOADING_REQUESTS.getUnit())
                .register(meterRegistry)
                .increment();
    }

    private void updateResourceLoadingDuration(List<FormDataResponse> formDataResponses) {
        Map<Tags, Double> resourcesStats = convertToResourceLoadingStats(formDataResponses);
        resourcesLoadingTimings.register(
                resourcesStats.entrySet().stream().map(networkLogEntry -> MultiGauge.Row.of(
                                networkLogEntry.getKey(),
                                networkLogEntry.getValue()))
                        .collect(toList()),
                true
        );
    }

    private Map<Tags, Double> convertToResourceLoadingStats(List<FormDataResponse> formDataResponses) {
        return formDataResponses.stream()
                .filter(Predicate.not(FormDataResponse::isFailed))
                .flatMap(formDataResponse ->
                        formDataResponse.getNetworkLogs().stream()
                                .map(networkLog -> Map.entry(MetricUtil.createCommonTags(formDataResponse)
                                                .and("resource",
                                                        MetricUtil.getNormalisedPath(networkLog,
                                                                formDataResponse.getRequest()
                                                                        .getInvoiceId(),
                                                                formDataResponse.getRequest()
                                                                        .getInvoiceAccessToken()))
                                                .and("method", networkLog.getMethod()),
                                        MetricUtil.calculateRequestDuration(networkLog))))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

}
