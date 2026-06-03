package com.meubairro.api.service;

import com.meubairro.api.domain.User.User;
import com.meubairro.api.dto.request.CadastroRequest;
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

    @Transactional
    public void cadastrar(CadastroRequest request){
        String document = limparDocumento(request.documento());
        validarDocumento (document);

        if(repository.existsByDocument(document)){
            throw new RuntimeException("Já existe uam conta com esse CPF/CNPJ.");
        }

        if (request.senha() == null || request.senha().length() < 6) {
            throw new RuntimeException("A senha deve conter pelo menos 6 caracteres.");
        }

        User user = User.builder()
                .name(request.nome().trim())
                .document(document)
                .password(passwordEncoder.encode(request.senha()))
                .perfil(request.perfil())
                .build();
        repository.save(user);
    }

    public LoginResponse login(LoginRequest request){
        String document = limparDocumento(request.documento());

        User user = repository.findByDocument(document)
                .orElseThrow(() -> new BadCredentialsException("CPF/CNPJ ou senha inválidos."));
        if (!passwordEncoder.matches(request.senha(), user.getPassword())) {
            throw new BadCredentialsException("CPF/CNPJ ou senha inválidos.");
        }

        String token = jwtService.gerarToken(user);

        return new LoginResponse(token, user.getId(), user.getName(), user.getPerfil());
    }

    private String limparDocumento(String document){
        return (document == null ? "" : document).replaceAll("[^0-9]", "");
    }

    private void validarDocumento(String document){
        if (document.length() == 11){
            if (!validarCpf(document)){
                throw new RuntimeException("CPF inválido.");
            }
        } else if (document.length() == 14) {
            if (!validarCnpj(document)){
                throw new RuntimeException("CNPJ inválido.");
            }
        }else {
            throw new RuntimeException("Documento inválido. Informe um CPF (11 dígitos) ou CNPJ (14 dígitos).");
        }
    }

    private boolean validarCpf(String cpf) {
        if (cpf.chars().distinct().count() == 1) return false;

        int[] n = cpf.chars().map(c -> c - '0').toArray();

        // Primeiro dígito
        int soma = 0;
        for (int i = 0; i < 9; i++) soma += n[i] * (10 - i);
        int resto = soma % 11;
        int primeiro = resto < 2 ? 0 : 11 - resto; // ← era % 10, correto é < 2
        if (primeiro != n[9]) return false;

        // Segundo dígito
        soma = 0;
        for (int i = 0; i < 10; i++) soma += n[i] * (11 - i);
        resto = soma % 11;
        int segundo = resto < 2 ? 0 : 11 - resto; // ← mesmo erro aqui
        return segundo == n[10];
    }

    private boolean validarCnpj(String cnpj){
        if (cnpj.chars().distinct().count() == 1) return false;

        int[] numeros = cnpj.chars().map(c -> c - '0').toArray();
        int[] peso1 = {5, 4, 3, 2, 9, 8, 7, 6, 5, 4, 3, 2};
        int[] peso2 = {6, 5, 4, 3, 2, 9, 8, 7, 6, 5, 4, 3, 2};

        int soma = 0;
        for (int i = 0; i < 12; i++) soma += numeros[i] * peso1[i];
        int primeiro = soma % 11 < 2 ? 0 : 11 - soma % 11;
        if (primeiro != numeros[12]) return false;

        soma = 0;
        for (int i = 0; i < 13; i++) soma += numeros[i] * peso2[i];
        int segundo = soma % 11 < 2 ? 0 : 11 - soma % 11;
        return segundo == numeros[13];
    }
}
