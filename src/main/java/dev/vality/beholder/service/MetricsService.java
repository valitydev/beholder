package dev.vality.beholder.service;

import dev.vality.beholder.model.FormDataResponse;
import dev.vality.beholder.model.Metric;
import dev.vality.beholder.model.NetworkLog;
import dev.vality.beholder.util.MetricUtil;
import dev.vality.beholder.util.StringUtil;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.MultiGauge;
import io.micrometer.core.instrument.Tags;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static dev.vality.beholder.model.Metric.PERFORMANCE_TIMING_TEMPLATE;
import static java.util.stream.Collectors.toList;

@Service
@Slf4j
public class MetricsService {

    private final MeterRegistry meterRegistry;
    private final MultiGauge resourcesLoadingTimings;
    private final Map<String, MultiGauge> performanceTimingGauges;

    public MetricsService(MeterRegistry meterRegistry) {

        this.meterRegistry = meterRegistry;

        this.resourcesLoadingTimings = MultiGauge.builder(Metric.RESOURCE_LOADING_DURATION.getName())
                .description(Metric.RESOURCE_LOADING_DURATION.getDescription())
                .baseUnit(Metric.RESOURCE_LOADING_DURATION.getUnit())
                .register(meterRegistry);

        this.performanceTimingGauges = createPerformanceTimingGauges(meterRegistry);
    }

    public void updateMetrics(List<FormDataResponse> formDataResponses) {
        log.debug("Updating beholder metrics started");
        updatePerformanceTimings(formDataResponses);
        updateResourceLoadingDuration(formDataResponses);
        updateFormLoadingRequestsTotal(formDataResponses);
        log.debug("Updating beholder metrics finished");
    }

    private Map<String, MultiGauge> createPerformanceTimingGauges(MeterRegistry registry) {
        Map<String, MultiGauge> gauges = new HashMap<>();
        for (String jsMetricName : MetricUtil.PERFORMANCE_METRICS) {
            String prometheusMetricName =
                    String.format(PERFORMANCE_TIMING_TEMPLATE.getName(), StringUtil.camelToSnake(jsMetricName));
            String prometheusMetricDescription =
                    String.format(PERFORMANCE_TIMING_TEMPLATE.getDescription(), jsMetricName);

            MultiGauge multiGauge = MultiGauge.builder(prometheusMetricName)
                    .description(prometheusMetricDescription)
                    .baseUnit(PERFORMANCE_TIMING_TEMPLATE.getUnit())
                    .register(registry);
            gauges.put(jsMetricName, multiGauge);
        }
        return gauges;
    }

    private void updatePerformanceTimings(List<FormDataResponse> formDataResponses) {
        MetricUtil.PERFORMANCE_METRICS.forEach(metricName -> {
            Map<Tags, Double> perfTimingsStats = convertToPerformanceTimingsStats(metricName, formDataResponses);
            performanceTimingGauges.get(metricName)
                    .register(
                            perfTimingsStats.entrySet().stream()
                                    .map(networkLogEntry -> MultiGauge.Row.of(networkLogEntry.getKey(),
                                            networkLogEntry.getValue()))
                                    .collect(toList()), true);
        });
    }

    private Map<Tags, Double> convertToPerformanceTimingsStats(String metricName,
                                                               List<FormDataResponse> formDataResponses) {
        return formDataResponses.stream()
                .filter(Predicate.not(FormDataResponse::isFailed))
                .map(formDataResponse -> convertToPerformanceTimingEntry(metricName, formDataResponse))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    private Map.Entry<Tags, Double> convertToPerformanceTimingEntry(String metricName,
                                                                    FormDataResponse formDataResponse) {
        Double value = MetricUtil.castToDouble(
                formDataResponse.getPerformanceMetrics().getOrDefault(metricName, Double.NaN));
        Tags tags = MetricUtil.createCommonTags(formDataResponse);
        return Map.entry(tags, value);
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
                resourcesStats.entrySet().stream()
                        .map(networkLogEntry -> MultiGauge.Row.of(
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
                                .map(log -> convertToResourceLoadingEntry(formDataResponse, log)))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    private Map.Entry<Tags, Double> convertToResourceLoadingEntry(FormDataResponse formDataResponse,
                                                                  NetworkLog networkLog) {
        return Map.entry(MetricUtil.createCommonTags(formDataResponse)
                .and("resource",
                        MetricUtil.getNormalisedPath(networkLog,
                                formDataResponse.getRequest()
                                        .getInvoiceId(),
                                formDataResponse.getRequest()
                                        .getInvoiceAccessToken()))
                .and("method", networkLog.getMethod()), MetricUtil.calculateRequestDuration(networkLog));
    }

}
