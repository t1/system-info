package com.github.t1.jms.browser.exceptions;

import static javax.ws.rs.core.Response.Status.*;

import javax.management.MalformedObjectNameException;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.ext.Provider;

@Provider
public class MalformedObjectNameExceptionMapper extends AbstractExceptionMapper<MalformedObjectNameException> {
    @Override
    public Status status() {
        return BAD_REQUEST;
    }
}
