package com.sb.savingsgoal.service;

import java.util.List;
import java.util.UUID;

import com.sb.savingsgoal.model.Accounts;
import com.sb.savingsgoal.model.SavingsGoalAmount;
import com.sb.savingsgoal.model.Amount;
import com.sb.savingsgoal.model.Balance;
import com.sb.savingsgoal.model.SavingsGoals;
import com.sb.savingsgoal.model.TransactionFeed;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.RestTemplate;

@Component
@Setter
public class CustomerApiClient {

    public static final String ACCOUNTS = "/accounts";
    public static final String FEED_ACCOUNT = "/feed/account/";
    public static final String CATEGORY = "/category/";
    public static final String CHANGES_SINCE = "?changesSince=";
    public static final String ACCOUNT = "/account/";
    public static final String SAVINGS_GOALS = "/savings-goals/";
    public static final String ADD_MONEY = "/add-money/";

    private final RestTemplate restTemplate;

    @Value("${app.root.context}")
    private String rootContext;
    @Value("${accounts.customer.bearer.token}")
    private String token;

    public CustomerApiClient(@Autowired final RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public Accounts getAllAccounts() {
        final String url = rootContext + ACCOUNTS;
        final ResponseEntity<Accounts> response;
        try {
            response = restTemplate.exchange(url, HttpMethod.GET, getHttpEntityForString(), Accounts.class);
        } catch (RestClientResponseException e) {
            throw new CustomerApiClientException("Error processing request:" + e.getMessage());
        }
        return response.getBody();
    }

    public TransactionFeed getAllTransactionsSince(String changeSince, UUID accountUid, UUID categoryUid) {

        final String url = rootContext + FEED_ACCOUNT + accountUid.toString() + CATEGORY + categoryUid.toString() + CHANGES_SINCE + changeSince;
        final ResponseEntity<TransactionFeed> response;
        try {
            response = restTemplate.exchange(url, HttpMethod.GET, getHttpEntityForString(), TransactionFeed.class);
        } catch (RestClientResponseException e) {
            throw new CustomerApiClientException("Error processing request:" + e.getMessage());
        }
        return response.getBody();
    }

    public Balance getBalanceByAccountId(UUID accountId) {
        final String url = rootContext + "/accounts/" + accountId.toString() + "/balance";
        final ResponseEntity<Balance> response;
        try {
            response = restTemplate.exchange(url, HttpMethod.GET, getHttpEntityForString(), Balance.class);
        } catch (RestClientResponseException e) {
            throw new CustomerApiClientException("Error processing request:" + e.getMessage());
        }
        return response.getBody();
    }

    public SavingsGoals getSavingsGoalByAccountId(UUID accountId) {
        final String url = rootContext + ACCOUNT + accountId.toString() + SAVINGS_GOALS;
        final ResponseEntity<SavingsGoals> response;
        try {
            response = restTemplate.exchange(url, HttpMethod.GET, getHttpEntityForString(), SavingsGoals.class);
        } catch (RestClientResponseException e) {
            throw new CustomerApiClientException("Error processing request:" + e.getMessage());
        }
        return response.getBody();
    }

    public void addMoneyToSavingsGoal(UUID accountId, UUID savingsGoalId, Amount amount) {
        final String url = rootContext + ACCOUNT + accountId.toString() + SAVINGS_GOALS + savingsGoalId.toString()
                + ADD_MONEY + UUID.randomUUID().toString();
        try {
            final SavingsGoalAmount savingsGoalAmount = new SavingsGoalAmount(amount);
            HttpEntity<SavingsGoalAmount> requestUpdate = new HttpEntity<>(savingsGoalAmount, getHttpHeader());
            restTemplate.put(url, requestUpdate);
        } catch (RestClientResponseException e) {
            throw new CustomerApiClientException("Error processing request:" + e.getMessage());
        }
    }

    private HttpEntity<String> getHttpEntityForString() {
        return new HttpEntity<>(getHttpHeader());
    }

    private HttpHeaders getHttpHeader() {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));
        return headers;
    }
}
