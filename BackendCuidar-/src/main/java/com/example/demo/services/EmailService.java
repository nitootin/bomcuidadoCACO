package com.example.demo.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import com.example.demo.exceptions.EmailSendingException;

import jakarta.mail.internet.MimeMessage;

@Service
public class EmailService {

    private static final Logger logger = LoggerFactory.getLogger(EmailService.class);

    private final JavaMailSender mailSender;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void enviarCodigoVerificacao(String destinatario, String codigo) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom("secretariabomcuidado@gmail.com");
            helper.setTo(destinatario);
            helper.setSubject("Código de verificação - Bom Cuidado");
            helper.setText(construirHtml(codigo), true);

            mailSender.send(message);
        } catch (Exception e) {
            logger.error("Falha ao enviar codigo de verificacao para {}", destinatario, e);
            throw new EmailSendingException();
        }
    }

    private String construirHtml(String codigo) {
        return """
                <!DOCTYPE html>
                <html>
                <body style="font-family: Arial, sans-serif; background-color: #f4f4f4; padding: 20px;">
                  <div style="max-width: 500px; margin: auto; background: white; border-radius: 8px; padding: 32px;">
                    <h2 style="color: #2e7d32;">Bom Cuidado</h2>
                    <p>Seu código de verificação é:</p>
                    <div style="font-size: 36px; font-weight: bold; letter-spacing: 8px; color: #2e7d32; margin: 24px 0;">
                      %s
                    </div>
                    <p style="color: #666;">Este código expira em <strong>10 minutos</strong>.</p>
                    <p style="color: #999; font-size: 12px;">Se você não solicitou este código, ignore este email.</p>
                  </div>
                </body>
                </html>
                """.formatted(codigo);
    }
}
