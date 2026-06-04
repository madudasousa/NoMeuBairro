package com.meubairro.api.config;

import com.meubairro.api.security.JwtFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
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
public class SecurityConfig {

    private final JwtFilter jwtFilter;

    public SecurityConfig(@Lazy JwtFilter jwtFilter) {
        this.jwtFilter = jwtFilter;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                // Desativa CSRF — não necessário para APIs REST com JWT
                .csrf(AbstractHttpConfigurer::disable)

                // Sem sessão no servidor — cada requisição é autenticada pelo token
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                .authorizeHttpRequests(auth -> auth

                        // ROTAS PÚBLICAS — qualquer um acessa sem login
                        .requestMatchers("/auth/**").permitAll()
                        .requestMatchers("/uploads/**").permitAll()
                        .requestMatchers("/categorias", "/categorias/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/estabelecimentos", "/estabelecimentos/**").permitAll()

                        // Arquivos estáticos do frontend
                        .requestMatchers("/", "/*.html", "/*.js", "/*.css", "/img/**").permitAll()

                        // ROTAS PROTEGIDAS — só DONO pode cadastrar/editar/deletar estabelecimento
                        .requestMatchers(HttpMethod.POST, "/estabelecimentos").hasRole("DONO")
                        .requestMatchers(HttpMethod.PUT, "/estabelecimentos/**").hasRole("DONO")
                        .requestMatchers(HttpMethod.DELETE, "/estabelecimentos/**").hasRole("DONO")
                        .requestMatchers(HttpMethod.POST, "/estabelecimentos/*/imagens").hasRole("DONO")
                        .requestMatchers(HttpMethod.DELETE, "/estabelecimentos/*/imagens/**").hasRole("DONO")

                        // Qualquer outra rota exige login
                        .anyRequest().authenticated()
                )

                // Registra o filtro JWT antes do filtro padrão de autenticação
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
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
}