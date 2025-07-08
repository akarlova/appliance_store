package com.epam.rd.autocode.assessment.appliances.dto;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ClientProfileDto {
    private Long id;
    private String name;
    private String email;
    private String card;
}
