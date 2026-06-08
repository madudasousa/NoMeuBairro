package com.meubairro.api.service;

import com.meubairro.api.domain.User;
import com.meubairro.api.dto.request.ChangePasswordRequest;
import com.meubairro.api.dto.request.LoginRequest;
import com.meubairro.api.dto.response.LoginResponse;
import com.meubairro.api.repositories.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserService implements UserDetailsService {

    private final UserRepository repository;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;

    @Override
    public UserDetails loadUserByUsername(String document) throws UsernameNotFoundException {
        return repository.findByDocument(document)
                .orElseThrow(() -> new UsernameNotFoundException("Usuário não encontrado: " + document));
    }

    public LoginResponse login(LoginRequest request){
        String document = limparDocumento(request.documento());

        User user = repository.findByDocument(document)
                .orElseThrow(() -> new BadCredentialsException("CPF/CNPJ ou senha inválidos."));
        if (!passwordEncoder.matches(request.senha(), user.getPassword())) {
            throw new BadCredentialsException("CPF/CNPJ ou senha inválidos.");
        }

        if (user.getEstab() != null) {
            if (Boolean.FALSE.equals(user.getEstab().getActiveAdmin())) {
                throw new BadCredentialsException(
                        "Seu estabelecimento foi desativado pelo administrador. Entre em contato para mais informações."
                );
            }
        }
        String token = jwtService.gerarToken(user);

        return new LoginResponse(token, user.getId(), user.getName(), user.getPerfil());
    }

    @Transactional
    public void trocarSenha (UUID userId, ChangePasswordRequest request){
        User user = repository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado."));

        if (!passwordEncoder.matches(request.password(), user.getPassword())) {
            throw new RuntimeException("Senha atual incorreta.");
        }

        if (!request.newpassword().equals(request.confirmNewPassword())) {
            throw new RuntimeException("A nova senha e a confirmação não coincidem.");
        }

        if (request.newpassword().length() < 6) {
            throw new RuntimeException("A nova senha deve conter pelo menos 6 caracteres.");
        }

        user.setPassword(passwordEncoder.encode(request.newpassword()));
        repository.save(user);
    }

    public User buscarId(UUID id){
        return repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado."));
    }

    private String limparDocumento(String document){
        return (document == null ? "" : document).replaceAll("[^0-9]", "");
    }
}
