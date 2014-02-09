package com.github.t1.jms.browser;

import static com.github.t1.jms.browser.JndiBrowser.*;
import static com.github.t1.jms.browser.MBeanBrowser.*;
import static com.github.t1.jms.browser.QueuesResource.*;
import static com.github.t1.jms.browser.SystemPropertiesBrowser.*;
import static javax.ws.rs.core.MediaType.*;

import javax.ws.rs.*;

@Path("/")
public class Index extends Resource {
    @GET
    @Produces(TEXT_HTML)
    public String index() {
        StringBuilder out = new StringBuilder();
        out.append("<html><body><table>\n");
        out.append("<tr><td>name</td><td>value</td></tr>\n");

        out.append("<tr><td>Server</td><td>").append(serverName()).append("</td></tr>\n");

        link(out, "System-Properties", SYSTEMPROPERTIES, "list");
        link(out, "jndi", JNDI, "root");
        link(out, "mbeans", MBEANS, "MBeans");
        link(out, "queues", QUEUES, "Queues");

        out.append("</table></body></html>");
        return out.toString();
    }

    private String serverName() {
        String glassfishVersion = System.getProperty("glassfish.version");
        if (glassfishVersion != null)
            return glassfishVersion;
        return "unknown";
    }

    private void link(StringBuilder out, String cellLabel, String path, String linkLabel) {
        out.append("<tr>");
        out.append("<td>").append(cellLabel).append("</td>");
        out.append("<td>").append(link(path, linkLabel)).append("</td>");
        out.append("</tr>\n");
    }

    @GET
    @Path("/echo/{echo}")
    public String echo(@PathParam("echo") String param) {
        return param;
    }
}
