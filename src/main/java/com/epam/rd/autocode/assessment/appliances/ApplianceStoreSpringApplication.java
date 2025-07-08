package com.epam.rd.autocode.assessment.appliances;

import net.minidev.json.JSONUtil;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@SpringBootApplication
@EnableAspectJAutoProxy
public class ApplianceStoreSpringApplication {

    public static void main(String[] args) {
       SpringApplication.run(ApplianceStoreSpringApplication.class, args);
    }


}
