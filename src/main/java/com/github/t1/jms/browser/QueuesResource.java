package com.github.t1.jms.browser;

import static javax.ws.rs.core.MediaType.*;

import java.util.Enumeration;

import javax.jms.*;
import javax.naming.*;
import javax.ws.rs.*;

@Path(QueuesResource.QUEUES)
public class QueuesResource extends Resource {
    public static final String QUEUES = "queues";

    @GET
    @Produces(TEXT_HTML)
    public String queues() throws NamingException {
        StringBuilder out = new StringBuilder();
        out.append("<html><head>\n");
        out.append("</head><body><ul>\n");
        scan(out, "");
        out.append("</ul></body></html>\n");
        return out.toString();
    }

    private void scan(StringBuilder out, String path) throws NamingException {
        InitialContext context = new InitialContext();
        Object resource = context.lookup(path);
        if (isSubContext(resource)) {
            NamingEnumeration<Binding> list = context.listBindings(path);
            while (list.hasMoreElements()) {
                Binding binding = list.nextElement();
                scan(out, path + "/" + binding.getName());
            }
        } else if (resource instanceof Destination) {
            out.append("<li>");
            out.append(link(QUEUES + "/" + path, path));
            out.append("</li>\n");
        } // else ignore
    }

    private boolean isSubContext(Object object) {
        return javax.naming.Context.class.isAssignableFrom(object.getClass());
    }

    @GET
    @Path("{queue:.*}")
    @Produces(TEXT_HTML)
    public String queue(@PathParam("queue") String queueName) {
        StringBuilder out = new StringBuilder();
        out.append("<html><head>\n");
        out.append("</head><body>\n");
        print(queueName, out);
        out.append("</body></html>\n");
        return out.toString();
    }

    private void print(String queueName, StringBuilder out) {
        Queue queue = getQueue(queueName);
        try (Session session = createSession()) {
            out.append("<h4>Queue: " + queue.getQueueName() + "</h4>\n");
            @SuppressWarnings("unchecked")
            Enumeration<Message> enumeration = session.createBrowser(queue).getEnumeration();
            while (enumeration.hasMoreElements()) {
                Message message = enumeration.nextElement();
                out.append("*" + message + "<br>");
            }
        } catch (JMSException e) {
            throw new RuntimeException(e);
        }
    }

    private Queue getQueue(String queueName) {
        try {
            return InitialContext.doLookup(queueName);
        } catch (NamingException e) {
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
