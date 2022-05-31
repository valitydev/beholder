package dev.vality.beholder.testutil;

import dev.vality.beholder.model.Browser;
import dev.vality.beholder.model.FormDataRequest;
import dev.vality.beholder.model.FormDataResponse;
import dev.vality.beholder.model.Region;
import dev.vality.swag.payments.model.*;
import lombok.experimental.UtilityClass;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;

@UtilityClass
public class ResponseUtil {

    public static Party getMyPartyResponse() {
        Party party = new Party();
        party.setId(UUID.randomUUID().toString());
        party.setIsBlocked(false);
        party.setIsBlocked(false);
        return party;
    }

    public static Shop getShopResponse(String id) {
        Shop shop = new Shop();
        shop.setId(id);
        shop.setCurrency("RUB");
        shop.setIsBlocked(false);
        shop.setIsSuspended(false);
        return shop;
    }

    public static Claim getClaim() {
        Claim claim = new Claim();
        claim.setId(0L);
        claim.setStatus("success");
        return claim;
    }

    public static InvoiceAndToken getInvoiceAndToken() {
        InvoiceAndToken invoiceAndToken = new InvoiceAndToken();
        invoiceAndToken.setInvoice(new Invoice().id("2"));
        invoiceAndToken.setInvoiceAccessToken(new AccessToken().payload("invoice_test_token"));
        return invoiceAndToken;
    }

    public static FormDataResponse getFormDataResponse() {
        var region = new Region();
        region.setCode("AM");
        region.setCountry("Armenia");

        var invoiceAndToken = getInvoiceAndToken();

        FormDataRequest request = new FormDataRequest();
        request.setInvoiceAccessToken(invoiceAndToken.getInvoiceAccessToken().getPayload());
        request.setInvoiceId(invoiceAndToken.getInvoice().getId());

        var requestStartAt = (double) Instant.now().minus(10, ChronoUnit.SECONDS).toEpochMilli();
        var responseStartAt = requestStartAt + 5;
        var responseEndAt = responseStartAt + 5;

        FormDataResponse.FormPerformance performance = FormDataResponse.FormPerformance.builder()
                .requestStartAt(requestStartAt)
                .responseStartAt(responseStartAt)
                .responseEndAt(responseEndAt).build();

        return FormDataResponse.builder()
                .region(region)
                .browser(Browser.CHROME)
                .request(request)
                .networkLogs(List.of())
                .formPerformance(performance)
                .build();
    }

}
