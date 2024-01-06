package ro.dragomiralin;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import ro.dragomiralin.entity.InvoiceEntity;
import ro.dragomiralin.service.InvoiceService;
import software.amazon.awssdk.regions.Region;

import java.util.List;

public class ListInvoicesHandler implements RequestHandler<Void, List<InvoiceEntity>> {
    @Override
    public List<InvoiceEntity> handleRequest(Void unused, Context context) {
        LambdaLogger logger = context.getLogger();
        logger.log("New event received: " + context.toString());

        InvoiceService invoiceService = new InvoiceService(Region.US_EAST_1);
        logger.log("List invoices from DynamoDB");
        return invoiceService.listInvoices();
    }
}
