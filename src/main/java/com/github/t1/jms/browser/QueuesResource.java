package com.github.t1.jms.browser;

import static javax.ws.rs.core.MediaType.*;

import java.util.*;

import javax.jms.*;
import javax.jms.Queue;
import javax.naming.*;
import javax.ws.rs.*;
import javax.ws.rs.core.Response;

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
    public Response message(@PathParam("queue") String queueName, @PathParam("messageId") String messageId) {
        Message message = getMessage(queueName, messageId);
        return ok(message);
    }

    private Message getMessage(String queueName, String messageId) {
        try (Session session = createSession()) {
            Queue queue = session.createQueue(queueName);
            return getMessage(session, queue, messageId);
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
}
