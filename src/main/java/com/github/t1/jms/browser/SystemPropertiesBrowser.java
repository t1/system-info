package com.github.t1.jms.browser;

import java.util.Properties;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.Response;

import com.github.t1.webresource.meta2.*;

@Path(SystemPropertiesBrowser.SYSTEMPROPERTIES)
public class SystemPropertiesBrowser {
    public static final String SYSTEMPROPERTIES = "system-properties";

    @Inject
    private MetaDataStore metaData;

    @GET
    public Response systemProperties() {
        Properties properties = System.getProperties();
        metaData.put(properties, new MapMetaData("System Properties", "Name", "Value"));
        return Response.ok(properties).build();
    }
}
