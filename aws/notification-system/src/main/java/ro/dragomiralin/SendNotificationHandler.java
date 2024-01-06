package ro.dragomiralin;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import ro.dragomiralin.dto.*;
import ro.dragomiralin.service.NotificationService;
import ro.dragomiralin.utils.Validator;
import software.amazon.awssdk.regions.Region;

public class SendNotificationHandler implements RequestHandler<NotificationRequest, NotificationResponse> {

    @Override
    public NotificationResponse handleRequest(NotificationRequest notificationRequest, Context context) {
        LambdaLogger logger = context.getLogger();
        logger.log("New event received: " + context.toString());

        Validator validator = new Validator();
        ValidatorResponse validatorResponse = validator.validateNotificationRequest(notificationRequest);

        NotificationService notificationService = new NotificationService(Region.US_EAST_1);

        if (validatorResponse.hasError()) {
            logger.log(validatorResponse.errorMessage());
            return new NotificationResponse(NotificationStatus.FAILED, validatorResponse.errorMessage());
        }

        logger.log("Sending email to: " + notificationRequest.toString());
        NotificationResponse response = notificationService.sendNotification(notificationRequest);

        if(response.result().equals(NotificationStatus.OK)){
            logger.log("Email sent to: " + notificationRequest.toString());
        } else {
            logger.log("Email not sent to: " + notificationRequest.toString());
        }

        notificationService.saveNotification(notificationRequest, response);
        return response;
    }
}
