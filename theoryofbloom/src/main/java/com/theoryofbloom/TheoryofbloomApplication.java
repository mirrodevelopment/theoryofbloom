package com.theoryofbloom;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.boot.CommandLineRunner;
import com.theoryofbloom.model.User;
import com.theoryofbloom.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import java.time.LocalDateTime;

@SpringBootApplication
public class TheoryofbloomApplication {

	public static void main(String[] args) {
		SpringApplication.run(TheoryofbloomApplication.class, args);
	}

	@Bean
	public CommandLineRunner initAdmin(UserRepository userRepository, PasswordEncoder passwordEncoder) {
		return args -> {
			if (userRepository.findByEmail("admin@theoryofbloom.com").isEmpty()) {
				User admin = new User();
				admin.setFullName("System Admin");
				admin.setEmail("admin@theoryofbloom.com");
				admin.setPassword(passwordEncoder.encode("admin@ToB26"));
				admin.setRole("ROLE_ADMIN");
				admin.setCreatedAt(LocalDateTime.now());
				userRepository.save(admin);
			} else {
				User admin = userRepository.findByEmail("admin@theoryofbloom.com").get();
				boolean modified = false;
				if (!"ROLE_ADMIN".equals(admin.getRole())) {
					admin.setRole("ROLE_ADMIN");
					modified = true;
				}
				// Force update password to new one if it's currently the old default
				if (passwordEncoder.matches("admin", admin.getPassword())) {
					admin.setPassword(passwordEncoder.encode("admin@ToB26"));
					modified = true;
				}
				if (modified) {
					userRepository.save(admin);
				}
			}
		};
	}
}
