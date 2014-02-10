package com.github.t1.jms.browser;

import static com.github.t1.jms.browser.QueuesResource.*;

import java.io.PrintWriter;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.List;

import javax.jms.*;
import javax.ws.rs.core.MediaType;

@javax.ws.rs.ext.Provider
@javax.ws.rs.Produces("text/html")
public class JmsQueueListHtmlBodyWriter extends AbstractMessageBodyWriter<List<Queue>> {
    @Override
    public boolean isWriteable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
        return List.class.isAssignableFrom(type);
    }


    @Override
    protected void print(List<Queue> list, PrintWriter out) {
        out.append("<ul>\n");

        for (Queue queue : list) {
            out.append("<li>");
            String name = name(queue);
            out.append(basePath.link(QUEUES + "/" + name, name));
            out.append("</li>\n");
        }

        out.append("</ul>");
    }

    private String name(Queue queue) {
        try {
            return queue.getQueueName();
        } catch (JMSException e) {
            throw new RuntimeException(e);
        }
    }
}
