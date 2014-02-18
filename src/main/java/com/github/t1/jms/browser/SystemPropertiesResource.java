package com.github.t1.jms.browser;

import java.net.URI;
import java.util.*;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.Response;

import org.slf4j.*;

import com.github.t1.log.Logged;
import com.github.t1.webresource.accessors.*;
import com.github.t1.webresource.codec2.BasePath;

@Path(SystemPropertiesResource.SYSTEMPROPERTIES)
public class SystemPropertiesResource {
    private static final Logger logger = LoggerFactory.getLogger(SystemPropertiesResource.class);

    public static final String SYSTEMPROPERTIES = "system-properties";

    @Inject
    private BasePath basePath;
    @Inject
    private MetaDataStore metaDataStore;

    @GET
    public Response listAll() {
        List<URI> list = new ArrayList<>();
        metaDataStore.put(list, new ListMetaData("System Properties"));
        Properties properties = System.getProperties();
        for (String key : properties.stringPropertyNames()) {
            URI uri = basePath.resolve(SYSTEMPROPERTIES + "/" + key);
            metaDataStore.put(uri, new UriMetaData(key));
            list.add(uri);
        }
        return Response.ok(list).build();
    }

    @GET
    @Path("{name:.*}")
    @Logged
    public Response get(@PathParam("name") String name) {
        logger.debug("get system property '{}'", name);
        String value = System.getProperty(name);
        return Response.ok(value).build();
    }
}
