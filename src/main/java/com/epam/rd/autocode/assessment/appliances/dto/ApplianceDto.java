package com.epam.rd.autocode.assessment.appliances.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ApplianceDto {
    private Long id;

    @NotBlank(message = "{appliance.name.notblank}")
    private String name;

    @NotNull(message = "{appliance.category.notnull}")
    private String category;

    @NotBlank(message = "{appliance.model.notblank}")
    private String model;

    @NotNull(message = "{appliance.manufacturer.notnull}")
    private Long manufacturerId;

    private String manufacturerName;

    @NotNull(message = "{appliance.powerType.notnull}")
    private String powerType;

    @NotBlank(message = "{appliance.characteristic.notblank}")
    private String characteristic;

    @NotBlank(message = "{appliance.description.notblank}")
    private String description;

    @NotNull(message = "{appliance.power.notnull}")
    @Min(value = 1, message = "{appliance.power.min}")
    private Integer power;

    @NotNull(message = "{appliance.price.notnull}")
    @DecimalMin(value = "0.01", message = "{appliance.price.min}")
    private BigDecimal price;
}
