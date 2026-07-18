package com.financafacil.infrastructure.email;

import com.financafacil.domain.port.out.EmailService;
import com.financafacil.infrastructure.config.AppProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class EmailServiceAdapter implements EmailService {
    private final JavaMailSender mailSender;
    private final AppProperties appProperties;

    @Override
    public void sendPasswordReset(String email, String resetLink) {
        var message = new SimpleMailMessage();
        message.setFrom(appProperties.getMail().getFrom());
        message.setTo(email);
        message.setSubject("Redefinição de senha — Finança Fácil");
        message.setText("Clique no link para redefinir sua senha: " + resetLink + "\n\nO link expira em 1 hora.");
        mailSender.send(message);
        log.info("Password reset email sent to {}", email);
    }

    @Override
    public void sendEmailVerification(String email, String verifyLink) {
        var message = new SimpleMailMessage();
        message.setFrom(appProperties.getMail().getFrom());
        message.setTo(email);
        message.setSubject("Verifique seu e-mail — Finança Fácil");
        message.setText("Clique no link para verificar seu e-mail: " + verifyLink + "\n\nO link expira em 24 horas.");
        mailSender.send(message);
        log.info("Verification email sent to {}", email);
    }
}
