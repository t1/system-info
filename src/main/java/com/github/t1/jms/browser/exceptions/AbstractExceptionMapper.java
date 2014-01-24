package com.github.t1.jms.browser.exceptions;

import static java.lang.Character.*;

import javax.ws.rs.core.*;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.ext.ExceptionMapper;

/**
 * Maps Exceptions to a status code, adding details as header fields for a {@link #STATUS_DETAIL_CODE_HEADER code} and a
 * {@link #STATUS_DETAIL_TEXT_HEADER text}.
 */
public abstract class AbstractExceptionMapper<T extends Throwable> implements ExceptionMapper<T> {
    /** a header field describing in more detail what caused the exception by a fixed error code. */
    public static final String STATUS_DETAIL_CODE_HEADER = "X-Status-Detail-Code";
    /** a header field describing in more detail what caused the exception by human readable text. */
    public static final String STATUS_DETAIL_TEXT_HEADER = "X-Status-Detail-Text";

    @Override
    public Response toResponse(T exception) {
        return Response.status(status(exception)) //
                .header(STATUS_DETAIL_CODE_HEADER, code(exception)) //
                .header(STATUS_DETAIL_TEXT_HEADER, message(exception)) //
                .build();
    }

    /** @return the {@link Status} to be returned */
    public abstract Status status(T exception);

    /**
     * @return the value for the {@link #STATUS_DETAIL_CODE_HEADER}; defaults to the exception class name, minus the
     *         "Exception" or "Throwable" suffix, and camel-case delimited with minuses "-", e.g.
     *         <code>FileNotFoundException</code> becomes <code>File-Not-Found</code>, which looks much more like a HTTP
     *         header.
     */
    protected String code(T exception) {
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

    /** @return the value for the {@link #STATUS_DETAIL_TEXT_HEADER}; defaults to the message of the exception */
    protected String message(T exception) {
        return exception.getMessage();
    }
}
