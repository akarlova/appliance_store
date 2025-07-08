package com.epam.rd.autocode.assessment.appliances.controller;

import com.epam.rd.autocode.assessment.appliances.aspect.Loggable;
import com.epam.rd.autocode.assessment.appliances.dto.OrdersDto;
import com.epam.rd.autocode.assessment.appliances.service.ApplianceService;
import com.epam.rd.autocode.assessment.appliances.service.ClientService;
import com.epam.rd.autocode.assessment.appliances.service.EmployeeService;
import com.epam.rd.autocode.assessment.appliances.service.OrderService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.support.SessionStatus;

import java.math.BigDecimal;
import java.security.Principal;

@Controller
@RequestMapping("/orders")
public class OrdersController {
    private final OrderService orderService;
    private final ApplianceService applianceService;
    private final ClientService clientService;
    private final EmployeeService employeeService;

    public OrdersController(OrderService orderService,
                            ApplianceService applianceService,
                            ClientService clientService,
                            EmployeeService employeeService) {
        this.orderService = orderService;
        this.applianceService = applianceService;
        this.clientService = clientService;
        this.employeeService = employeeService;
    }

    // LIST OF ORDERS
    @PreAuthorize("hasAnyRole('CLIENT','EMPLOYEE','ADMIN')")
    @GetMapping
    public String list(Model model, Principal principal) {
        model.addAttribute("orders", orderService.findAll(principal));
        return "order/orders";
    }

    // CART
    @Loggable
    @PreAuthorize("hasAnyRole('CLIENT','EMPLOYEE','ADMIN')")
    @GetMapping("/{orderId}/cart")
    public String viewCart(@PathVariable Long orderId, Model model, Principal principal,
                           HttpSession session) {
        OrdersDto order = orderService.findById(orderId, principal);
        session.setAttribute("origOrder_" + orderId, order);
        model.addAttribute("order", order);
        BigDecimal bonus = order.getTotalAmount().multiply(new BigDecimal("0.05"));
        model.addAttribute("bonus", bonus);
        return "order/cart";
    }

    // VIEWING OF THE COMPLETED ORDER
    @PreAuthorize("hasAnyRole('CLIENT','EMPLOYEE','ADMIN')")
    @GetMapping("/{id}")
    public String viewOrder(@PathVariable Long id,
                            Model model,
                            Principal principal) {
        model.addAttribute("order", orderService.findById(id, principal));
        return "order/viewOrder";
    }

    @PreAuthorize("hasAnyRole('CLIENT','EMPLOYEE','ADMIN')")
    @GetMapping("/new")
    public String newForm(Model model, Principal principal) {
        OrdersDto dto = new OrdersDto();
        if ( ((Authentication)principal).getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_EMPLOYEE")) ) {
            model.addAttribute("clients", clientService.findAll());
        } else {
            dto.setClientId( clientService.findByEmail(principal.getName()).getId() );
        }
        model.addAttribute("order", dto);
        return "order/newOrder";
    }

