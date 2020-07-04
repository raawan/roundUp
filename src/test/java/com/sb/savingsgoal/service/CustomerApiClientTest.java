package com.sb.savingsgoal.service;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.equalToJson;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.okJson;
import static com.github.tomakehurst.wiremock.client.WireMock.put;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathMatching;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

import com.sb.savingsgoal.model.Accounts;
import com.sb.savingsgoal.model.Amount;
import com.sb.savingsgoal.model.Balance;
import com.sb.savingsgoal.model.SavingsGoals;
import com.sb.savingsgoal.model.Transaction;
import com.sb.savingsgoal.model.TransactionFeed;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;

@SpringBootTest(properties = "app.baseUrl=http://localhost:6065", webEnvironment = SpringBootTest.WebEnvironment.NONE)
@AutoConfigureWireMock(port = 6065)
public class CustomerApiClientTest {

    public static final String ACCEPT = "Accept";
    public static final String CONTENT_TYPE = "Content-Type";
    public static final String APPLICATION_JSON = "application/json";
    public static final String ROOT_CONTEXT = "http://localhost:6065";
    private final static String token = "aabb";
    public static final int STATUS_CODE_FOR_CLIENT_FAULT = 404;
    public static final int STATUS_CODE_FOR_SERVER_FAULT = 500;

    @Autowired
    private CustomerApiClient customerApiClient;

    @BeforeEach
    public void setUp() {
        customerApiClient.setRootContext(ROOT_CONTEXT);
        customerApiClient.setToken(token);
    }

    @Test
    public void shouldGetAllAccounts() {

        //given
        stubFor(get(urlPathEqualTo("/accounts"))
                .withHeader(ACCEPT, equalTo(APPLICATION_JSON))
                .withHeader(CONTENT_TYPE, equalTo(APPLICATION_JSON))
                .willReturn(okJson("{\n" +
                        "    \"accounts\": [\n" +
                        "        {\n" +
                        "            \"accountUid\": \"8833f0cf-6373-4c4a-b926-33081a16b1c7\",\n" +
                        "            \"defaultCategory\": \"b26f9685-0c90-4eaf-b6ea-a492679b7ea1\",\n" +
                        "            \"currency\": \"GBP\",\n" +
                        "            \"createdAt\": \"2020-06-28T14:58:32.562Z\"\n" +
                        "        }\n" +
                        "    ]\n" +
                        "}"))
        );

        //when
        final Accounts allAccounts = customerApiClient.getAllAccounts();

        //then
        assertEquals(1, allAccounts.getAccounts().size());
        assertEquals("8833f0cf-6373-4c4a-b926-33081a16b1c7", allAccounts.getAccounts().get(0).getAccountUid().toString());
    }

