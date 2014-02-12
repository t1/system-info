package com.github.t1.jms.browser;

import static com.github.t1.jms.browser.QueuesResource.*;

import java.io.PrintWriter;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Enumeration;

import javax.jms.*;
import javax.naming.*;
import javax.ws.rs.core.MediaType;

import com.github.t1.webresource.meta2.AbstractHtmlMessageBodyWriter;

@javax.ws.rs.ext.Provider
@javax.ws.rs.Produces("text/html")
public class JmsQueueHtmlBodyWriter extends AbstractHtmlMessageBodyWriter<Queue> {
    @Override
    public boolean isWriteable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
        return Queue.class.isAssignableFrom(type);
    }

    @Override
    protected void printBody(Queue queue, PrintWriter out) {
        try (Session session = createSession()) {
            String queueName = queue.getQueueName();
            out.println("<h4>Queue: " + queueName + "</h4>");
            @SuppressWarnings("unchecked")
            Enumeration<Message> enumeration = session.createBrowser(queue).getEnumeration();
            while (enumeration.hasMoreElements()) {
                Message message = enumeration.nextElement();
                out.append(link(QUEUES + "/" + queueName + "/" + message.getJMSMessageID(), message.toString()));
                out.append("<br>\n");
            }
        } catch (JMSException e) {
            throw new RuntimeException(e);
        }
    }

    private Session createSession() throws JMSException {
        try {
            ConnectionFactory factory = (ConnectionFactory) InitialContext.doLookup("ConnectionFactory");
            return factory.createConnection().createSession();
        } catch (NamingException e) {
            throw new RuntimeException(e);
        }
    }
}
