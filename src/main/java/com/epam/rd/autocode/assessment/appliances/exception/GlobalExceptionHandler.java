package com.epam.rd.autocode.assessment.appliances.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class GlobalExceptionHandler {
    private final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    // 404
    @ExceptionHandler(ResourceNotFoundException.class)
    public String handleNotFound(ResourceNotFoundException ex, Model model) {
        model.addAttribute("status",  "404");
        model.addAttribute("error",   "Not Found");
        model.addAttribute("message", ex.getMessage());
        return "error";
    }

    // 400
    @ExceptionHandler(BadRequestException.class)
    public String handleBadRequest(BadRequestException ex, Model model) {
        model.addAttribute("status",  "400");
        model.addAttribute("error",   "Bad Request");
        model.addAttribute("message", ex.getMessage());
        return "error";
    }

    // all others -> 500
    @ExceptionHandler(Exception.class)
    public String handleInternal(Exception ex, Model model) {
        log.error("Unexpected error", ex);
        model.addAttribute("status",  "500");
        model.addAttribute("error",   "Server Error");
        model.addAttribute("message", "Internal server error, please try again later.");
        return "error";
    }
}
