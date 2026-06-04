package com.meubairro.api.controller;

import com.meubairro.api.dto.request.CadastroRequest;
import com.meubairro.api.dto.request.LoginRequest;
import com.meubairro.api.dto.response.LoginResponse;
import com.meubairro.api.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;

    // Autentica o usuário e retorna o token JWT
    // POST /auth/login
    // Body: { "documento": "12345678909", "senha": "123456" }
    // Retorna: { "token": "eyJ...", "id": "uuid", "nome": "João", "perfil": "CLIENTE" }
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        try {
            LoginResponse response = userService.login(request);
            return ResponseEntity.ok(response);
        } catch (BadCredentialsException e) {
            return ResponseEntity.status(401).body(Map.of("erro", e.getMessage()));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("erro", e.getMessage()));
        }
    }
}