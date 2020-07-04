package com.sb.savingsgoal.controller;

import java.util.List;
import java.util.UUID;

import com.sb.savingsgoal.model.SavingsGoal;
import com.sb.savingsgoal.service.SavingsGoalCalculator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class SavingsGoalController {

    private SavingsGoalCalculator savingsGoalCalculator;

    public SavingsGoalController(@Autowired final SavingsGoalCalculator savingsGoalCalculator) {
        this.savingsGoalCalculator = savingsGoalCalculator;
    }

    @PutMapping(value = "/accounts/{accountUid}/savingsgoal",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> triggerRoundUp(@PathVariable UUID accountUid, @RequestParam String triggerRoundUp) {

        if (triggerRoundUp.equalsIgnoreCase("true")) {
            final List<SavingsGoal> savingsGoals = savingsGoalCalculator.performRoundUpOfWeek(accountUid);
            return new ResponseEntity<>(savingsGoals, HttpStatus.OK);
        }
        return new ResponseEntity<>(new Error("triggerRoundUp should be true"), HttpStatus.OK);
    }

}
