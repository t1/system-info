package com.github.t1.jms.browser;

import static com.github.t1.jms.browser.QueuesResource.*;

import java.net.URI;

import javax.jms.*;

import com.github.t1.webresource.accessors.AbstractAccessor;

public class JmsQueueAccessor extends AbstractAccessor<Queue> {
    @Override
    public String title(Queue queue) {
        return name(queue);
    }

    @Override
    public URI link(Queue queue) {
        return resolve(QUEUES + "/" + name(queue));
    }

    private String name(Queue queue) {
        try {
            return queue.getQueueName();
        } catch (JMSException e) {
            throw new RuntimeException(e);
        }
    }
}
