package com.github.t1.jms.browser;

import static javax.ws.rs.core.MediaType.*;

import java.util.*;

import javax.jms.*;
import javax.jms.Queue;
import javax.naming.*;
import javax.ws.rs.*;
import javax.ws.rs.core.Response;

import org.joda.time.Instant;

import com.github.t1.jms.browser.exceptions.MessageNotFoundException;

@Path(QueuesResource.QUEUES)
public class QueuesResource extends Resource {
    public static final String QUEUES = "queues";

    @GET
    @Produces(TEXT_HTML)
    public String queuesAsHtml() throws NamingException, JMSException {
        StringBuilder out = new StringBuilder();
        out.append("<html><head>\n");
        out.append("</head><body><ul>\n");

        for (Queue queue : scan("")) {
            out.append("<li>");
            String name = queue.getQueueName();
            out.append(link(QUEUES + "/" + name, name));
            out.append("</li>\n");
        }

        out.append("</ul></body></html>\n");
        return out.toString();
    }

    @GET
    public Response queues() throws NamingException {
        return Response.ok(scan("")).build();
    }

    private List<Queue> scan(String path) throws NamingException {
        List<Queue> out = new ArrayList<>();
        scan(out, path);
        return out;
    }

    private void scan(List<Queue> out, String path) throws NamingException {
        InitialContext context = new InitialContext();
        Object resource = context.lookup(path);
        if (isSubContext(resource)) {
            NamingEnumeration<Binding> list = context.listBindings(path);
            while (list.hasMoreElements()) {
                Binding binding = list.nextElement();
                scan(out, path + "/" + binding.getName());
            }
        } else if (resource instanceof Queue) {
            out.add((Queue) resource);
        } // else ignore
    }

    private boolean isSubContext(Object object) {
        return javax.naming.Context.class.isAssignableFrom(object.getClass());
    }

    @GET
    @Path("{queue}")
    @Produces(TEXT_HTML)
    public String queue(@PathParam("queue") String queue) {
        StringBuilder out = new StringBuilder();
        out.append("<html><head>\n");
        out.append("</head><body>\n");
        printQueue(queue, out);
        out.append("</body></html>\n");
        return out.toString();
    }

    private void printQueue(String queueName, StringBuilder out) {
        try (Session session = createSession()) {
            Queue queue = session.createQueue(queueName);
            out.append("<h4>Queue: " + queue.getQueueName() + "</h4>\n");
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

    @GET
    @Path("{queue}/{messageId}")
    @Produces(TEXT_HTML)
    public String message(@PathParam("queue") String queue, @PathParam("messageId") String messageId) {
        StringBuilder out = new StringBuilder();
        out.append("<html><head>\n");
        out.append("</head><body>\n");
        printMessage(queue, messageId, out);
        out.append("</body></html>\n");
        return out.toString();
    }

    private void printMessage(String queueName, String messageId, StringBuilder out) {
        try (Session session = createSession()) {
            Queue queue = session.createQueue(queueName);
            out.append("<h4>Queue: " + queue.getQueueName() + "</h4>\n");
            Message message = getMessage(session, queue, messageId);
            out.append("<h5>" + messageId + "</h5>\n");
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

    private Message getMessage(Session session, Queue queue, String messageId) throws JMSException {
        @SuppressWarnings("unchecked")
        Enumeration<Message> enumeration = session.createBrowser(queue).getEnumeration();
        while (enumeration.hasMoreElements()) {
            Message message = enumeration.nextElement();
            if (messageId.equals(message.getJMSMessageID())) {
                return message;
            }
        }
        throw new MessageNotFoundException(messageId, queue.getQueueName());
    }

    private void printHeader(Message message, StringBuilder out) throws JMSException {
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

    private void printProperties(Message message, StringBuilder out) throws JMSException {
        @SuppressWarnings("unchecked")
        Enumeration<String> names = message.getPropertyNames();
        while (names.hasMoreElements()) {
            String name = names.nextElement();
            field(name, message.getStringProperty(name), out);
        }
    }

    private void printBody(Message message, StringBuilder out) throws JMSException {
        if (message instanceof BytesMessage) {
            BytesMessage bytesMessage = (BytesMessage) message;
            out.append("bytes: " + bytesMessage.getBodyLength());
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
            out.append("object: " + objectMessage.getObject());
        } else if (message instanceof StreamMessage) {
            out.append("stream");
        } else if (message instanceof TextMessage) {
            TextMessage textMessage = (TextMessage) message;
            out.append(textMessage.getText());
        } else {
            out.append("unsupported message body type in ").append(message.getJMSMessageID());
        }
    }

    private void hr(StringBuilder out) {
        out.append("<hr/>\n");
    }

    private void field(String name, Object value, StringBuilder out) throws JMSException {
        if (value != null) {
            out.append(name).append(": ").append(value).append("<br/>\n");
        }
    }
}
