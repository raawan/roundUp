package com.sb.savingsgoal.service;

import java.time.DayOfWeek;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import com.sb.savingsgoal.model.Account;
import com.sb.savingsgoal.model.Accounts;
import com.sb.savingsgoal.model.Amount;
import com.sb.savingsgoal.model.Balance;
import com.sb.savingsgoal.model.SavingsGoal;
import com.sb.savingsgoal.model.TransactionFeed;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class SavingsGoalCalculator {

    private CustomerApiClient customerApiClient;

    public SavingsGoalCalculator(@Autowired final CustomerApiClient customerApiClient) {
        this.customerApiClient = customerApiClient;
    }

    public List<SavingsGoal> performRoundUpOfWeek(final UUID accountUid) {

        final List<SavingsGoal> savingsGoalList1 = customerApiClient.getSavingsGoalByAccountId(accountUid).getSavingsGoalList();
        if (!savingsGoalList1.isEmpty()) {
            int roundedSumOfTheWeek = calculateRoundedSumOfTheWeek(accountUid);
            if (isSufficientBalanceToContributeForRoundUp(accountUid, roundedSumOfTheWeek)) {
                applyRoundUpToGoals(accountUid, savingsGoalList1, roundedSumOfTheWeek);
            }
        }
        return customerApiClient.getSavingsGoalByAccountId(accountUid).getSavingsGoalList();
    }

    private void applyRoundUpToGoals(final UUID accountUid, final List<SavingsGoal> savingsGoalList1, int roundedSumOfTheWeek) {
        for (SavingsGoal goal : findSavingsGoalWithOutstandingAmount(savingsGoalList1)) {
            if (isRoundedSumMoreThanGoalsOutstandingAmount(roundedSumOfTheWeek, goal)) {
                final int outstandingAmountOfSavingGoal = getOutstandingAmountOfSavingGoal(goal);
                addMoneyToSavingsGoal(accountUid, goal, outstandingAmountOfSavingGoal);
                roundedSumOfTheWeek -= outstandingAmountOfSavingGoal;
            } else {
                addMoneyToSavingsGoal(accountUid, goal, roundedSumOfTheWeek);
                break;
            }
        }
        if (roundedSumOfTheWeek > 0) {
            //remove the balance from savings
            //No API available to set balance
        }
    }

    private boolean isRoundedSumMoreThanGoalsOutstandingAmount(final int roundedSumOfTheWeek, final SavingsGoal goal) {
        return goal.getTotalSaved().getMinorUnits() + roundedSumOfTheWeek > goal.getTarget().getMinorUnits();
    }

    private void addMoneyToSavingsGoal(final UUID accountUid, final SavingsGoal goal, final int outstandingAmountOfSavingGoal) {
        customerApiClient.addMoneyToSavingsGoal(accountUid, goal.getSavingsGoalUid(),
                new Amount("GBP", outstandingAmountOfSavingGoal));
    }

    private boolean isSufficientBalanceToContributeForRoundUp(final UUID accountUid, final int roundedSumOfTheWeek) {
        final Balance balance = customerApiClient.getBalanceByAccountId(accountUid);
        return balance.getBalanceUnits() - roundedSumOfTheWeek > 0;
    }

    private List<SavingsGoal> findSavingsGoalWithOutstandingAmount(final List<SavingsGoal> savingsGoalList1) {
        return savingsGoalList1.stream()
                .filter(savingsGoal -> getOutstandingAmountOfSavingGoal(savingsGoal) > 0)
                .collect(Collectors.toList());
    }

    private int calculateRoundedSumOfTheWeek(final UUID accountUid) {
        final TransactionFeed transactionFeedOfCustomer = this.getTransactionFeedOfCustomer(calculateChangeSinceDate(), accountUid);
        return transactionFeedOfCustomer.getFeedItems()
                .stream()
                .filter(feedItem -> feedItem.getDirectionValue().equalsIgnoreCase("OUT"))
                .mapToInt(feedItem -> (feedItem.getAmount().getMinorUnits() % 100))
                .filter(amount -> amount > 0)
                .map(amount -> 100 - amount)
                .sum();
    }

    private int getOutstandingAmountOfSavingGoal(final SavingsGoal savingsGoal) {
        return savingsGoal.getTarget().getMinorUnits() - savingsGoal.getTotalSaved().getMinorUnits();
    }

    private String calculateChangeSinceDate() {
        ZonedDateTime now = ZonedDateTime.now();
        //Assuming week starting from Monday
        int daysOfWeekPassed = calculateHowManyDaysOfWeekPassed(now.getDayOfWeek());
        final ZonedDateTime changeSinceDate = now.minusDays(daysOfWeekPassed);
        return changeSinceDate.format(DateTimeFormatter.ISO_INSTANT);
    }

    private TransactionFeed getTransactionFeedOfCustomer(final String changeSince, final UUID accountUid) {
        final Account accountById = this.findAccountById(customerApiClient.getAllAccounts(), accountUid);
        return customerApiClient.getAllTransactionsSince(changeSince, accountUid, accountById.getDefaultCategory());
    }

    private Account findAccountById(final Accounts accounts, final UUID accountId) {
        final Optional<Account> optionalAccount = accounts.getAccounts().stream().filter(account -> account.getAccountUid().equals(accountId)).findAny();
        return optionalAccount.orElseThrow(() -> new RuntimeException("Invalid accountId :" + accountId));
    }

    private int calculateHowManyDaysOfWeekPassed(final DayOfWeek dayOfWeek) {
        switch (dayOfWeek) {
            case TUESDAY:
                return 1;
            case WEDNESDAY:
                return 2;
            case THURSDAY:
                return 3;
            case FRIDAY:
                return 4;
            case SATURDAY:
                return 5;
            case SUNDAY:
                return 6;
            default:
                return 0;
        }
    }

}
