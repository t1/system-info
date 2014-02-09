package com.github.t1.jms.browser;

import java.io.*;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Enumeration;

import javax.jms.*;
import javax.naming.*;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.*;
import javax.ws.rs.ext.MessageBodyWriter;

@javax.ws.rs.ext.Provider
@javax.ws.rs.Produces("text/html")
public class JmsQueueHtmlBodyWriter implements MessageBodyWriter<Queue> {

    @Override
    public boolean isWriteable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
        return Queue.class.isAssignableFrom(type);
    }

    @Override
    public long getSize(Queue t, Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
        return -1;
    }

    @Override
    public void writeTo(Queue queue, Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType,
            MultivaluedMap<String, Object> httpHeaders, OutputStream entityStream) throws IOException,
            WebApplicationException {
        PrintWriter out = new PrintWriter(entityStream);
        out.println("<html><head>");
        out.println("</head><body>");
        print(queue, out);
        out.println("</body></html>");
        out.flush();
    }

    private void print(Queue queue, PrintWriter out) {
        try (Session session = createSession()) {
            out.println("<h4>Queue: " + queue.getQueueName() + "</h4>");
            @SuppressWarnings("unchecked")
            Enumeration<Message> enumeration = session.createBrowser(queue).getEnumeration();
            while (enumeration.hasMoreElements()) {
                Message message = enumeration.nextElement();
                out.println("*" + message + "<br>");
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
