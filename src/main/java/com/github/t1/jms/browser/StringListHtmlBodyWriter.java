package com.github.t1.jms.browser;

import java.io.PrintWriter;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.List;

import javax.ws.rs.core.MediaType;

@javax.ws.rs.ext.Provider
@javax.ws.rs.Produces("text/html")
public class StringListHtmlBodyWriter extends AbstractMessageBodyWriter<List<String>> {
    @Override
    public boolean isWriteable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
        return "java.util.List<java.lang.String>".equals(genericType.toString());
    }

    @Override
    protected void print(List<String> list, PrintWriter out) {
        out.append("<ul>\n");
        for (String string : list) {
            out.append("<li>").append(string).append("</li>\n");
        }
        out.append("</ul>");
    }
}
