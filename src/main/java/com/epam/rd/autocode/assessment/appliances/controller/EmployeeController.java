package com.epam.rd.autocode.assessment.appliances.controller;

import com.epam.rd.autocode.assessment.appliances.dto.EmployeeDto;
import com.epam.rd.autocode.assessment.appliances.service.EmployeeService;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/employees")
@PreAuthorize("hasRole('ADMIN')")
public class EmployeeController {
    private final EmployeeService service;

    public EmployeeController(EmployeeService service) {
        this.service = service;
    }

    /** 1) LIST all (ADMIN only) */
    @GetMapping
    public String list(Model model) {
        model.addAttribute("employees", service.findAll());
        return "employee/employees";
    }

    /** 2) NEW form (ADMIN only) */
    @GetMapping("/new")
    public String newForm(Model model) {
        model.addAttribute("employee", new EmployeeDto());
        return "employee/newEmployee";
    }

    /** 3) CREATE (ADMIN only) */
    @PostMapping
    public String create(
            @Valid @ModelAttribute("employee") EmployeeDto dto,
            BindingResult br
    ) {
        if (dto.getPassword() == null || dto.getPassword().isBlank()) {
            br.rejectValue("password", "user.password.is.blank");
        }
        if (br.hasErrors()) {
            return "employee/newEmployee";
        }
        service.create(dto);
        return "redirect:/employees";
    }

    /** 4) EDIT form (ADMIN only) */
    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id, Model model) {
        model.addAttribute("employee", service.findById(id));
        return "employee/editEmployee";
    }

    /** 5) UPDATE (ADMIN only) */
    @PostMapping("/{id}")
    public String update(
            @PathVariable Long id,
            @Valid @ModelAttribute("employee") EmployeeDto dto,
            BindingResult br
    ) {
        dto.setId(id);
        if (br.hasFieldErrors("password")) {
            br.getFieldErrors("password").clear();
        }
        if (br.hasErrors()) {
            return "employee/editEmployee";
        }
        service.update(id, dto);
        return "redirect:/employees";
    }

    /** 6) DELETE (ADMIN only) */
    @GetMapping("/{id}/delete")
    public String delete(@PathVariable Long id) {
        service.delete(id);
        return "redirect:/employees";
    }
}
