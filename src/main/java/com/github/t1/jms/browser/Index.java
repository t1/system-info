package com.github.t1.jms.browser;

import static com.github.t1.jms.browser.JndiResource.*;
import static com.github.t1.jms.browser.MBeanResource.*;
import static com.github.t1.jms.browser.QueuesResource.*;
import static com.github.t1.jms.browser.SystemPropertiesResource.*;

import java.net.URI;
import java.util.*;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.Response;

import com.github.t1.log.Logged;
import com.github.t1.webresource.accessors.*;
import com.github.t1.webresource.codec2.BasePath;

@Path("/")
@Logged
public class Index {
    @Inject
    private BasePath basePath;
    @Inject
    private MetaDataStore metaDataStore;

    @GET
    public Response index() {
        Map<String, Object> map = new LinkedHashMap<>();
        metaDataStore.put(map, new MapMetaData("System-Info Index", "Name", "Value"));

        map.put("server", serverName());
        map.put("system-properties", link(SYSTEMPROPERTIES, "System-Properties"));
        map.put("jndi", link(JNDI, "Root"));
        map.put("mbeans", link(MBEANS, "MBeans"));
        map.put("queues", link(QUEUES, "Queues"));

        return Response.ok(map).build();
    }

    private String serverName() {
        String glassfishVersion = System.getProperty("glassfish.version");
        if (glassfishVersion != null)
            return glassfishVersion;
        return "unknown";
    }

    private URI link(String path, String title) {
        URI uri = basePath.resolve(path);
        metaDataStore.put(uri, new UriMetaData(title));
        return uri;
    }
}
