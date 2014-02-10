package com.github.t1.jms.browser;

import java.util.*;

import javax.jms.*;
import javax.jms.Queue;
import javax.naming.*;
import javax.ws.rs.*;
import javax.ws.rs.core.*;

import com.github.t1.jms.browser.exceptions.MessageNotFoundException;

@Path(QueuesResource.QUEUES)
public class QueuesResource {
    public static final String QUEUES = "queues";

    @GET
    public Response queues() throws NamingException {
        return Response.ok(new GenericEntity<List<Queue>>(scan("")) {}).build();
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
    public Response queue(@PathParam("queue") String queue) {
        return Response.ok(getQueue(queue)).build();
    }

    private Queue getQueue(String queueName) {
        try (Session session = createSession()) {
            return session.createQueue(queueName);
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
        return Response.ok(message).build();
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
