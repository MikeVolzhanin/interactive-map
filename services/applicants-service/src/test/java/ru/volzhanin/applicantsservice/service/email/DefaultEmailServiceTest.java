package ru.volzhanin.applicantsservice.service.email;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.MailSendException;
import org.springframework.mail.javamail.JavaMailSender;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DefaultEmailServiceTest {

    @Mock private JavaMailSender emailSender;

    @InjectMocks
    private DefaultEmailService emailService;

    @Test
    void sendVerificationEmail_validParams_sendsMessage() throws MessagingException {
        MimeMessage mimeMessage = mock(MimeMessage.class);
        when(emailSender.createMimeMessage()).thenReturn(mimeMessage);

        emailService.sendVerificationEmail("user@student.ru", "Подтверждение", "<p>Код: 123456</p>");

        verify(emailSender).send(mimeMessage);
    }

    @Test
    void sendVerificationEmail_sendFails_throwsMailSendException() throws MessagingException {
        MimeMessage mimeMessage = mock(MimeMessage.class);
        when(emailSender.createMimeMessage()).thenReturn(mimeMessage);
        doThrow(new MailSendException("smtp error"))
            .when(emailSender).send(mimeMessage);

        assertThatThrownBy(() ->
            emailService.sendVerificationEmail("user@student.ru", "Тема", "<p>текст</p>")
        ).isInstanceOf(MailSendException.class);
    }
}
