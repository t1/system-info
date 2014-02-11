package com.github.t1.jms.browser;

import static com.github.t1.jms.browser.QueuesResource.*;

import java.net.URI;

import javax.inject.Inject;
import javax.jms.*;

public class JmsQueueAccessor implements Accessor<Queue> {
    @Inject
    BasePath basePath;

    @Override
    public String title(Queue queue) {
        return name(queue);
    }

    @Override
    public URI link(Queue queue) {
        return basePath.resolve(QUEUES + "/" + name(queue));
    }

    private String name(Queue queue) {
        try {
            return queue.getQueueName();
        } catch (JMSException e) {
            throw new RuntimeException(e);
        }
    }
}
