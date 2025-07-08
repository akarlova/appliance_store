package com.epam.rd.autocode.assessment.appliances.controller;

import com.epam.rd.autocode.assessment.appliances.dto.ApplianceDto;
import com.epam.rd.autocode.assessment.appliances.dto.ClientDto;
import com.epam.rd.autocode.assessment.appliances.dto.OrdersDto;
import com.epam.rd.autocode.assessment.appliances.service.ApplianceService;
import com.epam.rd.autocode.assessment.appliances.service.ClientService;
import com.epam.rd.autocode.assessment.appliances.service.EmployeeService;
import com.epam.rd.autocode.assessment.appliances.service.OrderService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@WebMvcTest(OrdersController.class)
@AutoConfigureMockMvc(addFilters = false)
class OrdersControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private OrderService orderService;
    @MockBean
    private ApplianceService applianceService;
    @MockBean
    private ClientService clientService;
    @MockBean
    private EmployeeService employeeService;

    private OrdersDto orderDto;
    private ClientDto clientDto;

    @BeforeEach
    void setUp() {
        orderDto = new OrdersDto();
        orderDto.setId(1L);
        orderDto.setTotalAmount(new BigDecimal("100"));
        clientDto = new ClientDto(2L, "Jane", "jane@example.com", "pwd", "CARD");
    }

    @Test
    void list_shouldPopulateModelAndReturnView() throws Exception {
        when(orderService.findAll(any())).thenReturn(Collections.singletonList(orderDto));
        mockMvc.perform(get("/orders"))
                .andExpect(status().isOk())
                .andExpect(view().name("order/orders"))
                .andExpect(model().attribute("orders", hasSize(1)));
    }

    @Test
    void viewCart_shouldSetSessionAndReturnView() throws Exception {
        when(orderService.findById(eq(1L), any())).thenReturn(orderDto);
        mockMvc.perform(get("/orders/1/cart").session(new MockHttpSession()))
                .andExpect(status().isOk())
                .andExpect(view().name("order/cart"))
                .andExpect(model().attribute("order", orderDto))
                .andExpect(model().attribute("bonus", new BigDecimal("5.00")));
    }

    @Test
    void viewOrder_shouldPopulateModelAndReturnView() throws Exception {
        when(orderService.findById(eq(2L), any())).thenReturn(orderDto);
        mockMvc.perform(get("/orders/2"))
                .andExpect(status().isOk())
                .andExpect(view().name("order/viewOrder"))
                .andExpect(model().attribute("order", orderDto));
    }

    @Test
    @WithMockUser(roles = "CLIENT")
    void create_withValidInput_shouldRedirectToCart() throws Exception {
        OrdersDto created = new OrdersDto();
        created.setId(3L);
        when(orderService.create(any(OrdersDto.class), any())).thenReturn(created);
        mockMvc.perform(post("/orders").param("clientId", "2").with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/orders/3/cart"));
        verify(orderService).create(any(OrdersDto.class), any());
    }

    @Test
    @WithMockUser(roles = "CLIENT")
    void choiceAppliance_shouldPopulateModelAndReturnView() throws Exception {
        when(orderService.findById(eq(4L), any())).thenReturn(orderDto);
        when(applianceService.findAll()).thenReturn(Arrays.asList(new ApplianceDto()));
        mockMvc.perform(get("/orders/4/choice-appliance"))
                .andExpect(status().isOk())
                .andExpect(view().name("order/choiceAppliance"))
                .andExpect(model().attribute("order", orderDto))
                .andExpect(model().attribute("appliances", hasSize(1)));
    }
}
