package dev.vality.beholder.service;

import dev.vality.beholder.model.FormDataRequest;
import dev.vality.beholder.model.FormDataResponse;
import dev.vality.beholder.model.Region;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class BeholderService {

    private final PaymentsService paymentsService;
    private final SeleniumService seleniumService;
    private final MetricsService metricsService;

    private final List<Region> regions;

    @Scheduled(cron = "${schedule.cron:-}")
    public void behold() {
        log.info("Start sending requests from {} regions", regions);
        List<FormDataResponse> responses = new ArrayList<>();
        for (Region region : regions) {
            FormDataRequest request;
            try {
                log.debug("Preparing request for {} region", region.getCountry());
                request = paymentsService.prepareFormData();
                log.debug("Request for {} region successfully prepared", region.getCountry());
            } catch (Exception e) {
                log.error("Unable to prepare request for {}:", region.getCountry(), e);
                continue;
            }
            FormDataResponse response = seleniumService.executePaymentRequest(request, region);
            responses.add(response);
            log.debug("Metrics for {} region saved", region.getCountry());
        }
        metricsService.updateMetrics(responses);
        log.info("Finished processing requests from {} regions", regions);

    }
}
