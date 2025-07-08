package com.epam.rd.autocode.assessment.appliances.controller;

import com.epam.rd.autocode.assessment.appliances.dto.ApplianceDto;
import com.epam.rd.autocode.assessment.appliances.dto.OrdersDto;
import com.epam.rd.autocode.assessment.appliances.service.ApplianceService;
import com.epam.rd.autocode.assessment.appliances.service.ManufacturerService;
import com.epam.rd.autocode.assessment.appliances.service.OrderService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.security.Principal;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ApplianceController.class)
class ApplianceControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ApplianceService applianceService;

    @MockBean
    private ManufacturerService manufacturerService;

    @MockBean
    private OrderService orderService;

    @Test
    @WithMockUser(roles = "EMPLOYEE")
    void newForm_shouldPopulateModel() throws Exception {
        when(manufacturerService.findAll()).thenReturn(List.of());

        mockMvc.perform(get("/appliances/new"))
                .andExpect(status().isOk())
                .andExpect(view().name("appliance/newAppliance"))
                .andExpect(model().attributeExists(
                        "appliance",
                        "categories",
                        "powerTypes",
                        "manufacturers"));
    }

    @Test
    @WithMockUser(roles = "EMPLOYEE")
    void editForm_shouldPopulateModel() throws Exception {
        ApplianceDto dto = new ApplianceDto();
        dto.setId(3L);
        when(applianceService.findById(3L)).thenReturn(dto);
        when(manufacturerService.findAll()).thenReturn(List.of());

        mockMvc.perform(get("/appliances/3/edit"))
                .andExpect(status().isOk())
                .andExpect(view().name("appliance/editAppliance"))
                .andExpect(model().attribute("appliance", dto))
                .andExpect(model().attributeExists(
                        "categories",
                        "powerTypes",
                        "manufacturers"));
    }

    @Test
    @WithMockUser(roles = "EMPLOYEE")
    void delete_shouldRedirectToList_andCallService() throws Exception {
        mockMvc.perform(get("/appliances/4/delete"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/appliances"));

        verify(applianceService).delete(4L);
    }
}
