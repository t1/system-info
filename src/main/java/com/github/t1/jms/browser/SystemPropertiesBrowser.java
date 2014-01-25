package com.github.t1.jms.browser;

import static javax.ws.rs.core.MediaType.*;

import java.util.*;

import javax.ws.rs.*;

@Path(SystemPropertiesBrowser.SYSTEMPROPERTIES)
public class SystemPropertiesBrowser {
    public static final String SYSTEMPROPERTIES = "system-properties";

    @GET
    @Produces(TEXT_HTML)
    public String systemProperties() {
        StringBuilder out = new StringBuilder();
        out.append("<html><body><table>\n");
        out.append("<tr><td>name</td><td>value</td></tr>\n");
        Properties properties = System.getProperties();
        Set<Map.Entry<Object, Object>> entries = properties.entrySet();
        for (Map.Entry<Object, Object> property : entries) {
            out.append("<tr><td>");
            out.append(property.getKey());
            out.append("</td><td>");
            out.append(property.getValue());
            out.append("</td></tr>\n");
        }
        out.append("</table></body></html>");
        return out.toString();
    }
}
