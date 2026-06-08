package com.meubairro.api.dto.request;

public record ChangePasswordRequest(
        String password,
        String newpassword,
        String confirmNewPassword
) { }
