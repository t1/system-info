package com.github.t1.jms.browser;

import static javax.ws.rs.core.MediaType.*;

import java.lang.reflect.Proxy;
import java.net.URI;

import javax.naming.*;
import javax.ws.rs.*;
import javax.ws.rs.core.*;

@Path(JndiBrowser.JNDI)
public class JndiBrowser {
    public static final String JNDI = "jndi";

    @javax.ws.rs.core.Context
    private UriInfo context;

    @GET
    public Response jndi() throws NamingException {
        return getJndi("");
    }

    @GET
    @Path("{path:.+}")
    public Response jndi(@PathParam("path") String path) throws NamingException {
        return getJndi(path);
    }

    private Response getJndi(String path) throws NamingException {
        javax.naming.Context context = new InitialContext();
        Object object = context.lookup(path);
        if (isSubContext(object)) {
            StringBuilder out = new StringBuilder();
            out.append("<html><body>");
            listSubContext(out, context, path);
            out.append("</body></html>");
            return Response.ok(out.toString(), TEXT_HTML).build();
        } else {
            return Response.ok(object).build();
        }
    }

    private void listSubContext(StringBuilder out, javax.naming.Context context, String path) throws NamingException {
        out.append("<table>\n");
        NamingEnumeration<Binding> list = context.listBindings(path);
        while (list.hasMoreElements()) {
            Binding binding = list.nextElement();
            out.append("<tr><td>");
            out.append(binding.getName());
            out.append("</td><td>");
            if (isSubContext(binding.getObject())) {
                link(out, JNDI + "/" + path + "/" + binding.getName(), "sub-list");
            } else {
                info(out, binding.getObject().getClass());
                out.append(":").append(binding.getObject());
                if (Proxy.isProxyClass(binding.getObject().getClass()))
                    out.append("[*]");
            }
            out.append("</td></tr>\n");
        }
        out.append("</table>");
    }

    private boolean isSubContext(Object object) {
        return javax.naming.Context.class.isAssignableFrom(object.getClass());
    }

    private void link(StringBuilder out, String path, String txt) {
        out.append("<a href=\"").append(context.resolve(URI.create(path))).append("\">").append(txt).append("</a>");
    }

    private void info(StringBuilder out, Class<?> t0) {
        boolean first = true;
        for (Class<?> t = t0; t != null && !Object.class.equals(t); t = t.getSuperclass()) {
            if (first) {
                first = false;
            } else {
                out.append(", ");
            }
            out.append(t.getName());
            for (Class<?> i : t.getInterfaces()) {
                out.append("&lt;");
                info(out, i);
                out.append("&gt;");
            }
        }
    }
}
