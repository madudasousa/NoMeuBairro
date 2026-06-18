package com.meubairro.api.security;

import com.meubairro.api.service.JwtService;
import com.meubairro.api.service.UserService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class JwtFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserService userService;

    public JwtFilter(JwtService jwtService, @Lazy UserService userService) {
        this.jwtService = jwtService;
        this.userService = userService;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        String authHeader = request.getHeader("Authorization");

        // DEBUG TEMPORÁRIO —
        System.out.println("=== JWT FILTER DEBUG ===");
        System.out.println("URI: " + request.getRequestURI());
        System.out.println("Authorization header: " + authHeader);

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            String documento = jwtService.validarToken(token);

            System.out.println("Documento extraido do token: '" + documento + "'");

            // Se o token for válido e ainda não houver autenticação na sessão
            if (!documento.isEmpty() && SecurityContextHolder.getContext().getAuthentication() == null) {
                try {
                    UserDetails user = userService.loadUserByUsername(documento);

                    System.out.println("Usuario carregado: " + user.getUsername());
                    System.out.println("Authorities: " + user.getAuthorities());

                    // Registra o usuário como autenticado no contexto do Spring Security
                    UsernamePasswordAuthenticationToken auth =
                            new UsernamePasswordAuthenticationToken(
                                    user, null, user.getAuthorities());
                    SecurityContextHolder.getContext().setAuthentication(auth);

                    System.out.println("Autenticacao definida no SecurityContext com sucesso!");

                } catch (Exception e) {
                    System.out.println("ERRO ao carregar usuario: " + e.getMessage());
                    e.printStackTrace();
                }
            } else {
                System.out.println("Documento vazio ou ja autenticado anteriormente.");
            }
        } else {
            System.out.println("Sem header Authorization ou nao comeca com 'Bearer '");
        }

        System.out.println("========================");

        // Continua a requisição para o próximo filtro ou controller
        filterChain.doFilter(request, response);
    }
}