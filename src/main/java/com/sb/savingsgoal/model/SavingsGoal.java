package com.sb.savingsgoal.model;

import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@JsonIgnoreProperties(ignoreUnknown = true)
@EqualsAndHashCode
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class SavingsGoal {

    private UUID savingsGoalUid;
    private String name;
    private Amount target;
    private Amount totalSaved;

}
