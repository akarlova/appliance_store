package com.epam.rd.autocode.assessment.appliances.service.Impl;
import com.epam.rd.autocode.assessment.appliances.dto.OrderRowDto;
import com.epam.rd.autocode.assessment.appliances.dto.OrdersDto;
import com.epam.rd.autocode.assessment.appliances.exception.BadRequestException;
import com.epam.rd.autocode.assessment.appliances.exception.ResourceNotFoundException;
import com.epam.rd.autocode.assessment.appliances.model.*;
import com.epam.rd.autocode.assessment.appliances.repository.*;
import com.epam.rd.autocode.assessment.appliances.service.impl.OrderServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.modelmapper.ModelMapper;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.math.BigDecimal;
import java.security.Principal;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

public class OrderServiceImplTest {
    private OrdersRepository ordersRepo;
    private ApplianceRepository applianceRepo;
    private EmployeeRepository empRepo;
    private ApplianceInOrderRepository rowRepo;
    private ClientRepository clientRepo;
    private ModelMapper mapper;
    private OrderServiceImpl service;

    private Principal principal;
    private SecurityContext securityContext;

    @BeforeEach
    void setup() {
        ordersRepo = mock(OrdersRepository.class);
        applianceRepo = mock(ApplianceRepository.class);
        empRepo = mock(EmployeeRepository.class);
        rowRepo = mock(ApplianceInOrderRepository.class);
        clientRepo = mock(ClientRepository.class);
        mapper = mock(ModelMapper.class);
        service = new OrderServiceImpl(ordersRepo, applianceRepo, empRepo, rowRepo, mapper, clientRepo);

        principal = () -> "user@example.com";
        Authentication auth = mock(Authentication.class);
        when(auth.getAuthorities()).thenReturn(Collections.emptyList());
        securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(auth);
        SecurityContextHolder.setContext(securityContext);
    }

    @Test
    void findAll_nonClient_returnsAll() {
        Orders o = new Orders(); o.setStatus(OrderStatus.DRAFT);
        when(ordersRepo.findAllByStatusNot(OrderStatus.DRAFT)).thenReturn(Collections.singletonList(o));
        OrdersDto dto = new OrdersDto();
        when(mapper.map(o, OrdersDto.class)).thenReturn(dto);

        List<OrdersDto> result = service.findAll(principal);

        assertEquals(1, result.size());
        assertSame(dto, result.get(0));
    }

    @Test
    void findAll_client_returnsByEmail() {
        Authentication auth = mock(Authentication.class);
        List<GrantedAuthority> roles = Collections.singletonList(new SimpleGrantedAuthority("ROLE_CLIENT"));
        doReturn(roles).when(auth).getAuthorities();
        doReturn("user@example.com").when(auth).getName();
        when(securityContext.getAuthentication()).thenReturn(auth);

        Orders o = new Orders(); o.setStatus(OrderStatus.DRAFT);
        when(ordersRepo.findAllByClientEmailAndStatusNot("user@example.com", OrderStatus.DRAFT))
                .thenReturn(Collections.singletonList(o));
        OrdersDto dto = new OrdersDto();
        when(mapper.map(o, OrdersDto.class)).thenReturn(dto);

        List<OrdersDto> result = service.findAll(principal);

        assertEquals(1, result.size());
        assertSame(dto, result.get(0));
    }

    @Test
    void findById_exists() {
        Orders o = new Orders(); o.setStatus(OrderStatus.DRAFT); o.setId(5L);
        when(ordersRepo.findById(5L)).thenReturn(Optional.of(o));
        OrdersDto dto = new OrdersDto();
        when(mapper.map(o, OrdersDto.class)).thenReturn(dto);

        OrdersDto result = service.findById(5L, principal);

        assertSame(dto, result);
    }

    @Test
    void findById_notExists_throws() {
        when(ordersRepo.findById(1L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> service.findById(1L, principal));
    }

    @Test
    void create_withClientId_mapsAndSaves() {
        OrdersDto dtoIn = new OrdersDto(); dtoIn.setClientId(2L);
        Orders entity = new Orders();
        when(mapper.map(dtoIn, Orders.class)).thenReturn(entity);
        Client client = new Client(); client.setId(2L);
        when(clientRepo.findById(2L)).thenReturn(Optional.of(client));
        Orders saved = entity; entity.setId(10L);
        when(ordersRepo.save(entity)).thenReturn(saved);
        OrdersDto dtoOut = new OrdersDto();
        when(mapper.map(saved, OrdersDto.class)).thenReturn(dtoOut);

        OrdersDto result = service.create(dtoIn, principal);

        assertSame(dtoOut, result);
    }

    @Test
    void create_withoutClientId_usesPrincipalEmail() {
        OrdersDto dtoIn = new OrdersDto();
        Orders entity = new Orders();
        when(mapper.map(dtoIn, Orders.class)).thenReturn(entity);
        Client client = new Client(); client.setId(3L);
        when(clientRepo.findByEmail("user@example.com")).thenReturn(Optional.of(client));
        when(ordersRepo.save(entity)).thenReturn(entity);
        when(mapper.map(entity, OrdersDto.class)).thenReturn(new OrdersDto());

        assertDoesNotThrow(() -> service.create(dtoIn, principal));
    }

    @Test
    void update_whenNotInProgress_throws() {
        Orders entity = new Orders(); entity.setStatus(OrderStatus.DRAFT);
        when(ordersRepo.findById(7L)).thenReturn(Optional.of(entity));
        OrdersDto dto = new OrdersDto();
        assertThrows(IllegalStateException.class, () -> service.update(7L, dto, principal));
    }

    @Test
    void update_emptyRows_throwsBadRequest() {
        Orders entity = new Orders(); entity.setStatus(OrderStatus.IN_PROGRESS);
        when(ordersRepo.findById(8L)).thenReturn(Optional.of(entity));
        OrdersDto dto = new OrdersDto(); dto.setOrderRows(Collections.emptyList());
        assertThrows(BadRequestException.class, () -> service.update(8L, dto, principal));
    }

    @Test
    void deleteRow_removeAndError() {
        Orders order = new Orders();
        OrderRow row = new OrderRow(); row.setId(5L);
        order.setOrderRowSet(new HashSet<>(Collections.singletonList(row)));
        when(ordersRepo.findById(1L)).thenReturn(Optional.of(order));

        assertDoesNotThrow(() -> service.deleteRow(1L, 5L, principal));

        order.setOrderRowSet(new HashSet<>());
        assertThrows(ResourceNotFoundException.class, () -> service.deleteRow(1L, 5L, principal));
    }
}
