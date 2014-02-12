package com.github.t1.jms.browser;

import java.io.PrintWriter;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Map;

import javax.inject.Inject;
import javax.ws.rs.core.MediaType;

@javax.ws.rs.ext.Provider
@javax.ws.rs.Produces("text/html")
public class HtmlMapBodyWriter extends AbstractHtmlMessageBodyWriter<Map<?, ?>> {
    @Inject
    Accessors accessors;

    @Override
    public boolean isWriteable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
        return Map.class.isAssignableFrom(type);
    }

    @Override
    protected void print(Map<?, ?> map, PrintWriter out) {
        out.println("<html><body><table>");
        out.println("<tr><td>key</td><td>value</td></tr>");
        for (Map.Entry<?, ?> property : map.entrySet()) {
            out.print("<tr><td>");
            out.print(property.getKey());
            out.print("</td><td>");
            out.print(property.getValue());
            out.println("</td></tr>");
        }
        out.println("</table></body></html>");
    }
}
