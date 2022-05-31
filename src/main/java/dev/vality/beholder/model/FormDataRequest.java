package dev.vality.beholder.model;

import lombok.Builder;
import lombok.Data;

@Data
public class FormDataRequest {

    private String invoiceId;

    private String invoiceAccessToken;

    private Card cardInfo;

    @Data
    @Builder
    public static class Card {
        private String pan;
        private String expiration;
        private String cvv;
    }

}
