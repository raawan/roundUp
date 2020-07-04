package com.sb.savingsgoal.model;

import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@EqualsAndHashCode
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class Amount implements Serializable {

    private String currency;
    private Integer minorUnits;
}

