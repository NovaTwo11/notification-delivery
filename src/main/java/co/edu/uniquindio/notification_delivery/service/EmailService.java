package co.edu.uniquindio.notification_delivery.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Slf4j
@Service
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.from:noreply@tuapp.com}")
    private String fromEmail;

    // 🔥 Ahora usamos las URLs del BACKEND
    @Value("${app.backend.reset-password-url}")
    private String resetPasswordUrl;

    @Value("${app.backend.login-url}")
    private String loginUrl;

    @Value("${app.name:MiApp}")
    private String appName;

    @Value("${app.support-email:soporte@tuapp.com}")
    private String supportEmail;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void sendPasswordResetEmail(String toEmail, String resetToken) {
        String resetLink = resetPasswordUrl + "?email=" + toEmail + "&token=" + resetToken;

        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromEmail);
        message.setTo(toEmail);
        message.setSubject("🔐 Recuperación de Contraseña - " + appName);
        message.setText(String.format("""
            Hola,

            Has solicitado restablecer tu contraseña en %s.

            Haz clic en el siguiente enlace para continuar:
            %s

            Este enlace te llevará a una página donde podrás establecer tu nueva contraseña.
            El enlace expirará en 24 horas por seguridad.

            Si no solicitaste este cambio, ignora este mensaje.

            ---
            Equipo de %s
            Soporte: %s
            """, appName, resetLink, appName, supportEmail));

        try {
            mailSender.send(message);
            log.info("✅ Email de reset enviado a: {} con link: {}", toEmail, resetLink);
        } catch (Exception e) {
            log.error("❌ Error enviando email de reset a {}: {}", toEmail, e.getMessage(), e);
        }
    }

    public void sendWelcomeEmail(String toEmail, String userName) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromEmail);
        message.setTo(toEmail);
        message.setSubject("🎉 ¡Bienvenido a " + appName + "!");
        message.setText(String.format("""
                Hola %s,

                ¡Bienvenido a %s! Tu cuenta ha sido creada exitosamente.

                Ya puedes iniciar sesión en:
                %s

                Si tienes alguna pregunta, no dudes en contactarnos.

                ---
                Equipo de %s
                Soporte: %s
                """, userName, appName, loginUrl, appName, supportEmail));

        try {
            mailSender.send(message);
            log.info("✅ Email de bienvenida enviado a: {} ({})", toEmail, userName);
        } catch (Exception e) {
            log.error("❌ Error enviando email de bienvenida a {}: {}", toEmail, e.getMessage(), e);
        }
    }

    // Nuevo método para bienvenida con token de activación
    public void sendWelcomeEmailWithActivation(String toEmail, String userName, String activationToken) {
        String activationLink = loginUrl + "/activate?token=" + activationToken;

        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromEmail);
        message.setTo(toEmail);
        message.setSubject("🎉 ¡Bienvenido a " + appName + "! Confirma tu cuenta");
        message.setText(String.format("""
                Hola %s,

                ¡Bienvenido a %s! Para activar tu cuenta, por favor haz clic en el siguiente enlace:

                %s

                Si no creaste esta cuenta, puedes ignorar este correo.

                ---
                Equipo de %s
                Soporte: %s
                """, userName, appName, activationLink, appName, supportEmail));

        try {
            mailSender.send(message);
            log.info("✅ Email de bienvenida con activación enviado a: {} ({})", toEmail, userName);
        } catch (Exception e) {
            log.error("❌ Error enviando email de bienvenida con activación a {}: {}", toEmail, e.getMessage(), e);
        }
    }

    // Nuevo método para notificación de login con datos de seguridad
    public void sendLoginNotification(String toEmail, String userName, String ipAddress, String userAgent, String loginTime) {
        String subject = "🔔 Nuevo inicio de sesión - " + appName;
        String time = (loginTime != null && !loginTime.isEmpty()) ? loginTime : LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss"));

        String body = String.format("""
                Hola %s,

                Se ha detectado un nuevo inicio de sesión en tu cuenta de %s.

                Fecha y hora: %s
                Dirección IP: %s
                Dispositivo/Navegador: %s

                Si fuiste tú, puedes ignorar este mensaje.
                Si NO fuiste tú, cambia tu contraseña inmediatamente usando este enlace:

                %s

                ---
                Equipo de Seguridad de %s
                Soporte: %s
                """, userName, appName, time, ipAddress, userAgent, resetPasswordUrl, appName, supportEmail);

        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromEmail);
        message.setTo(toEmail);
        message.setSubject(subject);
        message.setText(body);

        try {
            mailSender.send(message);
            log.info("✅ Notificación de login enviada a: {} ({})", toEmail, userName);
        } catch (Exception e) {
            log.error("❌ Error enviando notificación de login a {}: {}", toEmail, e.getMessage(), e);
        }
    }

    public void sendPasswordUpdatedConfirmation(String toEmail, String userName) {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss"));

        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromEmail);
        message.setTo(toEmail);
        message.setSubject("✅ Contraseña actualizada - " + appName);
        message.setText(String.format("""
                Hola %s,

                Tu contraseña en %s ha sido actualizada exitosamente.

                Fecha y hora: %s

                Si NO realizaste este cambio, contacta inmediatamente a soporte.

                Puedes iniciar sesión con tu nueva contraseña en:
                %s

                ---
                Equipo de Seguridad de %s
                Soporte: %s
                """, userName, appName, timestamp, loginUrl, appName, supportEmail));

        try {
            mailSender.send(message);
            log.info("✅ Confirmación de password actualizado enviada a: {} ({})", toEmail, userName);
        } catch (Exception e) {
            log.error("❌ Error enviando confirmación de password a {}: {}", toEmail, e.getMessage(), e);
        }
    }
}