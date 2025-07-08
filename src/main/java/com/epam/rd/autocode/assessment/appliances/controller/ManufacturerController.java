package com.epam.rd.autocode.assessment.appliances.controller;

import com.epam.rd.autocode.assessment.appliances.dto.ManufacturerDto;
import com.epam.rd.autocode.assessment.appliances.service.ManufacturerService;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/manufacturers")
public class ManufacturerController {
    private final ManufacturerService manufacturerService;

    public ManufacturerController(ManufacturerService manufacturerService) {
        this.manufacturerService = manufacturerService;
    }

    @GetMapping
    public String list(Model model) {
        model.addAttribute("manufacturers", manufacturerService.findAll());
        return "manufacturer/manufacturers";
    }

    @PreAuthorize("hasAnyRole('EMPLOYEE','ADMIN')")
    @GetMapping("/new")
    public String newForm(Model model) {
        model.addAttribute("manufacturer", new ManufacturerDto());
        return "manufacturer/newManufacturer";
    }

    @PreAuthorize("hasAnyRole('EMPLOYEE','ADMIN')")
    @PostMapping
    public String create(@Valid @ModelAttribute("manufacturer") ManufacturerDto dto,
                         BindingResult br) {
        if (br.hasErrors()) {
            return "manufacturer/newManufacturer";
        }
        manufacturerService.create(dto);
        return "redirect:/manufacturers";
    }

    @PreAuthorize("hasAnyRole('EMPLOYEE','ADMIN')")
    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id, Model model) {
        model.addAttribute("manufacturer", manufacturerService.findById(id));
        return "manufacturer/editManufacturer";
    }

    @PreAuthorize("hasAnyRole('EMPLOYEE','ADMIN')")
    @PostMapping("/{id}")
    public String update(@PathVariable Long id,
                         @Valid @ModelAttribute("manufacturer") ManufacturerDto dto,
                         BindingResult br) {
        if (br.hasErrors()) {
            return "manufacturer/editManufacturer";
        }
        manufacturerService.update(id, dto);
        return "redirect:/manufacturers";
    }

    @PreAuthorize("hasAnyRole('EMPLOYEE','ADMIN')")
    @GetMapping("/{id}/delete")
    public String delete(@PathVariable Long id) {
        manufacturerService.delete(id);
        return "redirect:/manufacturers";
    }
}
