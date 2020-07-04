package com.sb.savingsgoal.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

@JsonIgnoreProperties(ignoreUnknown = true)
@EqualsAndHashCode
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class EffectiveBalance {

    private String currency;
    private Integer minorUnits;
}
