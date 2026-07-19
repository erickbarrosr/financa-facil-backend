package com.financafacil.infrastructure.email;

import com.financafacil.infrastructure.config.AppProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmailServiceAdapterTest {

    @Mock
    JavaMailSender mailSender;

    @Mock
    AppProperties appProperties;

    @Mock
    AppProperties.Mail mailProperties;

    @InjectMocks
    EmailServiceAdapter emailServiceAdapter;

    @BeforeEach
    void setUp() {
        when(appProperties.getMail()).thenReturn(mailProperties);
        when(mailProperties.getFrom()).thenReturn("noreply@financafacil.com");
    }

    @Test
    void sendPasswordReset_sendsEmailWithCorrectContent() {
        emailServiceAdapter.sendPasswordReset("user@test.com", "https://example.com/reset?token=abc");

        var captor = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(mailSender).send(captor.capture());

        var message = captor.getValue();
        assertThat(message.getTo()).containsExactly("user@test.com");
        assertThat(message.getFrom()).isEqualTo("noreply@financafacil.com");
        assertThat(message.getSubject()).contains("senha");
        assertThat(message.getText()).contains("https://example.com/reset?token=abc");
    }

    @Test
    void sendEmailVerification_sendsEmailWithCorrectContent() {
        emailServiceAdapter.sendEmailVerification("verify@test.com", "https://example.com/verify?token=xyz");

        var captor = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(mailSender).send(captor.capture());

        var message = captor.getValue();
        assertThat(message.getTo()).containsExactly("verify@test.com");
        assertThat(message.getFrom()).isEqualTo("noreply@financafacil.com");
        assertThat(message.getSubject()).contains("e-mail");
        assertThat(message.getText()).contains("https://example.com/verify?token=xyz");
    }
}
