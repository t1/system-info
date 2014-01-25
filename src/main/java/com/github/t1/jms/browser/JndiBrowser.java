package com.github.t1.jms.browser;

import java.lang.reflect.Proxy;
import java.net.URI;

import javax.naming.*;
import javax.ws.rs.*;
import javax.ws.rs.core.UriInfo;

@Path(JndiBrowser.JNDI)
public class JndiBrowser {
    public static final String JNDI = "jndi";

    @javax.ws.rs.core.Context
    private UriInfo context;

    @GET
    public String jndi() throws NamingException {
        return listJndi("");
    }

    @GET
    @Path("{name:.+}")
    public String jndi(@PathParam("name") String name) throws NamingException {
        return listJndi(name);
    }

    private String listJndi(String path) throws NamingException {
        Context c = new InitialContext();
        StringBuilder out = new StringBuilder();
        out.append("<html><body>");
        listJndi(out, c, path);
        out.append("</body></html>");
        return out.toString();
    }

    private void listJndi(StringBuilder out, Context c, String path) throws NamingException {
        out.append("<table>\n");
        NamingEnumeration<Binding> list = c.listBindings(path);
        while (list.hasMoreElements()) {
            Binding binding = list.nextElement();
            out.append("<tr><td>");
            out.append(binding.getName());
            out.append("</td><td>");
            final Class<?> type = type(binding);
            if (Context.class.isAssignableFrom(type)) {
                link(out, "jndi/" + path + "/" + binding.getName(), "sub-list");
            } else {
                info(out, type);
                out.append(":").append(binding.getObject());
                if (Proxy.isProxyClass(binding.getObject().getClass()))
                    out.append("[*]");
            }
            out.append("</td></tr>\n");
        }
        out.append("</table>");
    }

    private Class<?> type(NameClassPair pair) {
        String className = pair.getClassName();
        try {
            ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
            return Class.forName(className, false, classLoader);
        } catch (ClassNotFoundException e) {
            System.out.println("class " + className + " -> not found");
            return Object.class;
        }
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
