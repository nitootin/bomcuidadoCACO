package com.example.demo.services;

import org.springframework.stereotype.Service;

import com.example.demo.exceptions.PasswordPolicyException;

@Service
public class SenhaService {

    private static final String REGEX_SENHA_FORTE =
            "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[^A-Za-z0-9]).{8,}$";

    public void validar(String senha) {
        if (senha == null || !senha.matches(REGEX_SENHA_FORTE)) {
            throw new PasswordPolicyException();
        }
    }
}
