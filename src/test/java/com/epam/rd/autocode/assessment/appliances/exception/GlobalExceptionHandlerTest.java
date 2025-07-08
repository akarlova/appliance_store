package com.epam.rd.autocode.assessment.appliances.exception;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.ui.Model;
import org.springframework.validation.support.BindingAwareModelMap;

import static org.junit.jupiter.api.Assertions.*;

class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler handler;
    private Model model;

    @BeforeEach
    void setUp() {
        handler = new GlobalExceptionHandler();
        model = new BindingAwareModelMap();
    }

    @Test
    void handleNotFound_populatesModelAndReturnsErrorView() {
        ResourceNotFoundException ex = new ResourceNotFoundException("X not found");
        String view = handler.handleNotFound(ex, model);

        assertEquals("error", view);
        assertEquals("404", model.getAttribute("status"));
        assertEquals("Not Found", model.getAttribute("error"));
        assertEquals("X not found", model.getAttribute("message"));
    }

    @Test
    void handleBadRequest_populatesModelAndReturnsErrorView() {
        BadRequestException ex = new BadRequestException("Bad input");
        String view = handler.handleBadRequest(ex, model);

        assertEquals("error", view);
        assertEquals("400", model.getAttribute("status"));
        assertEquals("Bad Request", model.getAttribute("error"));
        assertEquals("Bad input", model.getAttribute("message"));
    }

    @Test
    void handleInternal_populatesModelAndReturnsErrorView() {
        Exception ex = new IllegalStateException("boom");
        String view = handler.handleInternal(ex, model);

        assertEquals("error", view);
        assertEquals("500", model.getAttribute("status"));
        assertEquals("Server Error", model.getAttribute("error"));
        assertEquals("Internal server error, please try again later.", model.getAttribute("message"));
    }
}
