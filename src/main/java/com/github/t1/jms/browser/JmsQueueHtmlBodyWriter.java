package com.github.t1.jms.browser;

import static com.github.t1.jms.browser.QueuesResource.*;

import java.io.PrintWriter;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Enumeration;

import javax.jms.*;
import javax.naming.*;
<<<<<<< HEAD
import javax.ws.rs.core.MediaType;
=======
import javax.ws.rs.core.*;
import javax.ws.rs.ext.MessageBodyWriter;
>>>>>>> 51af75088586cbf7bba50491a8130ac58c419a9b

@javax.ws.rs.ext.Provider
@javax.ws.rs.Produces("text/html")
public class JmsQueueHtmlBodyWriter extends AbstractMessageBodyWriter<Queue> {
    @Override
    public boolean isWriteable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
        return Queue.class.isAssignableFrom(type);
    }

    @Override
<<<<<<< HEAD
    protected void print(Queue queue, PrintWriter out) {
=======
    public long getSize(Queue t, Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
        return -1;
    }

    @Override
    public void writeTo(Queue queue, Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType,
            MultivaluedMap<String, Object> httpHeaders, OutputStream entityStream) {
        PrintWriter out = new PrintWriter(entityStream);
        out.println("<html><head>");
        out.println("</head><body>");
        print(queue, out);
        out.println("</body></html>");
        out.flush();
    }

    private void print(Queue queue, PrintWriter out) {
>>>>>>> 51af75088586cbf7bba50491a8130ac58c419a9b
        try (Session session = createSession()) {
            String queueName = queue.getQueueName();
            out.println("<h4>Queue: " + queueName + "</h4>");
            @SuppressWarnings("unchecked")
            Enumeration<Message> enumeration = session.createBrowser(queue).getEnumeration();
            while (enumeration.hasMoreElements()) {
                Message message = enumeration.nextElement();
                out.append(basePath.link(QUEUES + "/" + queueName + "/" + message.getJMSMessageID(), message.toString()));
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
