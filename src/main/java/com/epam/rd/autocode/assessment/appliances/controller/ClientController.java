package com.epam.rd.autocode.assessment.appliances.controller;

import com.epam.rd.autocode.assessment.appliances.dto.ClientDto;
import com.epam.rd.autocode.assessment.appliances.dto.ClientProfileDto;
import com.epam.rd.autocode.assessment.appliances.dto.ClientProfileUpdateDto;
import com.epam.rd.autocode.assessment.appliances.dto.ClientRegistrationDto;
import com.epam.rd.autocode.assessment.appliances.service.ClientService;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@Controller
@RequestMapping("/clients")
public class ClientController {

    private final ClientService service;

    public ClientController(ClientService service) {
        this.service = service;
    }

    @GetMapping("/register")
    public String showRegisterForm(Model model) {
        model.addAttribute("registration", new ClientRegistrationDto());
        return "client/register";
    }

    @PostMapping("/register")
    public String doRegister(
            @Valid @ModelAttribute("registration") ClientRegistrationDto dto,
            BindingResult br
    ) {
        if (br.hasErrors()) {
            return "client/register";
        }
        service.register(dto);
        return "redirect:/login?registered";
    }

    @PreAuthorize("hasRole('CLIENT')")
    @GetMapping("/profile")
    public String profileForm(Model model, Principal principal) {
        ClientProfileDto display = service.profileByEmail(principal.getName());
        ClientProfileUpdateDto form = new ClientProfileUpdateDto();
        form.setName(display.getName());
        form.setEmail(display.getEmail());
        form.setCard(display.getCard());
        model.addAttribute("profile", form);
        return "client/profile";
    }

    @PreAuthorize("hasRole('CLIENT')")
    @PostMapping("/profile")
    public String updateProfile(
            @Valid @ModelAttribute("profile") ClientProfileUpdateDto dto,
            BindingResult br,
            Principal principal
    ) {
        if (br.hasErrors()) {
            return "client/profile";
        }
        service.updateProfileByEmail(principal.getName(), dto);
        return "redirect:/clients/profile?success";
    }

    @PreAuthorize("hasAnyRole('EMPLOYEE','ADMIN')")
    @GetMapping
    public String list(Model model) {
        model.addAttribute("clients", service.findAll());
        return "client/clients";
    }

    @PreAuthorize("hasAnyRole('EMPLOYEE','ADMIN')")
    @GetMapping("/new")
    public String newForm(Model model) {
        model.addAttribute("client", new ClientDto());
        return "client/newClient";
    }

    @PreAuthorize("hasAnyRole('EMPLOYEE','ADMIN')")
    @PostMapping
    public String create(
            @Valid @ModelAttribute("client") ClientDto dto,
            BindingResult br
    ) {
        if (dto.getPassword().isBlank()) {
            br.rejectValue("password", "user.password.is.blank");
        }
        if (br.hasErrors()) {
            return "client/newClient";
        }
        service.create(dto);
        return "redirect:/clients";
    }

    @PreAuthorize("hasAnyRole('EMPLOYEE','ADMIN')")
    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id, Model model) {
        model.addAttribute("client", service.findById(id));
        return "client/editClient";
    }

    @PreAuthorize("hasAnyRole('EMPLOYEE','ADMIN')")
    @PostMapping("/{id}")
    public String update(
            @PathVariable Long id,
            @Valid @ModelAttribute("client") ClientDto dto,
            BindingResult br
    ) {
        dto.setId(id);
        if (br.hasFieldErrors("password")) {
            br.getFieldErrors("password").clear();
        }
        if (br.hasErrors()) {
            return "client/editClient";
        }
        service.update(id, dto);
        return "redirect:/clients";
    }

    @PreAuthorize("hasAnyRole('EMPLOYEE','ADMIN')")
    @GetMapping("/{id}/delete")
    public String delete(@PathVariable Long id) {
        service.delete(id);
        return "redirect:/clients";
    }
}