    @Test
    public void shouldGetTransactionFeed() {

        //given
        String url = "/feed/account/8833f0cf-6373-4c4a-b926-33081a16b1c7/category/b26f9685-0c90-4eaf-b6ea-a492679b7ea1?changesSince=2020-06-27T00:00:00Z";
        stubFor(get(urlEqualTo(url))
                .withHeader(ACCEPT, equalTo(APPLICATION_JSON))
                .withHeader(CONTENT_TYPE, equalTo(APPLICATION_JSON))
                .willReturn(okJson("{\n" +
                        "    \"feedItems\": [\n" +
                        "        {\n" +
                        "            \"feedItemUid\": \"3623a441-833b-4f6a-be9e-29f9d8be3e37\",\n" +
                        "            \"categoryUid\": \"b26f9685-0c90-4eaf-b6ea-a492679b7ea1\",\n" +
                        "            \"amount\": {\n" +
                        "                \"currency\": \"GBP\",\n" +
                        "                \"minorUnits\": 3871\n" +
                        "            },\n" +
                        "            \"direction\": \"OUT\",\n" +
                        "            \"updatedAt\": \"2020-06-28T14:59:38.648Z\",\n" +
                        "            \"transactionTime\": \"2020-06-28T14:59:38.299Z\"  \n" +
                        "        },\n" +
                        "        {\n" +
                        "            \"feedItemUid\": \"3623a193-8f93-4d3f-9f4d-7bc666e0f811\",\n" +
                        "            \"categoryUid\": \"b26f9685-0c90-4eaf-b6ea-a492679b7ea1\",\n" +
                        "            \"amount\": {\n" +
                        "                \"currency\": \"GBP\",\n" +
                        "                \"minorUnits\": 2556\n" +
                        "            },\n" +
                        "            \"direction\": \"OUT\",\n" +
                        "            \"updatedAt\": \"2020-06-28T14:59:38.545Z\",\n" +
                        "            \"transactionTime\": \"2020-06-28T14:59:38.171Z\"\n" +
                        "        }\n" +
                        "    ]\n" +
                        "}"))
        );

        final String dateSince = getDateInIsoFormat(2020, 06, 27);
        String accountUid = "8833f0cf-6373-4c4a-b926-33081a16b1c7";
        String categoryUid = "b26f9685-0c90-4eaf-b6ea-a492679b7ea1";

        //when
        final TransactionFeed allTransactionsSince =
                customerApiClient.getAllTransactionsSince(dateSince, UUID.fromString(accountUid), UUID.fromString(categoryUid));

        //then
        assertEquals(2, allTransactionsSince.getFeedItems().size());
        Transaction transaction = getTransactionWithFeedId(allTransactionsSince, "3623a441-833b-4f6a-be9e-29f9d8be3e37");
        assertEquals("3623a441-833b-4f6a-be9e-29f9d8be3e37", transaction.getFeedItemUid().toString());
        transaction = getTransactionWithFeedId(allTransactionsSince, "3623a193-8f93-4d3f-9f4d-7bc666e0f811");
        assertEquals("3623a193-8f93-4d3f-9f4d-7bc666e0f811", transaction.getFeedItemUid().toString());
    }

    @Test
    public void shouldGetAccountBalance() {

        //given
        stubFor(get(urlPathEqualTo("/accounts/8833f0cf-6373-4c4a-b926-33081a16b1c7/balance"))
                .withHeader(ACCEPT, equalTo(APPLICATION_JSON))
                .withHeader(CONTENT_TYPE, equalTo(APPLICATION_JSON))
                .willReturn(okJson("{\n" +
                        "    \"clearedBalance\": {\n" +
                        "        \"currency\": \"GBP\",\n" +
                        "        \"minorUnits\": 24318\n" +
                        "    },\n" +
                        "    \"effectiveBalance\": {\n" +
                        "        \"currency\": \"GBP\",\n" +
                        "        \"minorUnits\": 24318\n" +
                        "    },\n" +
                        "    \"pendingTransactions\": {\n" +
                        "        \"currency\": \"GBP\",\n" +
                        "        \"minorUnits\": 0\n" +
                        "    },\n" +
                        "    \"acceptedOverdraft\": {\n" +
                        "        \"currency\": \"GBP\",\n" +
                        "        \"minorUnits\": 0\n" +
                        "    },\n" +
                        "    \"amount\": {\n" +
                        "        \"currency\": \"GBP\",\n" +
                        "        \"minorUnits\": 24318\n" +
                        "    }\n" +
                        "}"))
        );

        //when
        final Balance balanceByAccountId = customerApiClient.getBalanceByAccountId(UUID.fromString("8833f0cf-6373-4c4a-b926-33081a16b1c7"));

        //then
        assertNotNull(balanceByAccountId.getEffectiveBalance());
    }

