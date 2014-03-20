package com.github.t1.jms.browser;

import java.util.*;

import javax.inject.Inject;
import javax.jms.*;
import javax.jms.Queue;
import javax.naming.*;
import javax.ws.rs.*;
import javax.ws.rs.core.Response;

import com.github.t1.jms.browser.exceptions.MessageNotFoundException;
import com.github.t1.log.Logged;
import com.github.t1.webresource.accessors.*;

@Logged
@Path(QueuesResource.QUEUES)
public class QueuesResource {
    public static final String QUEUES = "queues";

    @Inject
    MetaDataStore metaDataStore;

    @GET
    public List<Queue> queues() throws NamingException {
        List<Queue> out = new ArrayList<>();
        scanJndiForQueues(out, "");
        metaDataStore.put(out, new ListMetaData("JMS Queues"));
        return out;
    }

    private void scanJndiForQueues(List<Queue> out, String path) throws NamingException {
        InitialContext context = new InitialContext();
        Object resource = context.lookup(path);
        if (isSubContext(resource)) {
            NamingEnumeration<Binding> list = context.listBindings(path);
            while (list.hasMoreElements()) {
                Binding binding = list.nextElement();
                scanJndiForQueues(out, path + "/" + binding.getName());
            }
        } else if (resource instanceof Queue) {
            out.add((Queue) resource);
        } // else ignore Topics
    }

    private boolean isSubContext(Object object) {
        return javax.naming.Context.class.isAssignableFrom(object.getClass());
    }

    @GET
    @Path("{queue}")
    public Queue queue(@PathParam("queue") String queueName) {
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
