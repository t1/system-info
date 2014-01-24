package com.github.t1.jms.browser.exceptions;

import static javax.ws.rs.core.Response.Status.*;

import javax.management.JMException;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.ext.Provider;

@Provider
public class JMExceptionMapper extends AbstractExceptionMapper<JMException> {
    @Override
    public Status status(JMException e) {
        String name = e.getClass().getSimpleName();
        if (name.endsWith("NotFoundException"))
            return NOT_FOUND;
        return BAD_REQUEST;
    }
}
