package com.github.t1.jms.browser.exceptions;

import static javax.ws.rs.core.Response.Status.*;

import javax.management.InstanceNotFoundException;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.ext.Provider;

@Provider
public class InstanceNotFoundExceptionMapper extends AbstractExceptionMapper<InstanceNotFoundException> {
    @Override
    public Status status() {
        return NOT_FOUND;
    }
}
