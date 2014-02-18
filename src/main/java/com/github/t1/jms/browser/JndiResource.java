package com.github.t1.jms.browser;

import java.net.URI;
import java.util.*;

import javax.inject.Inject;
import javax.naming.*;
import javax.ws.rs.*;
import javax.ws.rs.core.Response;

import com.github.t1.webresource.accessors.*;
import com.github.t1.webresource.codec2.BasePath;

@Path(JndiResource.JNDI)
public class JndiResource {
    public static final String JNDI = "jndi";

    @Inject
    private BasePath basePath;
    @Inject
    private MetaDataStore metaDataStore;

    @GET
    public Response jndi() throws NamingException {
        return getJndi("");
    }

    @GET
    @Path("{path:.+}")
    public Response jndi(@PathParam("path") String path) throws NamingException {
        return getJndi(path);
    }

    private Response getJndi(String path) throws NamingException {
        javax.naming.Context context = new InitialContext();
        Object object = context.lookup(path);
        if (isSubContext(object)) {
            List<Object> list = listSubContext(context, path);
            return Response.ok(list).build();
        } else {
            return Response.ok(object).build();
        }
    }

    private boolean isSubContext(Object object) {
        return javax.naming.Context.class.isAssignableFrom(object.getClass());
    }

    private List<Object> listSubContext(javax.naming.Context context, String path) throws NamingException {
        List<Object> out = new ArrayList<>();
        NamingEnumeration<Binding> list = context.listBindings(path);
        while (list.hasMoreElements()) {
            Binding binding = list.nextElement();
            out.add(link(path, binding));
        }
        metaDataStore.put(out, new ListMetaData(path));
        return out;
    }

    private URI link(String path, Binding binding) {
        String name = binding.getName();
        URI uri = basePath.resolve(JNDI + "/" + path + "/" + name);
        metaDataStore.put(uri, new UriMetaData(name));
        return uri;
    }
}