    @PreAuthorize("hasAnyRole('CLIENT','EMPLOYEE','ADMIN')")
    @PostMapping
    public String create(@ModelAttribute("order") @Valid OrdersDto dto,
                         BindingResult br,
                         Principal principal,
                         Model model) {
        if (br.hasErrors()) {
            if ( ((Authentication)principal).getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals("ROLE_EMPLOYEE")) ) {
                model.addAttribute("clients", clientService.findAll());
            }
            return "order/newOrder";
        }
        OrdersDto created = orderService.create(dto, principal);
        return "redirect:/orders/" + created.getId() + "/cart";
    }

    @PreAuthorize("hasRole('CLIENT')")
    @GetMapping("/{orderId}/choice-appliance")
    public String choiceAppliance(@PathVariable Long orderId,
                                  Model model,
                                  Principal principal) {
        OrdersDto order = orderService.findById(orderId, principal);
        model.addAttribute("order", order);
        model.addAttribute("appliances", applianceService.findAll());
        return "order/choiceAppliance";
    }

    // ADD ROW TO CART
    @PreAuthorize("hasAnyRole('CLIENT','EMPLOYEE','ADMIN')")
    @PostMapping("/{orderId}/rows")
    public String addRow(@PathVariable Long orderId,
                         @RequestParam Long applianceId,
                         @RequestParam Long quantity,
                         Principal principal) {
        orderService.addRow(orderId, applianceId, quantity, principal);
        return "redirect:/orders/" + orderId + "/cart";
    }

    //UPDATE CART
    @PreAuthorize("hasAnyRole('CLIENT','EMPLOYEE','ADMIN')")
    @PostMapping("/{orderId}/rows/{rowId}")
    public String updateRow(@PathVariable Long orderId,
                            @PathVariable Long rowId,
                            @RequestParam Long quantity,
                            Principal principal) {
        orderService.updateRow(orderId, rowId, quantity, principal);
        return "redirect:/orders/" + orderId + "/cart";
    }

    //DELETE ROW FROM CART
    @PreAuthorize("hasAnyRole('CLIENT','EMPLOYEE','ADMIN')")
    @GetMapping("/{orderId}/rows/{rowId}/delete")
    public String deleteRow(@PathVariable Long orderId,
                            @PathVariable Long rowId,
                            Principal principal) {
        orderService.deleteRow(orderId, rowId, principal);
        return "redirect:/orders/" + orderId + "/cart";
    }


    // DELETE ORDER
    @PreAuthorize("hasRole('CLIENT')")
    @GetMapping("/{id}/delete")
    public String delete(@PathVariable("id") Long id,
                         Principal principal) {
        orderService.delete(id, principal);
        return "redirect:/orders";
    }

    //APPROVE//UNAPPROVE ORDERS
    @PreAuthorize("hasAnyRole('EMPLOYEE','ADMIN')")
    @GetMapping("/{id}/approve")
    public String approve(@PathVariable("id") Long id,
                          Principal principal) {
        orderService.approve(id, principal);
        return "redirect:/orders";
    }

    @PreAuthorize("hasAnyRole('EMPLOYEE','ADMIN')")
    @GetMapping("/{id}/unapprove")
    public String unapprove(@PathVariable("id") Long id,
                            Principal principal) {
        orderService.unapprove(id, principal);
        return "redirect:/orders";
    }

    // Submit (CLIENT) → thankYou.html
    @PreAuthorize("hasRole('CLIENT')")
    @PostMapping("/{orderId}/confirm")
    public String confirmOrder(@PathVariable Long orderId,
                               Principal principal,
                               Model model,
                               SessionStatus status) {
        orderService.submit(orderId, principal);
        status.setComplete();
        model.addAttribute("message", "orders.message.thankYou");
        return "order/thankYou";
    }

    // Submit (EMPLOYEE/ADMIN) → List of Orders
    @PreAuthorize("hasAnyRole('EMPLOYEE','ADMIN')")
    @PostMapping("/{orderId}/submit")
    public String submitByEmployee(@PathVariable Long orderId,
                                   Principal principal) {
        orderService.submit(orderId, principal);
        return "redirect:/orders";
    }

    @PreAuthorize("hasAnyRole('CLIENT','EMPLOYEE','ADMIN')")
    @GetMapping("/{id}/cancel")
    public String cancel(@PathVariable Long id, Principal principal) {
        orderService.cancelOrder(id, principal);
        return "redirect:/orders";
    }

    @Loggable
    @PreAuthorize("hasAnyRole('CLIENT','EMPLOYEE','ADMIN')")
    @GetMapping("/{orderId}/cart/cancel")
    public String cancelCart(@PathVariable Long orderId,
                             HttpSession session,
                             Principal principal) {
        String sessionKey = "origOrder_" + orderId;
        OrdersDto original = (OrdersDto) session.getAttribute(sessionKey);

        if (original != null) {
            orderService.restoreOrder(orderId, original, principal);
        }

        session.removeAttribute(sessionKey);
        return "redirect:/orders";
    }
    @PreAuthorize("hasAnyRole('EMPLOYEE','ADMIN')")
    @GetMapping("/client/{clientId}/cart")
    public String createOrderForClient(@PathVariable Long clientId,
                                       Principal principal) {
        OrdersDto draft = orderService.getOrCreateDraftForClient(clientId, principal);
        return "redirect:/orders/" + draft.getId() + "/cart";
    }
}
