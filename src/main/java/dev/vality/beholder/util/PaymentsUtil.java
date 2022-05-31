package dev.vality.beholder.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.vality.swag.payments.model.*;
import lombok.experimental.UtilityClass;

import java.time.OffsetDateTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

@UtilityClass
public class PaymentsUtil {

    public static final String TEST_CARD_PAN = "4242424242424242";
    public static final String TEST_CARD_EXPIRATION = "12/24";
    public static final String TEST_CARD_CVV = "123";

    private static final ObjectMapper MAPPER = new ObjectMapper();
    public static final String CURRENCY_CODE = "RUB";

    public static PartyModification createShopModification(String shopId, String contractId, String payoutToolId) {
        return new ShopCreation()
                .details(
                        new ShopDetails()
                                .name("OOOBlackMaster")
                                .description("Goods for education"))
                .location(
                        new ShopLocationUrl()
                                .url("http://all-time-favourite-spinners.com/"))
                .contractID(contractId)
                .payoutToolID(payoutToolId)
                .shopID(shopId)
                .shopModificationType(ShopModification.ShopModificationTypeEnum.SHOPCREATION)
                .partyModificationType(PartyModification.PartyModificationTypeEnum.SHOPMODIFICATION);
    }

    public static PartyModification createShopAccountCreationModification(String shopId) {
        return new ShopAccountCreation()
                .currency(CURRENCY_CODE)
                .shopID(shopId)
                .shopModificationType(ShopModification.ShopModificationTypeEnum.SHOPACCOUNTCREATION)
                .partyModificationType(PartyModification.PartyModificationTypeEnum.SHOPMODIFICATION);
    }

    public static PartyModification createShopCategoryChangeModification(String shopId, Integer categoryId) {
        return new ShopCategoryChange()
                .categoryID(categoryId)
                .shopID(shopId)
                .shopModificationType(ShopModification.ShopModificationTypeEnum.SHOPCATEGORYCHANGE)
                .partyModificationType(PartyModification.PartyModificationTypeEnum.SHOPMODIFICATION);
    }

    public static ClaimChangeset buildCreateShopClaim(Integer paymentInstitutionId, String shopId, Integer categoryId) {
        PartyModification contractCreation = createContractModification(shopId, paymentInstitutionId);
        PartyModification contractModification = createPayoutToolModification(shopId);
        PartyModification shopModification = createShopModification(shopId, shopId, shopId);
        PartyModification categoryChange = createShopCategoryChangeModification(shopId, categoryId);
        PartyModification accountChange = createShopAccountCreationModification(shopId);

        ClaimChangeset changeset = new ClaimChangeset();
        changeset.add(contractCreation);
        changeset.add(contractModification);
        changeset.add(shopModification);
        changeset.add(categoryChange);
        changeset.add(accountChange);
        return changeset;
    }

    public static PartyModification createContractModification(String contractId, Integer paymentInstitutionId) {
        return new ContractCreation()
                .contractor(createContractor())
                .paymentInstitutionID(paymentInstitutionId)
                .contractID(contractId)
                .contractModificationType(ContractModification.ContractModificationTypeEnum.CONTRACTCREATION)
                .partyModificationType(PartyModification.PartyModificationTypeEnum.CONTRACTMODIFICATION);
    }

    public static Contractor createContractor() {
        return new RussianLegalEntity()
                .registeredName("testRegisteredName")
                .registeredNumber("1234567890123")
                .inn("1234567890")
                .actualAddress("testActualAddress")
                .postAddress("testPostAddress")
                .representativePosition("testRepresentativePosition")
                .representativeFullName("testRepresentativeFullName")
                .representativeDocument("testRepresentativeDocument")
                .bankAccount(createBankAccount())
                .entityType(LegalEntity.EntityTypeEnum.RUSSIANLEGALENTITY)
                .contractorType(Contractor.ContractorTypeEnum.LEGALENTITY);
    }

    public static PartyModification createPayoutToolModification(String contractId) {
        return new ContractPayoutToolCreation()
                .payoutToolID(contractId)
                .currency(CURRENCY_CODE)
                .details(new PayoutToolDetailsBankAccount()
                        .account("12345678901234567890")
                        .bankName("testBankName")
                        .bankPostAccount("12345678901234567890")
                        .bankBik("123456789")
                )
                .contractID(contractId)
                .contractModificationType(ContractModification.ContractModificationTypeEnum.CONTRACTPAYOUTTOOLCREATION)
                .partyModificationType(PartyModification.PartyModificationTypeEnum.CONTRACTMODIFICATION);
    }

    public static BankAccount createBankAccount() {
        return new BankAccount()
                .account("12345678901234567890")
                .bankName("testBankName")
                .bankPostAccount("12345678901234567890")
                .bankBik("123456789");
    }

    public static InvoiceParams createInvoiceParams(String shopId) {
        return new InvoiceParams()
                .shopID(shopId)
                .dueDate(OffsetDateTime.now().plusDays(1))
                .currency(CURRENCY_CODE)
                .product("Order num 12345")
                .amount(1000L)
                .metadata(MAPPER.createObjectNode());
    }

    public static String getRequestId() {
        return RandomUtil.generateString(8);
    }

    public static String getRequestDeadline(long seconds) {
        return ZonedDateTime.now()
                .plusSeconds(seconds)
                .format(DateTimeFormatter.ISO_INSTANT);
    }

}
