package com.github.t1.jms.browser;

import java.io.PrintWriter;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.net.URI;
import java.util.List;

import javax.inject.Inject;
import javax.ws.rs.core.MediaType;

@javax.ws.rs.ext.Provider
@javax.ws.rs.Produces("text/html")
public class HtmlListBodyWriter extends AbstractHtmlMessageBodyWriter<List<?>> {
    @Inject
    Accessors accessors;

    @Override
    public boolean isWriteable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
        return List.class.isAssignableFrom(type);
    }

    @Override
    protected void print(List<?> list, PrintWriter out) {
        out.println("<ul>");
        for (Object item : list) {
            out.append("<li>");
            printItem(item, out);
            out.println("</li>");
        }
        out.println("</ul>");
    }

    private void printItem(Object item, PrintWriter out) {
        Accessor<Object> accessor = accessors.of(item);
        String title = accessor.title(item);
        URI link = accessor.link(item);
        if (link == null) {
            out.append(title);
        } else {
            out.printf("<a href=\"%s\">%s</a>\n", link, title);
        }
    }
}
