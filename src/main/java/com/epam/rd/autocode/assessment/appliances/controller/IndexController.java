package com.epam.rd.autocode.assessment.appliances.controller;

import com.epam.rd.autocode.assessment.appliances.dto.OrdersDto;
import com.epam.rd.autocode.assessment.appliances.exception.ResourceNotFoundException;
import com.epam.rd.autocode.assessment.appliances.model.Appliance;
import com.epam.rd.autocode.assessment.appliances.service.ApplianceService;
import com.epam.rd.autocode.assessment.appliances.service.ClientService;
import com.epam.rd.autocode.assessment.appliances.service.EmployeeService;
import com.epam.rd.autocode.assessment.appliances.service.OrderService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class IndexController {
    private final ApplianceService applianceService;
    private final ClientService clientService;
    private final EmployeeService employeeService;
    private final OrderService orderService;

    public IndexController(ApplianceService applianceService,
                           ClientService clientService,
                           EmployeeService employeeService,
                           OrderService orderService) {
        this.applianceService = applianceService;
        this.clientService = clientService;
        this.employeeService = employeeService;
        this.orderService = orderService;
    }

    @GetMapping({"/", "/index"})
    public String index(
            @RequestParam(required = false) String query,
            @PageableDefault(size = 4) Pageable pageable,
            Model model,
            Authentication auth
    ) {
        Page<Appliance> appliancePage;
        if (query != null && !query.isBlank()) {
            appliancePage = applianceService.search(query, pageable);
        } else {
            appliancePage = applianceService.findAll(pageable);
        }
        model.addAttribute("appliancePage", appliancePage);
        model.addAttribute("query", query);

        if (auth != null
                && auth.isAuthenticated()
                && auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_CLIENT"))
        ) {
            try {
                OrdersDto draft = orderService.getOrCreateDraft(auth);
                model.addAttribute("currentOrderId", draft.getId());
            } catch (ResourceNotFoundException ex) {
            }
        }

        if (auth != null
                && auth.isAuthenticated()
                && auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_EMPLOYEE")
                        || a.getAuthority().equals("ROLE_ADMIN"))
        ) {
            model.addAttribute("totalClients",   clientService.findAll().size());
            model.addAttribute("totalEmployees", employeeService.findAll().size());
            model.addAttribute("totalOrders",    orderService.findAll(null).size());
        }

        return "index";
    }
}
