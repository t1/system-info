package com.github.t1.jms.browser;

import static com.github.t1.jms.browser.JndiBrowser.*;
import static com.github.t1.jms.browser.MBeanBrowser.*;
import static com.github.t1.jms.browser.SystemPropertiesBrowser.*;
import static javax.ws.rs.core.MediaType.*;

import java.net.URI;

import javax.ws.rs.*;
import javax.ws.rs.core.UriInfo;

@Path("/")
public class Index {
    @javax.ws.rs.core.Context
    private UriInfo context;

    @GET
    @Produces(TEXT_HTML)
    public String index() {
        StringBuilder out = new StringBuilder();
        out.append("<html><body><table>\n");
        out.append("<tr><td>name</td><td>value</td></tr>\n");

        out.append("<tr><td>Server</td><td>").append(serverName()).append("</td></tr>\n");

        out.append("<tr><td>System-Properties</td><td>");
        link(out, SYSTEMPROPERTIES, "list");
        out.append("</td></tr>\n");

        out.append("<tr><td>jndi</td><td>");
        link(out, JNDI, "root");
        out.append("</td></tr>\n");

        out.append("<tr><td>mbeans</td><td>");
        link(out, MBEANS, "MBeans");
        out.append("</td></tr>\n");

        out.append("</table></body></html>");
        return out.toString();
    }

    private String serverName() {
        String glassfishVersion = System.getProperty("glassfish.version");
        if (glassfishVersion != null)
            return glassfishVersion;
        return "unknown";
    }

    private void link(StringBuilder out, String path, String txt) {
        URI uri = context.resolve(URI.create("-system/" + path));
        out.append("<a href=\"").append(uri).append("\">").append(txt).append("</a>");
    }

    @GET
    @Path("/echo/{echo}")
    public String echo(@PathParam("echo") String param) {
        return param;
    }
}
