package com.sb.savingsgoal;

import static com.sb.savingsgoal.model.DIRECTION.IN;
import static com.sb.savingsgoal.model.DIRECTION.OUT;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
import java.util.UUID;

import com.sb.savingsgoal.model.Account;
import com.sb.savingsgoal.model.Accounts;
import com.sb.savingsgoal.model.Amount;
import com.sb.savingsgoal.model.Balance;
import com.sb.savingsgoal.model.DIRECTION;
import com.sb.savingsgoal.model.EffectiveBalance;
import com.sb.savingsgoal.model.SavingsGoal;
import com.sb.savingsgoal.model.SavingsGoals;
import com.sb.savingsgoal.model.Transaction;
import com.sb.savingsgoal.model.TransactionFeed;
import com.sb.savingsgoal.service.CustomerApiClient;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ExtendWith(MockitoExtension.class)
@AutoConfigureMockMvc
class SavingsGoalApplicationTests {

    public static final String GBP = "GBP";

    @MockBean
    private CustomerApiClient customerApiClient;

    @Test
    void shouldReturnSavingsGoalsAfterApplyingRoundUp(@Autowired MockMvc mvc) throws Exception {

        //given
        UUID accountId = randomId();
        UUID categoryId = randomId();
        int expectedRoundUp = 90;

        when(customerApiClient.getAllAccounts())
                .thenReturn(accounts(accountId, categoryId));
        when(customerApiClient.getBalanceByAccountId(accountId))
                .thenReturn(balance(GBP, 122045));
        when(customerApiClient.getAllTransactionsSince(anyString(), eq(accountId), eq(categoryId)))
                .thenReturn(feed(
                        t(123, OUT), t(43210, IN), t(287, OUT), t(500, OUT))
                ); //RoundUp is 90 for this feed
        when(customerApiClient.getSavingsGoalByAccountId(accountId))
                .thenReturn(savingsGoals(goal("Buy XBOX", 567891, 0)))
                .thenReturn(savingsGoals(goal("Buy XBOX", 567891, expectedRoundUp)));
        doNothing().when(customerApiClient).addMoneyToSavingsGoal(eq(accountId), any(UUID.class), any(Amount.class));


        //when and then
        mvc.perform(put("/accounts/" + accountId + "/savingsgoal")
                .queryParam("triggerRoundUp", "true")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.[:1].target.minorUnits").value(567891))
                .andExpect(jsonPath("$.[:1].totalSaved.minorUnits").value(expectedRoundUp));
    }

    private SavingsGoals savingsGoals(SavingsGoal... savingsGoalsList) {
        return new SavingsGoals(List.of(savingsGoalsList));
    }

    private SavingsGoal goal(String name, int target, int saved) {
        return new SavingsGoal(randomId(), name, targetAmt(target), savedAmt(saved));
    }

    private TransactionFeed feed(Transaction... transactions) {
        return new TransactionFeed(List.of(transactions));
    }

    private Transaction t(final int units, final DIRECTION direction) {
        return new Transaction(randomId(), randomId(), amount(units), direction);
    }

    private Amount savedAmt(final int i) {
        return amount(i);
    }

    private Amount targetAmt(final int i) {
        return amount(i);
    }

    private Amount amount(final int i) {
        return new Amount(GBP, i);
    }

    private UUID randomId() {
        return UUID.randomUUID();
    }

    private Balance balance(final String currency, final int minorUnits) {
        return new Balance(new EffectiveBalance(currency, minorUnits));
    }

    private Accounts accounts(UUID accountid, UUID categoryId) {
        return new Accounts(List.of(new Account(accountid, categoryId)));
    }

}
