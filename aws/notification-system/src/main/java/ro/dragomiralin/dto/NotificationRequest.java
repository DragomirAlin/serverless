package ro.dragomiralin.dto;


public record NotificationRequest(String emailTo, String subject, String message) {
}