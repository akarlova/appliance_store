package com.epam.rd.autocode.assessment.appliances.service.impl;

import com.epam.rd.autocode.assessment.appliances.dto.OrderRowDto;
import com.epam.rd.autocode.assessment.appliances.dto.OrdersDto;
import com.epam.rd.autocode.assessment.appliances.exception.BadRequestException;
import com.epam.rd.autocode.assessment.appliances.exception.ResourceNotFoundException;
import com.epam.rd.autocode.assessment.appliances.model.*;
import com.epam.rd.autocode.assessment.appliances.repository.*;
import com.epam.rd.autocode.assessment.appliances.service.OrderService;
import org.modelmapper.ModelMapper;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.security.Principal;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@Transactional(readOnly = true)
public class OrderServiceImpl implements OrderService {
    private final OrdersRepository ordersRepo;
    private final ApplianceRepository applianceRepo;
    private final EmployeeRepository empRepo;
    private final ApplianceInOrderRepository rowRepo;
    private final ModelMapper mapper;
    private final ClientRepository clientRepo;

    public OrderServiceImpl(OrdersRepository ordersRepo,
                            ApplianceRepository applianceRepo,
                            EmployeeRepository empRepo,
                            ApplianceInOrderRepository rowRepo,
                            ModelMapper mapper,
                            ClientRepository clientRepo) {
        this.ordersRepo = ordersRepo;
        this.applianceRepo = applianceRepo;
        this.empRepo = empRepo;
        this.rowRepo = rowRepo;
        this.mapper = mapper;
        this.clientRepo = clientRepo;
    }

    @Override
    public List<OrdersDto> findAll(Principal principal) {
        if (hasClientRole()) {
            return ordersRepo
                    .findAllByClientEmailAndStatusNot(principal.getName(), OrderStatus.DRAFT)
                    .stream().map(this::toDto).toList();
        } else {
            return ordersRepo
                    .findAllByStatusNot(OrderStatus.DRAFT)
                    .stream().map(this::toDto).toList();
        }
    }

    @Override
    public OrdersDto findById(Long id, Principal principal) {
        Orders o = ordersRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found: " + id));
        return toDto(o);
    }

    @Override
    @Transactional
    public OrdersDto create(OrdersDto dto, Principal principal) {
        Orders entity = mapper.map(dto, Orders.class);
        entity.setStatus(OrderStatus.DRAFT);
        Long clientId = dto.getClientId();
        if (clientId == null) {
            clientId = clientRepo.findByEmail(principal.getName())
                    .orElseThrow(() -> new ResourceNotFoundException("Client not found"))
                    .getId();
        }
        clientRepo.findById(clientId).ifPresent(entity::setClient);
        Orders saved = ordersRepo.save(entity);
        return toDto(saved);
    }

    @Override
    @Transactional
    public OrdersDto update(Long id, OrdersDto dto, Principal principal) {
        Orders entity = ordersRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found: " + id));
        if (entity.getStatus() != OrderStatus.IN_PROGRESS) {
            throw new IllegalStateException(
                    "Forbidden to edit order with status  " + entity.getStatus());
        }
        List<OrderRowDto> validRows = (dto.getOrderRows() == null
                ? Collections.<OrderRowDto>emptyList()
                : dto.getOrderRows())
                .stream()
                .filter(rdto -> rdto.getApplianceId() != null && rdto.getNumber() != null && rdto.getNumber() > 0)
                .collect(Collectors.toList());
        if (validRows.isEmpty()) {
            throw new BadRequestException("Order must contain at least one item");
        }
        entity.getOrderRowSet().clear();
        List<OrderRow> rows = validRows.stream()
                .map(rdto -> {
                    Appliance a = applianceRepo.findById(rdto.getApplianceId())
                            .orElseThrow(() -> new ResourceNotFoundException(
                                    "Appliance not found: " + rdto.getApplianceId()));
                    OrderRow row = new OrderRow();
                    row.setAppliance(a);
                    row.setNumber(rdto.getNumber());
                    row.setAmount(a.getPrice().multiply(
                            BigDecimal.valueOf(rdto.getNumber())));
                    return row;
                })
                .collect(Collectors.toList());
        entity.getOrderRowSet().addAll(rows);
        Orders updated = ordersRepo.save(entity);
        return toDto(updated);
    }

