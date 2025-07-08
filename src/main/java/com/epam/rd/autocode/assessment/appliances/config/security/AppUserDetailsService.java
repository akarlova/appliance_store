package com.epam.rd.autocode.assessment.appliances.config.security;

import com.epam.rd.autocode.assessment.appliances.repository.ClientRepository;
import com.epam.rd.autocode.assessment.appliances.repository.EmployeeRepository;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;


@Service
public class AppUserDetailsService implements UserDetailsService {

    private final ClientRepository clientRepo;
    private final EmployeeRepository empRepo;

    public AppUserDetailsService(ClientRepository clientRepo,
                                 EmployeeRepository empRepo
                                 ) {
        this.clientRepo = clientRepo;
        this.empRepo = empRepo;
    }

    @Override
    public UserDetails loadUserByUsername(String username) {
        return clientRepo.findByEmail(username)
                .map(c -> User.builder()
                        .username(c.getEmail())
                        .password(c.getPassword())
                        .authorities("ROLE_CLIENT")
                        .build())
                .orElseGet(() -> empRepo.findByEmail(username)
                        .map(e -> User.builder()
                                .username(e.getEmail())
                                .password(e.getPassword())
                                .authorities("ROLE_EMPLOYEE")
                                .build())
                        .orElseThrow(() ->
                                new UsernameNotFoundException("User not found: " + username))
                );
    }
}
