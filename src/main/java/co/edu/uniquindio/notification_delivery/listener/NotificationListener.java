package co.edu.uniquindio.notification_delivery.listener;

import co.edu.uniquindio.notification_delivery.service.EmailService;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class NotificationListener {

    private final EmailService emailService;

    public NotificationListener(EmailService emailService) {
        this.emailService = emailService;
    }

    /**
     * 🔥 LISTENER ÚNICO que recibe TODOS los eventos desde el Orchestrator
     * El Orchestrator ya procesó y transformó los eventos de dominio en NotificationEvents
     */
    @RabbitListener(queues = "notifications.delivery")
    public void handleNotification(JsonNode message) {
        try {
            log.info("📩 Evento recibido en Delivery: {}", message.toString());

            // Validar que el mensaje tenga los campos mínimos requeridos
            if (!message.has("type") || !message.has("email")) {
                log.error("❌ Mensaje inválido - faltan campos obligatorios: {}", message);
                return;
            }

            String notificationType = message.get("type").asText();
            String email = message.get("email").asText();
            String userName = message.has("userName") && !message.get("userName").isNull() ?
                    message.get("userName").asText() : "Usuario";

            log.info("🎯 Procesando notificación tipo: {} para email: {}", notificationType, email);

            switch (notificationType.toLowerCase()) {
                case "password_reset":
                    handlePasswordReset(message);
                    break;
                case "user_welcome":
                    handleUserWelcome(message, email, userName);
                    break;
                case "login_notification":
                    handleLoginNotification(message, email, userName);
                    break;
                case "password_updated":
                    handlePasswordUpdated(email, userName);
                    break;
                default:
                    log.warn("⚠️ Tipo de notificación desconocido: {}", notificationType);
            }

        } catch (Exception e) {
            log.error("❌ Error procesando notificación: {}", e.getMessage(), e);
        }
    }

    private void handlePasswordReset(JsonNode message) {
        try {
            String email = message.get("email").asText();

            // Extraer resetToken desde additionalData si existe
            String resetToken = null;
            if (message.has("additionalData") && !message.get("additionalData").isNull()) {
                JsonNode additionalData = message.get("additionalData");
                if (additionalData.has("resetToken") && !additionalData.get("resetToken").isNull()) {
                    resetToken = additionalData.get("resetToken").asText();
                }
            }

            if (resetToken == null || resetToken.isEmpty()) {
                resetToken = "temp-token-" + System.currentTimeMillis();
                log.warn("⚠️ No se encontró resetToken, usando temporal: {}", resetToken);
            }

            log.info("🔑 Procesando reset de contraseña para: {} con token: {}", email, resetToken);
            emailService.sendPasswordResetEmail(email, resetToken);

        } catch (Exception e) {
            log.error("❌ Error procesando password reset: {}", e.getMessage(), e);
        }
    }

    private void handleUserWelcome(JsonNode message, String email, String userName) {
        try {
            log.info("👋 Procesando bienvenida para usuario: {} ({})", userName, email);

            // Extraer token de activación si existe
            String activationToken = null;
            if (message.has("additionalData") && !message.get("additionalData").isNull()) {
                JsonNode additionalData = message.get("additionalData");
                if (additionalData.has("activationToken") && !additionalData.get("activationToken").isNull()) {
                    activationToken = additionalData.get("activationToken").asText();
                }
            }

            if (activationToken != null && !activationToken.isEmpty()) {
                emailService.sendWelcomeEmailWithActivation(email, userName, activationToken);
                log.info("🔑 Enviado email de bienvenida con token de activación a: {}", email);
            } else {
                emailService.sendWelcomeEmail(email, userName);
                log.info("👋 Enviado email de bienvenida simple a: {}", email);
            }

        } catch (Exception e) {
            log.error("❌ Error procesando bienvenida: {}", e.getMessage(), e);
        }
    }

    private void handleLoginNotification(JsonNode message, String email, String userName) {
        try {
            log.info("🔐 Procesando notificación de login para: {} ({})", userName, email);

            // Extraer datos de seguridad desde additionalData
            String ipAddress = "IP desconocida";
            String userAgent = "Dispositivo desconocido";
            String loginTime = null;

            if (message.has("additionalData") && !message.get("additionalData").isNull()) {
                JsonNode additionalData = message.get("additionalData");
                if (additionalData.has("ipAddress") && !additionalData.get("ipAddress").isNull()) {
                    ipAddress = additionalData.get("ipAddress").asText();
                }
                if (additionalData.has("userAgent") && !additionalData.get("userAgent").isNull()) {
                    userAgent = additionalData.get("userAgent").asText();
                }
                if (additionalData.has("loginTime") && !additionalData.get("loginTime").isNull()) {
                    loginTime = additionalData.get("loginTime").asText();
                }
            }

            emailService.sendLoginNotification(email, userName, ipAddress, userAgent, loginTime);
            log.info("🔐 Enviado email de notificación de login a: {}", email);

        } catch (Exception e) {
            log.error("❌ Error procesando login: {}", e.getMessage(), e);
        }
    }

    private void handlePasswordUpdated(String email, String userName) {
        try {
            log.info("🔒 Procesando confirmación de password actualizado para: {} ({})", userName, email);
            emailService.sendPasswordUpdatedConfirmation(email, userName);
        } catch (Exception e) {
            log.error("❌ Error procesando password updated: {}", e.getMessage(), e);
        }
    }
}