    @Override
    @Transactional
    public void delete(Long id, Principal principal) {
        ordersRepo.deleteById(id);
    }

    @Override
    @Transactional
    public OrdersDto approve(Long id, Principal principal) {
        Orders o = ordersRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found: " + id));
        Employee emp = empRepo.findByEmail(principal.getName())
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found: " + principal.getName()));
        o.setStatus(OrderStatus.APPROVED);
        o.setEmployee(emp);
        Orders saved = ordersRepo.save(o);
        return toDto(saved);
    }

    @Override
    @Transactional
    public OrdersDto unapprove(Long id, Principal principal) {
        Orders o = ordersRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found: " + id));
        o.setStatus(OrderStatus.IN_PROGRESS);
        Orders saved = ordersRepo.save(o);
        return toDto(saved);
    }

    @Override
    @Transactional
    public OrdersDto addRow(Long orderId,
                            Long applianceId,
                            Long quantity,
                            Principal principal) {
        Orders order = ordersRepo.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found: " + orderId));
        Appliance a = applianceRepo.findById(applianceId)
                .orElseThrow(() -> new ResourceNotFoundException("Appliance not found: " + applianceId));

        Optional<OrderRow> existing = order.getOrderRowSet().stream()
                .filter(r -> r.getAppliance().getId().equals(applianceId))
                .findFirst();

        if (existing.isPresent()) {
            OrderRow row = existing.get();
            long newQty = row.getNumber() + quantity;
            row.setNumber(newQty);
            row.setAmount(a.getPrice().multiply(BigDecimal.valueOf(newQty)));
        } else {
            OrderRow row = new OrderRow();
            row.setAppliance(a);
            row.setNumber(quantity);
            row.setAmount(a.getPrice().multiply(BigDecimal.valueOf(quantity)));
            order.getOrderRowSet().add(row);
        }
        Orders saved = ordersRepo.save(order);
        return toDto(saved);
    }

