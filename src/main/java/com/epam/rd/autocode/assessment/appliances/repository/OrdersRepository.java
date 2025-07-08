package com.epam.rd.autocode.assessment.appliances.repository;

import com.epam.rd.autocode.assessment.appliances.model.OrderStatus;
import com.epam.rd.autocode.assessment.appliances.model.Orders;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface OrdersRepository extends JpaRepository<Orders, Long> {
    Optional<Orders> findFirstByClientEmailAndStatus(String email, OrderStatus status);
    Optional<Orders> findFirstByClientIdAndStatus(Long clientId, OrderStatus status);
    List<Orders> findAllByClientEmailAndStatusNot(String email, OrderStatus status);
    List<Orders> findAllByStatusNot(OrderStatus status);
}