    @Test
    public void shouldGetAllSavingsGoals() {

        //given
        stubFor(get(urlPathEqualTo("/account/8833f0cf-6373-4c4a-b926-33081a16b1c7/savings-goals/"))
                .withHeader(ACCEPT, equalTo(APPLICATION_JSON))
                .withHeader(CONTENT_TYPE, equalTo(APPLICATION_JSON))
                .willReturn(okJson("{\n" +
                        "    \"savingsGoalList\": [\n" +
                        "        {\n" +
                        "            \"savingsGoalUid\": \"cad7da20-6f41-41ab-a1b4-aee309f07d21\",\n" +
                        "            \"name\": \"buy Xbox\",\n" +
                        "            \"target\": {\n" +
                        "                \"currency\": \"GBP\",\n" +
                        "                \"minorUnits\": 567891\n" +
                        "            },\n" +
                        "            \"totalSaved\": {\n" +
                        "                \"currency\": \"GBP\",\n" +
                        "                \"minorUnits\": 666\n" +
                        "            },\n" +
                        "            \"savedPercentage\": 0\n" +
                        "        }\n" +
                        "    ]\n" +
                        "}"))
        );

        //when
        final SavingsGoals savingsGoalByAccountId = customerApiClient.getSavingsGoalByAccountId(UUID.fromString("8833f0cf-6373-4c4a-b926-33081a16b1c7"));

        //then
        assertNotNull(savingsGoalByAccountId);
        assertEquals(savingsGoalByAccountId.getSavingsGoalList().get(0).getSavingsGoalUid().toString(), "cad7da20-6f41-41ab-a1b4-aee309f07d21");
    }

    @Test
    public void shouldAddMoneyToSavingsGoal() {
        //given
        String url = "/account/8833f0cf-6373-4c4a-b926-33081a16b1c7" +
                "/savings-goals/d28adf44-38d1-4d11-a3ce-84899f225a4d/add-money/([a-z0-9-]*)";
        stubFor(put(urlPathMatching(url))
                .withHeader(ACCEPT, equalTo(APPLICATION_JSON))
                .withHeader(CONTENT_TYPE, equalTo(APPLICATION_JSON))
                .withRequestBody(equalToJson("{\n" +
                        "  \"amount\": {\n" +
                        "    \"currency\": \"GBP\",\n" +
                        "    \"minorUnits\": 123\n" +
                        "  }\n" +
                        "}"))
                .willReturn(aResponse()
                        .withStatus(200))
        );

        //when and then
        customerApiClient.addMoneyToSavingsGoal(UUID.fromString("8833f0cf-6373-4c4a-b926-33081a16b1c7"),
                UUID.fromString("d28adf44-38d1-4d11-a3ce-84899f225a4d"),
                new Amount("GBP", 123));


    }

    @Test
    public void shouldThrowExceptionOnClientFaults() {

        //given
        stubFor(get(urlPathEqualTo("/accounts/8833f0cf-6373-4c4a-b926-33081a16b1c7/balance"))
                .withHeader(ACCEPT, equalTo(APPLICATION_JSON))
                .withHeader(CONTENT_TYPE, equalTo(APPLICATION_JSON))
                .willReturn(aResponse()
                        .withStatus(STATUS_CODE_FOR_CLIENT_FAULT)
                        .withHeader(CONTENT_TYPE, APPLICATION_JSON))
        );

        //when and then
        Exception exception = assertThrows(RuntimeException.class, () ->
                customerApiClient.getAllAccounts());
        assertTrue(exception.getMessage().contains("Error processing request"));
    }

    @Test
    public void shouldThrowExceptionOnServerFaults() {

        //given
        stubFor(get(urlPathEqualTo("/accounts"))
                .withHeader(ACCEPT, equalTo(APPLICATION_JSON))
                .withHeader(CONTENT_TYPE, equalTo(APPLICATION_JSON))
                .willReturn(aResponse()
                        .withStatus(STATUS_CODE_FOR_SERVER_FAULT)
                        .withHeader(CONTENT_TYPE, APPLICATION_JSON))
        );

        //when and then
        Exception exception = assertThrows(RuntimeException.class, () ->
                customerApiClient.getAllAccounts());
        assertTrue(exception.getMessage().contains("Error processing request"));
    }

    private Transaction getTransactionWithFeedId(final TransactionFeed allTransactionsSince, String feedId) {
        return allTransactionsSince.getFeedItems().stream()
                .filter(feeditem -> feeditem.getFeedItemUid().toString().equals(feedId))
                .findAny()
                .get();
    }

    private String getDateInIsoFormat(int year, int month, int dayOfMonth) {
        ZonedDateTime zonedDateTime = ZonedDateTime.of(year, month, dayOfMonth,
                0, 0, 0, 0,
                ZoneId.of("GMT"));
        return zonedDateTime.format(DateTimeFormatter.ISO_INSTANT);
    }

}
