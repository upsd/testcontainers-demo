package acceptance;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.AmazonSQSClient;
import com.amazonaws.services.sqs.model.SendMessageResult;
import com.amazonaws.util.IOUtils;
import org.awaitility.core.ThrowingRunnable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.localstack.LocalStackContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import upsd.App;
import upsd.MessageProcessor;

import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.TimeUnit;

import static org.awaitility.Awaitility.given;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.testcontainers.containers.localstack.LocalStackContainer.Service.S3;
import static org.testcontainers.containers.localstack.LocalStackContainer.Service.SQS;

@Testcontainers
class WriteToS3 {

    private static final String BUCKET_NAME = "a-nice-bucket";
    private static final String MESSAGE = "Hello world";
    private static final String QUEUE_NAME = "Main";

    @Container
    private static LocalStackContainer localstack = new LocalStackContainer().withServices(SQS, S3);

    private AmazonSQS sqs;
    private AmazonS3 s3;
    private String mainQueueURL;

    @BeforeEach
    void setUp() {
        sqs = AmazonSQSClient.builder()
                .withEndpointConfiguration(localstack.getEndpointConfiguration(SQS))
                .withCredentials(localstack.getDefaultCredentialsProvider())
                .build();
        s3 = AmazonS3Client.builder()
                .withEndpointConfiguration(localstack.getEndpointConfiguration(S3))
                .withCredentials(localstack.getDefaultCredentialsProvider())
                .build();

        mainQueueURL = createQueue(QUEUE_NAME);
        s3.createBucket(BUCKET_NAME);
    }

    @Test
    void when_queue_populated() {
        String expectedMessageId = placeMessageInQueue();

        new App().start(new MessageProcessor(sqs, s3, BUCKET_NAME, mainQueueURL));

        waitFor(() -> {
            S3Object objectFound = s3.getObject(BUCKET_NAME, expectedMessageId);
            String body = getObjectBodyFrom(objectFound);

            assertThat(body, is(MESSAGE));
        });
    }

    private String createQueue(String queueName) {
        return sqs.createQueue(queueName).getQueueUrl();
    }

    private void waitFor(ThrowingRunnable assertionMethod) {
        given().ignoreExceptions().atMost(5, TimeUnit.SECONDS).await().untilAsserted(assertionMethod);
    }

    private String placeMessageInQueue() {
        SendMessageResult messageResult = sqs.sendMessage(mainQueueURL, MESSAGE);
        return messageResult.getMessageId();
    }

    private String getObjectBodyFrom(S3Object objectFound) throws IOException {
        try (InputStream is = objectFound.getObjectContent()) {
            return IOUtils.toString(is);
        }
    }

}
