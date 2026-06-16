package com.meubairro.api.controller;

import com.meubairro.api.domain.User;
import com.meubairro.api.dto.request.ChangePasswordRequest;
import com.meubairro.api.dto.request.LoginRequest;
import com.meubairro.api.dto.request.CadastroRequest;
import com.meubairro.api.dto.response.EstabResponse;
import com.meubairro.api.dto.response.LoginResponse;
import com.meubairro.api.service.EstabService;
import com.meubairro.api.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/usuarios")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final EstabService estabService;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        try {
            LoginResponse response = userService.login(request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(401).body(Map.of("erro", e.getMessage()));
        }
    }

    @PostMapping("/registro")
    public ResponseEntity<?> registro(@RequestBody CadastroRequest request) {
        try {
            LoginResponse response = userService.registrar(request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("erro", e.getMessage()));
        }
    }

    @GetMapping("/minha-loja")
    @PreAuthorize("hasRole('DONO')")
    public ResponseEntity<?> minhaLoja(@AuthenticationPrincipal User user){
        if (user.getEstab() == null){
            return ResponseEntity.notFound().build();
        }
        EstabResponse response = estabService.buscarPorId(user.getEstab().getId());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/trocar-senha")
    @PreAuthorize("hasAnyRole('DONO', 'ADM')")
    public ResponseEntity<?> trocarSenha(@AuthenticationPrincipal User user, @RequestBody ChangePasswordRequest request){
        try {
            userService.trocarSenha(user.getId(), request);
            return ResponseEntity.ok(Map.of("mensagem", "Senha alterada com sucesso."));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("erro", e.getMessage()));
        }
    }
}