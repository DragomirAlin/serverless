package ro.dragomiralin;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.S3Event;
import com.amazonaws.services.lambda.runtime.events.models.s3.S3EventNotification;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import ro.dragomiralin.entity.InvoiceEntity;
import ro.dragomiralin.service.InvoiceService;
import software.amazon.awssdk.regions.Region;

import java.util.List;

public class InvoiceImporterHandler implements RequestHandler<S3Event, String> {

    @Override
    public String handleRequest(S3Event s3Event, Context context) {
        LambdaLogger logger = context.getLogger();
        List<S3EventNotification.S3EventNotificationRecord> records = s3Event.getRecords();

        logger.log("New event received: " + context.toString());

        InvoiceService invoiceService = new InvoiceService(Region.US_EAST_1);
        for (S3EventNotification.S3EventNotificationRecord record : records) {
            String key = record.getS3().getObject().getKey();
            String bucketName = record.getS3().getBucket().getName();

            List<InvoiceEntity> invoiceEntities = invoiceService.processCsvFromS3(bucketName, key);
            invoiceService.saveInvoices(invoiceEntities);
            logger.log("%s %s saved to DynamoDB".formatted(invoiceEntities.size(), invoiceEntities.size() == 1 ? "invoice" : "invoices"));
        }

        return "Processing complete";
    }

}
