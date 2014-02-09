package com.github.t1.jms.browser;

import static javax.ws.rs.core.MediaType.*;
import static javax.ws.rs.core.Response.Status.*;

import java.net.URI;

import javax.ws.rs.core.*;

public class Resource {
    public static Response ok(Object value) {
        return Response.ok(value).build();
    }

    public static Response badRequest(String message) {
        return Response.status(BAD_REQUEST).encoding(TEXT_PLAIN).entity(message).build();
    }

    public static Response notFound(String message) {
        return Response.status(NOT_FOUND).encoding(TEXT_PLAIN).entity(message).build();
    }

    @Context
    private UriInfo uriInfo;

    public URI resolve(String path) {
        return baseUri().resolve(path);
    }

    public URI baseUri() {
        URI baseUri = uriInfo.getBaseUri();
        String string = baseUri.toASCIIString();
        if (!string.endsWith("/"))
            string = string + "/";
        return URI.create(string);
    }

    public String link(String path, String label) {
        return "<a href=\"" + resolve(path) + "\">" + label + "</a>";
    }
}
