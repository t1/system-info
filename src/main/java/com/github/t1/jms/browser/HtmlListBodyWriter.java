package com.github.t1.jms.browser;

import java.io.PrintWriter;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.List;

import javax.ws.rs.core.MediaType;

@javax.ws.rs.ext.Provider
@javax.ws.rs.Produces("text/html")
public class HtmlListBodyWriter extends AbstractHtmlMessageBodyWriter<List<?>> {
    @Override
    public boolean isWriteable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
        return List.class.isAssignableFrom(type);
    }

    @Override
    protected void printBody(List<?> list, PrintWriter out) {
        out.println("<ul>");
        for (Object item : list) {
            out.append("<li>");
            printItem(item, out);
            out.println("</li>");
        }
        out.println("</ul>");
    }
}
