package com.epam.rd.autocode.assessment.appliances.dto;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrdersDto {
    private Long id;
    private String status;
    private Long clientId;
    private Long employeeId;
    private List<OrderRowDto> orderRows;
    private BigDecimal totalAmount;

    private String clientName;
    private String clientEmail;
    private String employeeName;
    private String employeeDepartment;
}
