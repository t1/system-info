package com.github.t1.jms.browser.exceptions;

import static java.lang.Character.*;

import javax.ws.rs.core.*;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.ext.ExceptionMapper;

public abstract class AbstractExceptionMapper<T extends Throwable> implements ExceptionMapper<T> {
    @Override
    public Response toResponse(T exception) {
        return Response.status(status()) //
                .header("X-Status-Detail-Code", code(exception)) //
                .header("X-Status-Detail-Text", message(exception)) //
                .build();
    }

    public abstract Status status();

    private String code(T exception) {
        String simpleName = exception.getClass().getSimpleName();
        return addMinusToCamel(simpleName.replaceAll("Exception$|Throwable$", ""));
    }

    /** Put a minus '-' before every uppercase character... looks more like a http header value ;) */
    private String addMinusToCamel(String string) {
        StringBuilder out = new StringBuilder();
        boolean first = true;
        for (Character c : string.toCharArray()) {
            if (first)
                first = false;
            else if (isUpperCase(c))
                out.append('-');
            out.append(c);
        }
        return out.toString();
    }

    private String message(T exception) {
        return exception.getMessage();
    }
}
