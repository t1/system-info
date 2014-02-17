package com.github.t1.jms.browser;

import java.net.URI;
import java.util.*;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.Response;

import com.github.t1.webresource.codec2.BasePath;

@Path(SystemPropertiesBrowser.SYSTEMPROPERTIES)
public class SystemPropertiesBrowser {
    public static final String SYSTEMPROPERTIES = "system-properties";

    @Inject
    private BasePath basePath;

    @GET
    public Response listAll() {
        List<URI> list = new ArrayList<>();
        Properties properties = System.getProperties();
        for (String key : properties.stringPropertyNames()) {
            list.add(basePath.resolve(key));
        }
        return Response.ok(list).build();
    }
    //
    // @GET
    // @Path("{name}")
    // public Response get(@PathParam("name") String name) {
    // String value = System.getProperty(name);
    // return Response.ok(value).build();
    // }
}