    private OrdersDto toDto(Orders o) {
        OrdersDto dto = mapper.map(o, OrdersDto.class);
        dto.setStatus(o.getStatus().name());
        //Client
        if (o.getClient() != null) {
            dto.setClientId(o.getClient().getId());
            dto.setClientName(o.getClient().getName());
            dto.setClientEmail(o.getClient().getEmail());
        }
        //Employee
        if (o.getEmployee() != null) {
            dto.setEmployeeId(o.getEmployee().getId());
            dto.setEmployeeName(o.getEmployee().getName());
            dto.setEmployeeDepartment(o.getEmployee().getDepartment());
        }
        Set<OrderRow> rowEntities = o.getOrderRowSet();
        Stream<OrderRow> rowStream = (rowEntities == null
                ? Stream.empty()
                : rowEntities.stream()
        );
        List<OrderRowDto> rows = rowStream
                .sorted(Comparator.comparing(OrderRow::getId))
                .map(r -> {
                    OrderRowDto rd = mapper.map(r, OrderRowDto.class);
                    rd.setApplianceId(r.getAppliance().getId());
                    rd.setApplianceName(r.getAppliance().getName());
                    rd.setNumber(r.getNumber());     // количество
                    rd.setAmount(r.getAmount());     // сумма строки
                    return rd;
                })
                .collect(Collectors.toList());
        dto.setOrderRows(rows);
        BigDecimal total = rows.stream()
                .map(OrderRowDto::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        dto.setTotalAmount(total);
        return dto;
    }

    @Override
    @Transactional
    public OrdersDto getOrCreateDraft(Principal principal) {
        String email = principal.getName();
        Optional<Orders> opt = ordersRepo
                .findFirstByClientEmailAndStatus(email, OrderStatus.DRAFT);
        Orders order = opt.orElseGet(() -> {
            Orders d = new Orders();
            d.setStatus(OrderStatus.DRAFT);
            d.setClient(clientRepo.findByEmail(email)
                    .orElseThrow(() -> new ResourceNotFoundException("Client not found")));
            return ordersRepo.save(d);
        });
        return toDto(order);
    }

    @Transactional
    public OrdersDto getOrCreateDraftForClient(Long clientId, Principal principal) {
        Orders draft = ordersRepo
                .findFirstByClientIdAndStatus(clientId, OrderStatus.DRAFT)
                .orElseGet(() -> {
                    Orders d = new Orders();
                    d.setStatus(OrderStatus.DRAFT);
                    d.setClient(clientRepo.findById(clientId)
                            .orElseThrow(() -> new ResourceNotFoundException("Client not found: " + clientId)));
                    return ordersRepo.save(d);
                });
        return toDto(draft);
    }

    @Override
    @Transactional
    public OrdersDto submit(Long orderId, Principal principal) {
        Orders order = ordersRepo.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));
        order.setStatus(OrderStatus.IN_PROGRESS);
        return toDto(ordersRepo.save(order));
    }

    @Override
    @Transactional
    public OrdersDto updateRow(Long orderId, Long rowId, Long quantity, Principal principal) {
        Orders order = ordersRepo.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found: " + orderId));

        OrderRow row = order.getOrderRowSet()
                .stream()
                .filter(r -> r.getId().equals(rowId))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Order row not found: " + rowId));

        row.setNumber(quantity);
        BigDecimal price = row.getAppliance().getPrice();
        row.setAmount(price.multiply(BigDecimal.valueOf(quantity)));
        Orders saved = ordersRepo.save(order);
        return toDto(saved);
    }

    @Override
    @Transactional
    public void deleteRow(Long orderId, Long rowId, Principal principal) {
        Orders order = ordersRepo.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found: " + orderId));
        boolean removed = order.getOrderRowSet().removeIf(r -> r.getId().equals(rowId));
        if (!removed) {
            throw new ResourceNotFoundException("Order row not found: " + rowId);
        }
        ordersRepo.save(order);
    }

    private boolean hasClientRole() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return auth != null && auth.getAuthorities().stream()
                .anyMatch(a -> "ROLE_CLIENT".equals(a.getAuthority()));
    }

    @Override
    @Transactional
    public void cancelOrder(Long orderId, Principal principal) {
        Orders o = ordersRepo.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found: " + orderId));
        Employee emp = empRepo.findByEmail(principal.getName())
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found: " + principal.getName()));
        o.setStatus(OrderStatus.CANCELED);
        o.setEmployee(emp);
        ordersRepo.save(o);
    }

    @Override
    @Transactional
    public void restoreOrder(Long orderId, OrdersDto originalDto, Principal principal) {
        Orders order = ordersRepo.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found: " + orderId));
        order.setStatus(OrderStatus.valueOf(originalDto.getStatus()));
        order.getOrderRowSet().clear();
        List<OrderRow> restoredRows = originalDto.getOrderRows().stream()
                .map(rowDto -> {
                    Appliance appliance = applianceRepo.findById(rowDto.getApplianceId())
                            .orElseThrow(() -> new ResourceNotFoundException("Appliance not found"));
                    OrderRow row = new OrderRow();
                    row.setAppliance(appliance);
                    row.setNumber(rowDto.getNumber());
                    row.setAmount(appliance.getPrice().multiply(BigDecimal.valueOf(rowDto.getNumber())));
                    return row;
                })
                .collect(Collectors.toList());
        order.getOrderRowSet().addAll(restoredRows);
        ordersRepo.save(order);
    }
}
