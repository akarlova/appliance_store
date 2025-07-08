package com.epam.rd.autocode.assessment.appliances.controller;

import com.epam.rd.autocode.assessment.appliances.dto.ApplianceDto;
import com.epam.rd.autocode.assessment.appliances.exception.ResourceNotFoundException;
import com.epam.rd.autocode.assessment.appliances.model.Appliance;
import com.epam.rd.autocode.assessment.appliances.model.Category;
import com.epam.rd.autocode.assessment.appliances.model.PowerType;
import com.epam.rd.autocode.assessment.appliances.service.ApplianceService;
import com.epam.rd.autocode.assessment.appliances.service.ManufacturerService;
import com.epam.rd.autocode.assessment.appliances.service.OrderService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@Controller
@RequestMapping("/appliances")
public class ApplianceController {
    private final ApplianceService applianceService;
    private final ManufacturerService manufacturerService;
    private final OrderService orderService;

    public ApplianceController(ApplianceService applianceService,
                               ManufacturerService manufacturerService,
                               OrderService orderService) {
        this.applianceService = applianceService;
        this.manufacturerService = manufacturerService;
        this.orderService = orderService;
    }

    @GetMapping
    public String list(
            @RequestParam(value = "orderId", required = false) Long orderId,
            @RequestParam(value = "query", required = false) String query,
            @PageableDefault(size = 10) Pageable pageable,
            Model model,
            Principal principal
    ) {
        Page<Appliance> page;
        if (query != null && !query.isBlank()) {
            page = applianceService.search(query, pageable);
        } else {
            page = applianceService.findAll(pageable);
        }
        model.addAttribute("page", page);
        model.addAttribute("query", query);

        Long currentOrderId = orderId;
        if (principal != null &&
                ((Authentication) principal).getAuthorities().stream()
                        .anyMatch(a -> a.getAuthority().equals("ROLE_CLIENT"))
        ) {
            try {
                if (currentOrderId == null) {
                    currentOrderId = orderService.getOrCreateDraft(principal).getId();
                }
            } catch (ResourceNotFoundException ex) {
            }
        }
        if (currentOrderId != null) {
            model.addAttribute("currentOrderId", currentOrderId);
        }
        return "appliance/appliances";
    }

    @PreAuthorize("hasAnyRole('EMPLOYEE','ADMIN')")
    @GetMapping("/new")
    public String newForm(Model model) {
        model.addAttribute("appliance", new ApplianceDto());
        model.addAttribute("categories", Category.values());
        model.addAttribute("powerTypes", PowerType.values());
        model.addAttribute("manufacturers", manufacturerService.findAll());
        return "appliance/newAppliance";
    }

    @PreAuthorize("hasAnyRole('EMPLOYEE','ADMIN')")
    @PostMapping
    public String create(@Valid @ModelAttribute("appliance") ApplianceDto dto,
                         BindingResult br,
                         Model model) {
        if (br.hasErrors()) {
            model.addAttribute("categories", Category.values());
            model.addAttribute("powerTypes", PowerType.values());
            model.addAttribute("manufacturers", manufacturerService.findAll());
            return "appliance/newAppliance";
        }
        applianceService.create(dto);
        return "redirect:/appliances";
    }

    @PreAuthorize("hasAnyRole('EMPLOYEE','ADMIN')")
    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id, Model model) {
        ApplianceDto dto = applianceService.findById(id);
        model.addAttribute("appliance", dto);
        model.addAttribute("categories", Category.values());
        model.addAttribute("powerTypes", PowerType.values());
        model.addAttribute("manufacturers", manufacturerService.findAll());
        return "appliance/editAppliance";
    }

    @PreAuthorize("hasAnyRole('EMPLOYEE','ADMIN')")
    @PostMapping("/{id}")
    public String update(@PathVariable Long id,
                         @Valid @ModelAttribute("appliance") ApplianceDto dto,
                         BindingResult br,
                         Model model) {
        if (br.hasErrors()) {
            model.addAttribute("categories", Category.values());
            model.addAttribute("powerTypes", PowerType.values());
            model.addAttribute("manufacturers", manufacturerService.findAll());
            return "appliance/editAppliance";
        }
        applianceService.update(id, dto);
        return "redirect:/appliances";
    }

    @PreAuthorize("hasAnyRole('EMPLOYEE','ADMIN')")
    @GetMapping("/{id}/delete")
    public String delete(@PathVariable Long id) {
        applianceService.delete(id);
        return "redirect:/appliances";
    }
}

