package dev.vality.beholder.service;

import dev.vality.beholder.config.properties.PaymentsProperties;
import dev.vality.beholder.model.FormDataRequest;
import dev.vality.beholder.security.KeycloakService;
import dev.vality.beholder.util.PaymentsUtil;
import dev.vality.swag.payments.ApiClient;
import dev.vality.swag.payments.api.ClaimsApi;
import dev.vality.swag.payments.api.InvoicesApi;
import dev.vality.swag.payments.api.PartiesApi;
import dev.vality.swag.payments.api.ShopsApi;
import dev.vality.swag.payments.model.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpStatusCodeException;

@Service
@RequiredArgsConstructor
public class PaymentsService {

    private final ApiClient apiClient;
    private final PartiesApi partiesApi;
    private final ShopsApi shopsApi;
    private final InvoicesApi invoicesApi;
    private final ClaimsApi claimsApi;
    private final KeycloakService keycloakService;
    private final PaymentsProperties paymentsProperties;

    public FormDataRequest prepareFormData() {
        apiClient.setApiKey(keycloakService.getUserToken());
        String shopId = getShopId();
        InvoiceParams invoiceParams = PaymentsUtil.createInvoiceParams(shopId);
        InvoiceAndToken invoiceAndToken = invoicesApi.createInvoice(PaymentsUtil.getRequestId(), invoiceParams,
                PaymentsUtil.getRequestDeadline(paymentsProperties.getApiTimeoutSec()));
        return createFormDataRequest(invoiceAndToken);
    }

    private String getShopId() {
        Party party = partiesApi.getMyParty(PaymentsUtil.getRequestId(),
                PaymentsUtil.getRequestDeadline(paymentsProperties.getApiTimeoutSec()));
        var request = paymentsProperties.getRequest();
        String shopId = request.getShopId();
        try {
            shopsApi.getShopByIDForParty(PaymentsUtil.getRequestId(), shopId, party.getId(),
                    PaymentsUtil.getRequestDeadline(paymentsProperties.getApiTimeoutSec()));
        } catch (HttpStatusCodeException httpStatusCodeException) {
            if (!isNotFoundError(httpStatusCodeException) || !request.getCreateShopIfNotFound()) {
                throw httpStatusCodeException;
            }
            sendShopCreationClaim(request, shopId);
        }
        return shopId;
    }

    private void sendShopCreationClaim(PaymentsProperties.Request request, String shopId) {
        ClaimChangeset changeset = PaymentsUtil.buildCreateShopClaim(request.getPaymentInstitutionId(),
                shopId, request.getCategoryId());
        claimsApi.createClaim(PaymentsUtil.getRequestId(), changeset,
                PaymentsUtil.getRequestDeadline(paymentsProperties.getApiTimeoutSec()));
    }

    private FormDataRequest createFormDataRequest(InvoiceAndToken invoiceAndToken) {
        FormDataRequest formDataRequest = new FormDataRequest();
        formDataRequest.setCardInfo(FormDataRequest.Card.builder()
                .pan(PaymentsUtil.TEST_CARD_PAN)
                .cvv(PaymentsUtil.TEST_CARD_CVV)
                .expiration(PaymentsUtil.TEST_CARD_EXPIRATION).build());
        formDataRequest.setInvoiceId(invoiceAndToken.getInvoice().getId());
        formDataRequest.setInvoiceAccessToken(invoiceAndToken.getInvoiceAccessToken().getPayload());
        return formDataRequest;
    }

    private boolean isNotFoundError(HttpStatusCodeException httpStatusCodeException) {
        return HttpStatus.NOT_FOUND.equals(httpStatusCodeException.getStatusCode());
    }
}
