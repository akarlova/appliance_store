package com.epam.rd.autocode.assessment.appliances.model;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Entity
@Table(name = "order_row")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderRow {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "appliance_id")
    private Appliance appliance;

    /** (appliance.price × number) */
    private BigDecimal amount;

    /** quantity */
    private Long number;
}
