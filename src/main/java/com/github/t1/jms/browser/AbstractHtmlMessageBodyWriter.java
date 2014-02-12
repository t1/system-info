package com.github.t1.jms.browser;

import java.io.*;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

import javax.inject.Inject;
import javax.ws.rs.core.*;
import javax.ws.rs.ext.MessageBodyWriter;

public abstract class AbstractHtmlMessageBodyWriter<T> implements MessageBodyWriter<T> {
    @Inject
    BasePath basePath;

    @Override
    public long getSize(T t, Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
        return -1;
    }

    @Override
    public void writeTo(T t, Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType,
            MultivaluedMap<String, Object> httpHeaders, OutputStream entityStream) {
        PrintWriter out = new PrintWriter(entityStream);
        out.println("<html><head>");
        out.println("</head><body>");
        print(t, out);
        out.println("</body></html>");
        out.flush(); // JBoss doesn't work without :(
    }

    protected abstract void print(T t, PrintWriter out);

    public String link(String path, String label) {
        return "<a href=\"" + basePath.resolve(path) + "\">" + label + "</a>";
    }
}
