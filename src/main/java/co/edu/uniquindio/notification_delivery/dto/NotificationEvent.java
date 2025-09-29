package co.edu.uniquindio.notification_delivery.dto;

import lombok.Data;

@Data
public class NotificationEvent {
    private String email;
    private String channel; // EMAIL, SMS, etc.
    private String subject;
    private String body;

    public NotificationEvent(String email, String channel, String subject, String body) {
        this.email = email;
        this.channel = channel;
        this.subject = subject;
        this.body = body;
    }

    public NotificationEvent() {
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getChannel() {
        return channel;
    }

    public void setChannel(String channel) {
        this.channel = channel;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }
}