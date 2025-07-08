package com.epam.rd.autocode.assessment.appliances.config.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;


@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    private final AppUserDetailsService appUserDetailsService;
    private final CustomAuthenticationFailureHandler failureHandler;
    private final CustomAuthenticationSuccessHandler successHandler;

    public SecurityConfig(AppUserDetailsService appUserDetailsService,
                          CustomAuthenticationFailureHandler failureHandler,
                          CustomAuthenticationSuccessHandler successHandler
    ) {
        this.appUserDetailsService = appUserDetailsService;
        this.failureHandler = failureHandler;
        this.successHandler = successHandler;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public DaoAuthenticationProvider dbAuthProvider(PasswordEncoder pw) {
        DaoAuthenticationProvider p = new DaoAuthenticationProvider();
        p.setUserDetailsService(appUserDetailsService);
        p.setPasswordEncoder(pw);
        return p;
    }

    @Bean
    public InMemoryUserDetailsManager inMemoryAuth(PasswordEncoder pw) {
        UserDetails adm = User.withUsername("admin")
                .password(pw.encode("password"))
                .roles("ADMIN")
                .build();
        return new InMemoryUserDetailsManager(adm);
    }

    @Bean
    public DaoAuthenticationProvider memAuthProvider(InMemoryUserDetailsManager users,
                                                     PasswordEncoder pw) {
        DaoAuthenticationProvider p = new DaoAuthenticationProvider();
        p.setUserDetailsService(users);
        p.setPasswordEncoder(pw);
        return p;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http,
                                           DaoAuthenticationProvider dbAuthProvider,
                                           DaoAuthenticationProvider memAuthProvider
    ) throws Exception {

        http
                .csrf(csrf -> csrf.ignoringRequestMatchers("/h2-console/**"))
                .headers(headers -> headers
                        .frameOptions(frame -> frame.sameOrigin())
                )
                .authenticationProvider(dbAuthProvider)
                .authenticationProvider(memAuthProvider)
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.GET,
                                "/h2-console/**",
                                "/", "/index",
                                "/login",
                                "/error",
                                "/clients/register",
                                "/images/**",
                                "/css/**",
                                "/js/**",
                                "/appliances/**",
                                "/manufacturers/**"
                        ).permitAll()
                        .requestMatchers(HttpMethod.POST, "/clients/register").permitAll()
                        .requestMatchers(HttpMethod.POST, "/login").permitAll()
                        .requestMatchers("/orders/**")
                        .hasAnyRole("CLIENT", "EMPLOYEE", "ADMIN")
                        .requestMatchers(HttpMethod.GET, "/clients/profile")
                        .hasRole("CLIENT")
                        .requestMatchers("/clients/**").hasAnyRole("EMPLOYEE", "ADMIN")
                        .requestMatchers("/employees/**").hasRole("ADMIN")
                        .anyRequest().authenticated()
                )
                .formLogin(form -> form
                        .loginPage("/login")
                        .loginProcessingUrl("/login")
                        .failureHandler(failureHandler)
                        .successHandler(successHandler)
                        .permitAll()

                )
                .logout(Customizer.withDefaults());
        return http.build();
    }

    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration cfg) throws Exception {
        return cfg.getAuthenticationManager();
    }
}
