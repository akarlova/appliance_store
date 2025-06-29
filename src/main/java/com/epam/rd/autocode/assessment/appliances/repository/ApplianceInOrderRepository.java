package com.epam.rd.autocode.assessment.appliances.repository;

import com.epam.rd.autocode.assessment.appliances.model.OrderRow;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ApplianceInOrderRepository extends JpaRepository<OrderRow, Long> {

}
