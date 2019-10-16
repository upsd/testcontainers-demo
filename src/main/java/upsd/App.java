package upsd;

import java.util.concurrent.ScheduledExecutorService;

import static java.util.concurrent.Executors.newSingleThreadScheduledExecutor;
import static java.util.concurrent.TimeUnit.SECONDS;

public class App {

    public void start(MessageProcessor messageProcessor) {
        ScheduledExecutorService service = newSingleThreadScheduledExecutor();
        service.scheduleAtFixedRate(messageProcessor, 0, 1, SECONDS);
    }
}
