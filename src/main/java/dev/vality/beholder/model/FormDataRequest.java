package dev.vality.beholder.model;

import lombok.Builder;
import lombok.Data;
import lombok.ToString;

@Data
public class FormDataRequest {

    private String invoiceId;

    private String invoiceAccessToken;

    private Card cardInfo;

    @Data
    @Builder
    public static class Card {
        @ToString.Exclude
        private String pan;
        private String expiration;
        @ToString.Exclude
        private String cvv;
    }

}
