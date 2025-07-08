package com.epam.rd.autocode.assessment.appliances.controller;

import com.epam.rd.autocode.assessment.appliances.dto.ManufacturerDto;
import com.epam.rd.autocode.assessment.appliances.service.ManufacturerService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;

import static org.mockito.Mockito.*;
import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ManufacturerController.class)
class ManufacturerControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ManufacturerService manufacturerService;

    private ManufacturerDto sample;

    @BeforeEach
    void setUp() {
        sample = new ManufacturerDto(1L, "TestCo");
    }

    @Test
    @WithMockUser(roles = "EMPLOYEE")
    void list_shouldPopulateModelAndReturnView() throws Exception {
        when(manufacturerService.findAll())
                .thenReturn(Arrays.asList(sample));

        mockMvc.perform(get("/manufacturers"))
                .andExpect(status().isOk())
                .andExpect(view().name("manufacturer/manufacturers"))
                .andExpect(model().attribute("manufacturers", hasSize(1)))
                .andExpect(model().attribute("manufacturers", contains(sample)));
    }

    @Test
    @WithMockUser(roles = "EMPLOYEE")
    void newForm_shouldPopulateModelAndReturnForm() throws Exception {
        mockMvc.perform(get("/manufacturers/new"))
                .andExpect(status().isOk())
                .andExpect(view().name("manufacturer/newManufacturer"))
                .andExpect(model().attributeExists("manufacturer"));
    }


    @Test
    @WithMockUser(roles = "EMPLOYEE")
    void editForm_shouldPopulateModelAndReturnForm() throws Exception {
        when(manufacturerService.findById(1L)).thenReturn(sample);

        mockMvc.perform(get("/manufacturers/1/edit"))
                .andExpect(status().isOk())
                .andExpect(view().name("manufacturer/editManufacturer"))
                .andExpect(model().attribute("manufacturer", sample));
    }


    @Test
    @WithMockUser(roles = "EMPLOYEE")
    void delete_shouldRedirect() throws Exception {
        mockMvc.perform(get("/manufacturers/1/delete"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/manufacturers"));

        verify(manufacturerService).delete(1L);
    }
}
