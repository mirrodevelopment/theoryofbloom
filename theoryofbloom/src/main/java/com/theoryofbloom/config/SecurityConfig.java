package com.theoryofbloom.config;

import com.theoryofbloom.service.CustomUserDetailsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.security.core.session.SessionRegistryImpl;
import org.springframework.security.web.session.HttpSessionEventPublisher;
import org.springframework.context.ApplicationListener;
import org.springframework.security.authentication.event.AuthenticationSuccessEvent;
import java.util.List;
import org.springframework.security.core.session.SessionInformation;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Autowired
    private CustomUserDetailsService userDetailsService;

    private org.springframework.web.filter.OncePerRequestFilter getCsrfFilter() {
        return new org.springframework.web.filter.OncePerRequestFilter() {
            @Override
            protected void doFilterInternal(
                    @org.springframework.lang.NonNull jakarta.servlet.http.HttpServletRequest request,
                    @org.springframework.lang.NonNull jakarta.servlet.http.HttpServletResponse response,
                    @org.springframework.lang.NonNull jakarta.servlet.FilterChain filterChain)
                    throws jakarta.servlet.ServletException, java.io.IOException {
                org.springframework.security.web.csrf.CsrfToken csrfToken = (org.springframework.security.web.csrf.CsrfToken) request
                        .getAttribute(org.springframework.security.web.csrf.CsrfToken.class.getName());
                if (csrfToken != null) {
                    csrfToken.getToken();
                }
                filterChain.doFilter(request, response);
            }
        };
    }

    @Bean
    public SessionRegistry sessionRegistry() {
        return new SessionRegistryImpl();
    }

    @Bean
    public HttpSessionEventPublisher httpSessionEventPublisher() {
        return new HttpSessionEventPublisher();
    }

    @Bean
    @org.springframework.core.annotation.Order(1)
    @SuppressWarnings("removal")
    public SecurityFilterChain adminFilterChain(HttpSecurity http) throws Exception {
        http.securityMatcher("/admin/**", "/api/admin/**", "/admin-login")
                .addFilterAfter(getCsrfFilter(), org.springframework.security.web.csrf.CsrfFilter.class)
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/admin-login", "/css/**", "/images/**").permitAll()
                        .requestMatchers("/admin/users/**", "/api/admin/users/**").hasRole("ADMIN")
                        .anyRequest().hasAnyRole("ADMIN", "SUB_ADMIN"))
                .formLogin(form -> form
                        .loginPage("/admin-login")
                        .loginProcessingUrl("/admin-login")
                        .successHandler((request, response, authentication) -> {
                            boolean isAdmin = authentication.getAuthorities().stream()
                                    .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
                            if (isAdmin) {
                                response.sendRedirect("/admin");
                            } else {
                                response.sendRedirect("/admin-login?error=unauthorized");
                            }
                        })
                        .failureUrl("/admin-login?error=true")
                        .permitAll())
                .sessionManagement(session -> session
                        .sessionConcurrency(concurrency -> concurrency
                                .maximumSessions(-1)
                                .sessionRegistry(sessionRegistry())))
                .logout(logout -> logout
                        .logoutRequestMatcher(new org.springframework.security.web.util.matcher.AntPathRequestMatcher(
                                "/admin/logout", "GET"))
                        .logoutSuccessUrl("/admin-login?logout")
                        .permitAll());
        return http.build();
    }

    @Bean
    @org.springframework.core.annotation.Order(2)
    @SuppressWarnings("removal")
    public SecurityFilterChain userFilterChain(HttpSecurity http) throws Exception {
        http.addFilterAfter(getCsrfFilter(), org.springframework.security.web.csrf.CsrfFilter.class)
                .csrf(csrf -> csrf.ignoringRequestMatchers(
                        "/cart/add/ajax",
                        "/membership/upgrade/ajax",
                        "/api/upload"
                ))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/", "/home",
                                "/shop", "/shop/**",
                                "/blog", "/blog/**",
                                "/contact",
                                "/login", "/register",
                                "/forgot-password", "/reset-password",
                                "/privacy-policy", "/terms",
                                "/subscribe",
                                "/cart", "/cart/**", "/checkout", "/checkout/**",
                                "/css/**", "/js/**", "/images/**", "/fonts/**", "/favicon.ico", "/error")
                        .permitAll()
                        .requestMatchers("/api/upload").permitAll()
                        .anyRequest().authenticated())
                .formLogin(form -> form
                        .loginPage("/login")
                        .loginProcessingUrl("/login")
                        .successHandler((request, response, authentication) -> {
                            boolean isAdmin = authentication.getAuthorities().stream()
                                    .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
                            if (isAdmin) {
                                response.sendRedirect("/admin");
                            } else {
                                response.sendRedirect("/");
                            }
                        })
                        .failureHandler((request, response, exception) -> {
                            if (exception instanceof org.springframework.security.core.userdetails.UsernameNotFoundException) {
                                response.sendRedirect("/login?error=notfound");
                            } else if (exception instanceof org.springframework.security.authentication.BadCredentialsException) {
                                response.sendRedirect("/login?error=badpass");
                            } else {
                                response.sendRedirect("/login?error");
                            }
                        })
                        .permitAll())
                .sessionManagement(session -> session
                        .sessionConcurrency(concurrency -> concurrency
                                .maximumSessions(-1)
                                .sessionRegistry(sessionRegistry())))
                .logout(logout -> logout
                        .logoutRequestMatcher(new org.springframework.security.web.util.matcher.AntPathRequestMatcher(
                                "/logout", "GET"))
                        .logoutSuccessUrl("/")
                        .permitAll());
        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    @SuppressWarnings("deprecation")
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }

    @Bean
    public ApplicationListener<AuthenticationSuccessEvent> adminSessionControlListener() {
        return event -> {
            boolean isAdmin = event.getAuthentication().getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
            if (isAdmin) {
                Object principal = event.getAuthentication().getPrincipal();
                List<SessionInformation> sessions = sessionRegistry().getAllSessions(principal, false);
                for (SessionInformation session : sessions) {
                    session.expireNow();
                }
            }
        };
    }
}