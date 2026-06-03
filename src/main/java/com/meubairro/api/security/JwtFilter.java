package com.meubairro.api.security;

import com.meubairro.api.service.JwtService;
import com.meubairro.api.service.UserService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class JwtFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserService userService;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        String authHeader = request.getHeader("Authorization");

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            String documento = jwtService.validarToken(token);

            // Se o token for válido e ainda não houver autenticação na sessão
            if (!documento.isEmpty() && SecurityContextHolder.getContext().getAuthentication() == null) {
                try {
                    UserDetails user = userService.loadUserByUsername(documento);

                    // Registra o usuário como autenticado no contexto do Spring Security
                    UsernamePasswordAuthenticationToken auth =
                            new UsernamePasswordAuthenticationToken(
                                    user, null, user.getAuthorities()
                            );
                    SecurityContextHolder.getContext().setAuthentication(auth);
                } catch (Exception e) {
                    // Se o usuário não for encontrado ou ocorrer erro, não autentica
                    // e deixa a requisição seguir como não autenticada
                }
            }
        }

        // Continua a requisição para o próximo filtro ou controller
        filterChain.doFilter(request, response);
    }
}