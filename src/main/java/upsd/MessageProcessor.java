package upsd;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.sqs.AmazonSQS;

public class MessageProcessor implements Runnable {

    private final AmazonSQS sqs;
    private final AmazonS3 s3;
    private final String bucketName;
    private final String mainQueueURL;

    public MessageProcessor(AmazonSQS sqs, AmazonS3 s3, String bucketName, String mainQueueURL) {
        this.sqs = sqs;
        this.s3 = s3;
        this.bucketName = bucketName;
        this.mainQueueURL = mainQueueURL;
    }

    @Override
    public void run() {
        sqs.receiveMessage(mainQueueURL).getMessages().stream().forEach(m -> {
            s3.putObject(bucketName, m.getMessageId(), m.getBody());
        });
    }
}
