package com.sb.savingsgoal.model;

import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Getter
public class SavingsGoalAmount implements Serializable {

    private Amount amount;
}
