package ro.dragomiralin.service;

import ro.dragomiralin.dto.NotificationRequest;
import ro.dragomiralin.dto.NotificationResponse;
import ro.dragomiralin.dto.NotificationStatus;
import ro.dragomiralin.entity.NotificationEntity;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.ses.SesClient;
import software.amazon.awssdk.services.ses.model.*;

import java.util.List;
import java.util.UUID;

public class NotificationService {
    private Region region;
    private final static String NOTIFICATION_TABLE = "notifications";

    public NotificationService(Region region) {
        this.region = region;
    }

    public void saveNotification(NotificationRequest notificationRequest, NotificationResponse response) {
        NotificationEntity notification = new NotificationEntity();
        notification.setId(UUID.randomUUID().toString());
        notification.setEmailTo(notificationRequest.emailTo());
        notification.setSubject(notificationRequest.subject());
        notification.setMessage(notificationRequest.message());
        notification.setStatus(response.result().toString());
        if (response.result().equals(NotificationStatus.FAILED)) {
            notification.setErrorMessage(response.message());
        }

        DynamoDbClient ddb = DynamoDbClient.builder()
                .region(region)
                .build();

        DynamoDbEnhancedClient enhancedClient = DynamoDbEnhancedClient.builder()
                .dynamoDbClient(ddb)
                .build();

        DynamoDbTable<NotificationEntity> table = enhancedClient.table(NOTIFICATION_TABLE, TableSchema.fromBean(NotificationEntity.class));
        table.putItem(notification);
    }

    public NotificationResponse sendNotification(NotificationRequest notificationRequest) {
        Destination destination = Destination.builder()
                .toAddresses(notificationRequest.emailTo())
                .build();

        Content content = Content.builder()
                .data(notificationRequest.message())
                .build();

        Content sub = Content.builder()
                .data(notificationRequest.subject())
                .build();

        Body body = Body.builder()
                .html(content)
                .build();

        Message msg = Message.builder()
                .subject(sub)
                .body(body)
                .build();

        SendEmailRequest emailRequest = SendEmailRequest.builder()
                .destination(destination)
                .message(msg)
                .source("dragomirdanielalin@gmail.com")
                .build();

        try {
            System.out.println("Sending an email through Amazon SES");

            SesClient client = SesClient.builder()
                    .region(region)
                    .build();

            SendEmailResponse sendEmailResponse = client.sendEmail(emailRequest);


            if (sendEmailResponse.sdkHttpResponse().isSuccessful()) {
                return new NotificationResponse(NotificationStatus.OK, "Email has been sent!");
            } else {
                return new NotificationResponse(NotificationStatus.FAILED, "Email not sent because %s!".formatted(sendEmailResponse.sdkHttpResponse().statusText()));

            }
        } catch (SesException e) {
            System.err.println(e.awsErrorDetails().errorMessage());
            return new NotificationResponse(NotificationStatus.FAILED, "Email not sent because %s!".formatted(e.awsErrorDetails().errorMessage()));
        }
    }

    public List<NotificationEntity> listNotifications() {
        DynamoDbClient ddb = DynamoDbClient.builder()
                .region(region)
                .build();

        DynamoDbEnhancedClient enhancedClient = DynamoDbEnhancedClient.builder()
                .dynamoDbClient(ddb)
                .build();

        DynamoDbTable<NotificationEntity> table = enhancedClient.table(NOTIFICATION_TABLE, TableSchema.fromBean(NotificationEntity.class));
        return table.scan().items().stream().toList();
    }


}
