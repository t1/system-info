package com.github.t1.jms.browser;

import static javax.ws.rs.core.MediaType.*;
import static javax.ws.rs.core.Response.Status.*;

import javax.ws.rs.core.Response;

public class RestTools {
    public static Response ok(Object value) {
        return Response.ok(value).build();
    }

    public static Response badRequest(String message) {
        return Response.status(BAD_REQUEST).encoding(TEXT_PLAIN).entity(message).build();
    }

    public static Response notFound(String message) {
        return Response.status(NOT_FOUND).encoding(TEXT_PLAIN).entity(message).build();
    }
}
