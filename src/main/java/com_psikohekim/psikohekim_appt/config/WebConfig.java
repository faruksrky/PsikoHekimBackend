package com_psikohekim.psikohekim_appt.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
@Configuration
public class WebConfig {

    /**
     * CORS sadece Nginx ekliyor (api.iyihislerapp.com). Backend CORS kapalı - duplicate header önlenir.
     * Local dev: Frontend api.iyihislerapp.com kullanıyorsa nginx yok, .env ile localhost:8083 kullan.
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(authorizeRequests ->
                        authorizeRequests
                                .requestMatchers(
                                        "/actuator/**",
                                        "/api/google-calendar/**",
                                        "/api/calendar/**",
                                        "/favicon.ico",
                                        "/public/**",
                                        "/therapist/**",
                                        "/patient/**",
                                        "/therapist-patient/**",
                                        "/therapy-sessions/**",
                                        "/pricing/**",
                                        "/finance/**",
                                        "/process/send-assignment-request",
                                        "/process/**",
                                        "/api/bpmn/**"
                                ).permitAll()
                                .anyRequest().authenticated()
                );

        return http.build();
    }

    @Bean
    public WebMvcConfigurer webMvcConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addResourceHandlers(ResourceHandlerRegistry registry) {
                registry.addResourceHandler("/favicon.ico")
                        .addResourceLocations("classpath:/static/")
                        .setCachePeriod(3600 * 24);
            }
        };
    }

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}