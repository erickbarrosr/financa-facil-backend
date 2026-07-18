package com.financafacil.domain.port.out;

public interface EmailService {
    void sendPasswordReset(String email, String resetLink);
    void sendEmailVerification(String email, String verifyLink);
}
