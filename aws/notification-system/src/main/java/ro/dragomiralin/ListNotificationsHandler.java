package ro.dragomiralin;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import ro.dragomiralin.entity.NotificationEntity;
import ro.dragomiralin.service.NotificationService;
import software.amazon.awssdk.regions.Region;

import java.util.List;

public class ListNotificationsHandler implements RequestHandler<Void, List<NotificationEntity>> {

    @Override
    public List<NotificationEntity> handleRequest(Void unused, Context context) {
        LambdaLogger logger = context.getLogger();
        logger.log("New event received: " + context.toString());

        NotificationService notificationService = new NotificationService(Region.US_EAST_1);
        logger.log("List notifications from DynamoDB");
        return notificationService.listNotifications();
    }
}
