package com.sb.savingsgoal.model;

import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

@JsonIgnoreProperties(ignoreUnknown = true)
@EqualsAndHashCode
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class Account {

    private UUID accountUid;
    private UUID defaultCategory;
}
