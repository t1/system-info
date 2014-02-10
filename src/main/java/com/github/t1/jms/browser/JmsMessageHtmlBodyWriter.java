package com.github.t1.jms.browser;

import java.io.*;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Enumeration;

import javax.jms.*;
import javax.ws.rs.core.*;
import javax.ws.rs.ext.MessageBodyWriter;

import org.joda.time.Instant;

@javax.ws.rs.ext.Provider
@javax.ws.rs.Produces("text/html")
public class JmsMessageHtmlBodyWriter implements MessageBodyWriter<Message> {

    @Override
    public boolean isWriteable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
        return Message.class.isAssignableFrom(type);
    }

    @Override
    public long getSize(Message t, Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
        return -1;
    }

    @Override
    public void writeTo(Message message, Class<?> type, Type genericType, Annotation[] annotations,
            MediaType mediaType, MultivaluedMap<String, Object> httpHeaders, OutputStream entityStream) {
        PrintWriter out = new PrintWriter(entityStream);
        out.println("<html><head>");
        out.println("</head><body>");
        printMessage(message, out);
        out.println("</body></html>");
        out.flush();
    }


    private void printMessage(Message message, PrintWriter out) {
        try {
            out.println("<h4>Queue: " + name(message.getJMSDestination()) + "</h4>");
            out.println("<h5>" + message.getJMSMessageID() + "</h5>");
            hr(out);
            printHeader(message, out);
            hr(out);
            printProperties(message, out);
            hr(out);
            printBody(message, out);
            hr(out);
        } catch (JMSException e) {
            throw new RuntimeException(e);
        }
    }

    private String name(Destination destination) throws JMSException {
        if (destination instanceof Queue) {
            Queue queue = (Queue) destination;
            return queue.getQueueName();
        }
        if (destination instanceof Topic) {
            Topic topic = (Topic) destination;
            return topic.getTopicName();
        }
        throw new UnsupportedOperationException("unexpected destinaiton type: " + destination);
    }

    private void hr(PrintWriter out) {
        out.println("<hr/>");
    }

    private void printHeader(Message message, PrintWriter out) throws JMSException {
        field("correlationId", message.getJMSCorrelationID(), out);
        field("deliveryMode", message.getJMSDeliveryMode(), out);
        field("deliveryTime", message.getJMSDeliveryTime(), out);
        field("expiration", message.getJMSExpiration(), out);
        field("priority", message.getJMSPriority(), out);
        field("redelivered", message.getJMSRedelivered(), out);
        field("replyTo", message.getJMSReplyTo(), out);
        field("timestamp", new Instant(message.getJMSTimestamp()), out);
        field("type", message.getJMSType(), out);
    }

    private void printProperties(Message message, PrintWriter out) throws JMSException {
        @SuppressWarnings("unchecked")
        Enumeration<String> names = message.getPropertyNames();
        while (names.hasMoreElements()) {
            String name = names.nextElement();
            field(name, message.getStringProperty(name), out);
        }
    }

    private void printBody(Message message, PrintWriter out) throws JMSException {
        if (message instanceof BytesMessage) {
            BytesMessage bytesMessage = (BytesMessage) message;
            out.println("bytes: " + bytesMessage.getBodyLength());
        } else if (message instanceof MapMessage) {
            MapMessage mapMessage = (MapMessage) message;
            @SuppressWarnings("unchecked")
            Enumeration<String> names = mapMessage.getMapNames();
            while (names.hasMoreElements()) {
                String name = names.nextElement();
                field(name, mapMessage.getString(name), out);
            }
        } else if (message instanceof ObjectMessage) {
            ObjectMessage objectMessage = (ObjectMessage) message;
            out.println("object: " + objectMessage.getObject());
        } else if (message instanceof StreamMessage) {
            out.println("stream");
        } else if (message instanceof TextMessage) {
            TextMessage textMessage = (TextMessage) message;
            out.println(textMessage.getText());
        } else {
            out.println("unsupported message body type in " + message.getJMSMessageID());
        }
    }

    private void field(String name, Object value, PrintWriter out) throws JMSException {
        if (value != null) {
            out.printf("<b>%s</b>: %s<br/>\n", name, value);
        }
    }
}
