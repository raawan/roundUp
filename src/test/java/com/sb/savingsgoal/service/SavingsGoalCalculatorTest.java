package com.sb.savingsgoal.service;


import static com.sb.savingsgoal.model.DIRECTION.IN;
import static com.sb.savingsgoal.model.DIRECTION.OUT;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class SavingsGoalCalculatorTest {

    public static final String GBP = "GBP";

    @Mock
    private CustomerApiClient customerApiClient;
    private SavingsGoalCalculator savingsGoalCalculator;
    ArgumentCaptor<Amount> argument = ArgumentCaptor.forClass(Amount.class);


    @BeforeEach
    public void setUp() {
        savingsGoalCalculator = new SavingsGoalCalculator(customerApiClient);
    }

    @Test
    public void shouldPerformRoundUpOfTheWeek() throws ExecutionException, InterruptedException {
        //given
        UUID accountId = randomId();
        UUID categoryId = randomId();

        when(customerApiClient.getAllAccounts())
                .thenReturn(accounts(accountId, categoryId));
        when(customerApiClient.getBalanceByAccountId(accountId))
                .thenReturn(balance(GBP, 122045));
        when(customerApiClient.getAllTransactionsSince(anyString(), eq(accountId), eq(categoryId)))
                .thenReturn(feed(
                        t(123, OUT), t(43210, IN), t(287, OUT), t(500, OUT))
                ); //RoundUp is 90 for this feed
        when(customerApiClient.getSavingsGoalByAccountId(accountId))
                .thenReturn(savingsGoals(goal("Buy XBOX", 567891, 0)));
        doNothing().when(customerApiClient).addMoneyToSavingsGoal(eq(accountId), any(UUID.class), any(Amount.class));

        //when
        savingsGoalCalculator.performRoundUpOfWeek(accountId);

        //then
        final int expectedRoundUp = 90;
        verify(customerApiClient, times(1))
                .addMoneyToSavingsGoal(eq(accountId), any(UUID.class), eq(amount(expectedRoundUp)));
    }

    @Test
    public void shouldPerformRoundOnMultipleGoals() throws ExecutionException, InterruptedException {

        //given
        UUID accountId = randomId();
        UUID categoryId = randomId();

        when(customerApiClient.getAllAccounts())
                .thenReturn(accounts(accountId, categoryId));
        when(customerApiClient.getBalanceByAccountId(accountId))
                .thenReturn(balance(GBP, 122045));
        when(customerApiClient.getAllTransactionsSince(anyString(), eq(accountId), eq(categoryId)))
                .thenReturn(feed(
                        t(123, OUT), t(43210, IN), t(287, OUT),
                        t(500, OUT), t(130, OUT), t(160, OUT))
                ); //RoundUp is 200 for this feed
        when(customerApiClient.getSavingsGoalByAccountId(accountId))
                .thenReturn(
                        savingsGoals(
                                goal("Buy XBOX", 1300, 1240),
                                goal("Trip To Paris", 2700, 2600),
                                goal("Buy a car", 190000, 189960))
                );

        doNothing() //invocation 1
                .doNothing() // invocation 2
                .doNothing() // invocation 3
                .when(customerApiClient).addMoneyToSavingsGoal(eq(accountId), any(UUID.class), any(Amount.class));

        //when
        savingsGoalCalculator.performRoundUpOfWeek(accountId);

        //then
        verify(customerApiClient, times(3))
                .addMoneyToSavingsGoal(eq(accountId), any(UUID.class), argument.capture());

        final List<Amount> amountList = argument.getAllValues();
        assertTrue(containsIn(amountList, 40));
        assertTrue(containsIn(amountList, 60));
        assertTrue(containsIn(amountList, 100));
    }

    private boolean containsIn(final List<Amount> amountList, final int expectedRoundUp) {

        return amountList.stream()
                .filter(amount -> amount.getMinorUnits() == expectedRoundUp)
                .findAny()
                .isPresent();
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
