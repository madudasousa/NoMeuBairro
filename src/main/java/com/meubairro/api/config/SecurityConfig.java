package com.meubairro.api.config;

import com.meubairro.api.security.JwtFilter;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    private final JwtFilter jwtFilter;

    public SecurityConfig(@Lazy JwtFilter jwtFilter) {
        this.jwtFilter = jwtFilter;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) {
        return http
                // Desativa CSRF — não necessário para APIs REST com JWT
                .csrf(AbstractHttpConfigurer::disable)

                // Sem sessão no servidor — cada requisição é autenticada pelo token
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/usuarios/login").permitAll()
                        .requestMatchers("/usuarios/registro").permitAll()
                        .requestMatchers("/uploads/**").permitAll()
                        .requestMatchers("/categorias", "/categorias/**").permitAll()
                        .requestMatchers("/estabelecimentos/admin/**").hasAuthority("ROLE_ADM")
                        .requestMatchers(HttpMethod.GET, "/estabelecimentos", "/estabelecimentos/**").permitAll()
                        // Cadastro de estab é público — cria dono + estab de uma vez
                        .requestMatchers(HttpMethod.POST, "/estabelecimentos").permitAll()

                        // Arquivos estáticos do frontend
                        .requestMatchers("/", "/*.html", "/*.js", "/*.css", "/img/**").permitAll()

                        // Qualquer outra rota exige login
                        .anyRequest().authenticated()
                )
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }

    // BCrypt é o algoritmo de criptografia de senha recomendado
    // Ele gera um hash diferente a cada vez, dificultando ataques
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) {
        return config.getAuthenticationManager();
    }
}