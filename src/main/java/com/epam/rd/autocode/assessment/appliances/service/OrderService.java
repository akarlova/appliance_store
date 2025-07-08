package com.epam.rd.autocode.assessment.appliances.service;

import com.epam.rd.autocode.assessment.appliances.dto.OrdersDto;
import com.epam.rd.autocode.assessment.appliances.model.OrderStatus;
import com.epam.rd.autocode.assessment.appliances.model.Orders;

import java.security.Principal;
import java.util.List;

public interface OrderService {
    List<OrdersDto> findAll(Principal principal);
    OrdersDto findById(Long id, Principal principal);
    OrdersDto create(OrdersDto dto, Principal principal);
    OrdersDto update(Long id, OrdersDto dto, Principal principal);
    void delete(Long id, Principal principal);
    OrdersDto approve(Long id, Principal principal);
    OrdersDto unapprove(Long id, Principal principal);
    OrdersDto addRow(Long orderId, Long applianceId, Long quantity, Principal principal);
    OrdersDto getOrCreateDraft(Principal principal);
    OrdersDto submit(Long orderId, Principal principal);
    OrdersDto updateRow(Long orderId, Long rowId, Long quantity, Principal principal);
    void deleteRow(Long orderId, Long rowId, Principal user);
    void cancelOrder(Long orderId, Principal principal);
    void restoreOrder(Long orderId, OrdersDto originalDto, Principal principal);
    OrdersDto getOrCreateDraftForClient(Long clientId, Principal principal);

}
