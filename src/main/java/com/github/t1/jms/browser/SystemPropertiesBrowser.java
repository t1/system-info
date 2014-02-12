package com.github.t1.jms.browser;

import javax.ws.rs.*;
import javax.ws.rs.core.Response;

@Path(SystemPropertiesBrowser.SYSTEMPROPERTIES)
public class SystemPropertiesBrowser {
    public static final String SYSTEMPROPERTIES = "system-properties";

    @GET
    public Response systemProperties() {
        return Response.ok(System.getProperties()).build();
    }
}
