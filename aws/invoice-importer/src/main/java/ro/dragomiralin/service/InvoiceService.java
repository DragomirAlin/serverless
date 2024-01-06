package ro.dragomiralin.service;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import ro.dragomiralin.entity.InvoiceEntity;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;
import java.util.UUID;

public class InvoiceService {
    private final Region region;
    private final static String INVOICES_TABLE = "invoices";


    public InvoiceService(Region region) {
        this.region = region;
    }

    public List<InvoiceEntity> processCsvFromS3(String bucketName, String key) {
        // Create an S3 client
        S3Client s3Client = S3Client.builder()
                .region(region)
                .build();

        // Download the CSV file from S3
        try (ResponseInputStream<GetObjectResponse> s3Object = s3Client.getObject(builder -> builder.bucket(bucketName).key(key))) {

            // Parse CSV data
            try (CSVParser csvParser = new CSVParser(new InputStreamReader(s3Object), CSVFormat.DEFAULT)) {
                return csvParser.getRecords().stream()
                        .map(this::mapToInvoiceEntity)
                        .toList();
            } catch (IOException e) {
                System.err.println("Error parsing CSV file");
                e.printStackTrace();
                return List.of();
            }
        } catch (IOException e) {
            System.err.println("Error downloading CSV file from S3");
            e.printStackTrace();
            return List.of();
        }
    }

    public void saveInvoices(List<InvoiceEntity> invoices) {
        DynamoDbClient ddb = DynamoDbClient.builder()
                .region(region)
                .build();

        DynamoDbEnhancedClient enhancedClient = DynamoDbEnhancedClient.builder()
                .dynamoDbClient(ddb)
                .build();

        DynamoDbTable<InvoiceEntity> table = enhancedClient.table(INVOICES_TABLE, TableSchema.fromBean(InvoiceEntity.class));
        invoices.forEach(table::putItem);
    }

    public List<InvoiceEntity> listInvoices() {
        DynamoDbClient ddb = DynamoDbClient.builder()
                .region(region)
                .build();

        DynamoDbEnhancedClient enhancedClient = DynamoDbEnhancedClient.builder()
                .dynamoDbClient(ddb)
                .build();

        DynamoDbTable<InvoiceEntity> table = enhancedClient.table(INVOICES_TABLE, TableSchema.fromBean(InvoiceEntity.class));
        return table.scan().items().stream().toList();
    }

    private InvoiceEntity mapToInvoiceEntity(CSVRecord record) {
        InvoiceEntity invoiceEntity = new InvoiceEntity();
        invoiceEntity.setId(UUID.randomUUID().toString());
        invoiceEntity.setInvoiceNumber(record.get(0));
        invoiceEntity.setInvoiceDate(record.get(1));
        invoiceEntity.setGrossAmount(record.get(2));
        invoiceEntity.setNetAmount(record.get(3));
        invoiceEntity.setVatAmount(record.get(4));
        invoiceEntity.setSupplierName(record.get(5));
        invoiceEntity.setSupplierAddress(record.get(6));
        invoiceEntity.setCurrency(record.get(7));
        invoiceEntity.setStatus(record.get(8));
        return invoiceEntity;
    }
}
