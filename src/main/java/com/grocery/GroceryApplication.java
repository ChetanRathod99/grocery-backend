package com.grocery;

import com.grocery.entity.Role;
import com.grocery.entity.RoleName;
import com.grocery.repository.RoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
@RequiredArgsConstructor
public class GroceryApplication {
    public static void main(String[] args) {
        SpringApplication.run(GroceryApplication.class, args);
    }

    @Bean
    CommandLineRunner seedRoles(RoleRepository roles) {
        return args -> {
            for (RoleName name : RoleName.values()) {
                roles.findByName(name).orElseGet(() -> roles.save(new Role(null, name)));
            }
        };
    }
}
