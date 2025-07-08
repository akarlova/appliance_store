package com.epam.rd.autocode.assessment.appliances.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderRowDto {
    private Long id;
    private Long applianceId;
    private String applianceName;
    private Long number;
    private BigDecimal amount;
}
