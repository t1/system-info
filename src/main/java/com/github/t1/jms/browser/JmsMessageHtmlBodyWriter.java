package com.github.t1.jms.browser;

import java.io.PrintWriter;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Enumeration;

import javax.jms.*;
import javax.ws.rs.core.MediaType;

import org.joda.time.Instant;

import com.github.t1.webresource.meta2.AbstractHtmlMessageBodyWriter;

@javax.ws.rs.ext.Provider
@javax.ws.rs.Produces("text/html")
public class JmsMessageHtmlBodyWriter extends AbstractHtmlMessageBodyWriter<Message> {

    @Override
    public boolean isWriteable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
        return Message.class.isAssignableFrom(type);
    }

    @Override
    protected void printBody(Message message, PrintWriter out) {
        try {
            out.println("<h4>" + name(message.getJMSDestination()) + ": " + message.getJMSMessageID() + "</h4>");
            hr(out);
            printMessageHeader(message, out);
            hr(out);
            printMessageProperties(message, out);
            hr(out);
            printMessageBody(message, out);
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

    private void printMessageHeader(Message message, PrintWriter out) throws JMSException {
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

    private void printMessageProperties(Message message, PrintWriter out) throws JMSException {
        @SuppressWarnings("unchecked")
        Enumeration<String> names = message.getPropertyNames();
        while (names.hasMoreElements()) {
            String name = names.nextElement();
            field(name, message.getStringProperty(name), out);
        }
    }

    private void printMessageBody(Message message, PrintWriter out) throws JMSException {
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

    private void field(String name, Object value, PrintWriter out) {
        if (value != null) {
            out.printf("<b>%s</b>: %s<br/>\n", name, value);
        }
    }
}
