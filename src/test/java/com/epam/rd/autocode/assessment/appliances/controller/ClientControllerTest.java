package com.epam.rd.autocode.assessment.appliances.controller;

import com.epam.rd.autocode.assessment.appliances.dto.*;
import com.epam.rd.autocode.assessment.appliances.service.ClientService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.anonymous;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ClientController.class)
class ClientControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ClientService clientService;

    private ClientRegistrationDto registrationDto;
    private ClientProfileDto profileDto;
    private ClientDto clientDto;

    @BeforeEach
    void setUp() {
        // ClientRegistrationDto has only name, email, password
        registrationDto = new ClientRegistrationDto("John Doe", "john@example.com", "Password1!");
        profileDto = new ClientProfileDto(1L, "John Doe", "john@example.com", "CARD123");
        clientDto = new ClientDto(2L, "Jane Doe", "jane@example.com", "Passw0rd!", "CARD456");
    }

    @Test
    @WithMockUser(roles = "CLIENT")
    void profileForm_shouldPopulateModelAndReturnView() throws Exception {
        when(clientService.profileByEmail(anyString())).thenReturn(profileDto);

        mockMvc.perform(get("/clients/profile"))
                .andExpect(status().isOk())
                .andExpect(view().name("client/profile"))
                .andExpect(model().attributeExists("profile"));
    }


    @Test
    @WithMockUser(roles = "CLIENT")
    void updateProfile_withInvalidInput_shouldReturnForm() throws Exception {
        mockMvc.perform(post("/clients/profile")
                        .param("name", "")
                        .param("email", "bad")
                        .param("password", "short")
                        .param("card", "")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(view().name("client/profile"));
    }

    @Test
    @WithMockUser(roles = "EMPLOYEE")
    void list_shouldPopulateModelAndReturnView() throws Exception {
        when(clientService.findAll()).thenReturn(Arrays.asList(clientDto));

        mockMvc.perform(get("/clients"))
                .andExpect(status().isOk())
                .andExpect(view().name("client/clients"))
                .andExpect(model().attribute("clients", hasSize(1)));
    }

    @Test
    @WithMockUser(roles = "EMPLOYEE")
    void newForm_shouldPopulateModelAndReturnForm() throws Exception {
        mockMvc.perform(get("/clients/new"))
                .andExpect(status().isOk())
                .andExpect(view().name("client/newClient"))
                .andExpect(model().attributeExists("client"));
    }

    @Test
    @WithMockUser(roles = "EMPLOYEE")
    void create_withValidInput_shouldRedirect() throws Exception {
        when(clientService.create(any(ClientDto.class))).thenReturn(clientDto);

        mockMvc.perform(post("/clients")
                        .param("name", clientDto.getName())
                        .param("email", clientDto.getEmail())
                        .param("password", clientDto.getPassword())
                        .param("card", clientDto.getCard())
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/clients"));

        verify(clientService).create(any(ClientDto.class));
    }

    @Test
    @WithMockUser(roles = "EMPLOYEE")
    void create_withInvalidInput_shouldReturnForm() throws Exception {
        mockMvc.perform(post("/clients")
                        .param("name", "")
                        .param("email", "bad")
                        .param("password", "")
                        .param("card", "")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(view().name("client/newClient"));
    }

    @Test
    @WithMockUser(roles = "EMPLOYEE")
    void editForm_shouldPopulateModelAndReturnForm() throws Exception {
        when(clientService.findById(2L)).thenReturn(clientDto);

        mockMvc.perform(get("/clients/2/edit"))
                .andExpect(status().isOk())
                .andExpect(view().name("client/editClient"))
                .andExpect(model().attribute("client", clientDto));
    }

    @Test
    @WithMockUser(roles = "EMPLOYEE")
    void update_withValidInput_shouldRedirect() throws Exception {
        mockMvc.perform(post("/clients/2")
                        .param("name", clientDto.getName())
                        .param("email", clientDto.getEmail())
                        .param("password", clientDto.getPassword())
                        .param("card", clientDto.getCard())
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/clients"));

        verify(clientService).update(eq(2L), any(ClientDto.class));
    }

    @Test
    @WithMockUser(roles = "EMPLOYEE")
    void update_withInvalidInput_shouldReturnForm() throws Exception {
        mockMvc.perform(post("/clients/2")
                        .param("name", "")
                        .param("email", "bad")
                        .param("password", "short")
                        .param("card", "")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(view().name("client/editClient"));
    }
}


