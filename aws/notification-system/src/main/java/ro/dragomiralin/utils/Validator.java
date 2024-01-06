package ro.dragomiralin.utils;

import ro.dragomiralin.dto.NotificationRequest;
import ro.dragomiralin.dto.ValidatorResponse;

public class Validator {

    public ValidatorResponse validateNotificationRequest(NotificationRequest notificationRequest) {
        ValidatorResponse validatorResponse = new ValidatorResponse(false, null);

        if (notificationRequest.emailTo() == null || notificationRequest.emailTo().isEmpty()) {
            validatorResponse = new ValidatorResponse(true, "To field is required");
        }
        if (notificationRequest.subject() == null || notificationRequest.subject().isEmpty()) {
            validatorResponse = new ValidatorResponse(true, "Subject field is required");
        }
        if (notificationRequest.message() == null || notificationRequest.message().isEmpty()) {
            validatorResponse = new ValidatorResponse(true, "Message field is required");
        }
        return validatorResponse;
    }
}
