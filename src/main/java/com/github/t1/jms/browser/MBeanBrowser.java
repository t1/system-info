package com.github.t1.jms.browser;

import static com.github.t1.jms.browser.MBeanBrowser.*;
import static javax.ws.rs.core.MediaType.*;
import static javax.ws.rs.core.Response.Status.*;

import java.lang.management.ManagementFactory;

import javax.management.*;
import javax.ws.rs.*;
import javax.ws.rs.core.*;

import org.slf4j.*;

import com.google.common.collect.ImmutableList;

@Path(MBEANS)
public class MBeanBrowser {
    public static final String MBEANS = "mbeans";

    private final Logger log = LoggerFactory.getLogger(MBeanBrowser.class);

    @javax.ws.rs.core.Context
    private UriInfo context;

    MBeanServer server = ManagementFactory.getPlatformMBeanServer();

    @GET
    public Response domains() throws JMException {
        log.warn("list domains");
        ImmutableList.Builder<String> out = ImmutableList.builder();
        for (String domain : server.getDomains()) {
            out.add(domain);
        }
        return Response.ok(out.build()).build();
    }

    @GET
    @Path("{domain}")
    public Response names(@PathParam("domain") String domain) throws JMException {
        ImmutableList.Builder<String> out = ImmutableList.builder();
        ObjectName name = new ObjectName(domain + ":" + query());
        for (ObjectName objectName : server.queryNames(name, null)) {
            out.add(objectName.getKeyPropertyListString());
        }
        return Response.ok(out.build()).build();
    }

    private String query() {
        MultivaluedMap<String, String> urlQuery = context.getQueryParameters();
        if (urlQuery.isEmpty())
            return "*";
        StringBuilder out = new StringBuilder();
        for (String key : urlQuery.keySet()) {
            if (out.length() > 0)
                out.append(",");
            out.append(key).append("=").append(urlQuery.getFirst(key));
        }
        return out.toString();
    }

    @GET
    @Path("{domain}/{beanName}")
    public Response object(@PathParam("domain") String domain, @PathParam("beanName") String beanName)
            throws JMException {
        MBeanInfo info = info(domain, beanName);

        return Response.ok(info).build();
    }

    private MBeanInfo info(String domain, String beanName) throws MalformedObjectNameException, ReflectionException,
            IntrospectionException, InstanceNotFoundException {
        ObjectName objectName = ObjectName.getInstance(domain + ":" + beanName);
        return server.getMBeanInfo(objectName);
    }

    @GET
    @Path("{domain}/{beanName}/attributes")
    public Response attributes(@PathParam("domain") String domain, @PathParam("beanName") String beanName)
            throws JMException {
        MBeanInfo info = info(domain, beanName);

        return Response.ok(ImmutableList.copyOf(info.getAttributes())).build();
    }

    @GET
    @Path("{domain}/{beanName}/attributes")
    @Produces(TEXT_PLAIN)
    public String attributeNamesAsText(@PathParam("domain") String domain, @PathParam("beanName") String beanName)
            throws JMException {
        MBeanInfo info = info(domain, beanName);

        StringBuilder out = new StringBuilder();
        for (MBeanAttributeInfo attribute : info.getAttributes()) {
            out.append(attribute.getName()).append("\n");
        }
        return out.toString();
    }

    @GET
    @Path("{domain}/{beanName}/attributes/{attributeName}")
    public Response attributeValue(@PathParam("domain") String domain, @PathParam("beanName") String beanName,
            @PathParam("attributeName") String attributeName) throws JMException {
        ObjectName objectName = ObjectName.getInstance(domain + ":" + beanName);
        MBeanInfo info = info(domain, beanName);
        System.out.println("   -----> " + info.getClassName());
        if ("[Ljavax.management.openmbean.CompositeData;".equals(info.getClassName())) {
            return Response.ok("composite").build();
        } else {
            Object bean = server.getAttribute(objectName, attributeName);
            return Response.ok(bean).build();
        }
    }

    @GET
    @Path("{domain}/{beanName}/attributes/{attributeName}/info")
    public Response attribute(@PathParam("domain") String domain, @PathParam("beanName") String beanName,
            @PathParam("attributeName") String attributeName) throws JMException {
        for (MBeanAttributeInfo attribute : info(domain, beanName).getAttributes()) {
            if (attributeName.equals(attribute.getName())) {
                return Response.ok(attribute).build();
            }
        }
        return Response.status(NOT_FOUND).build();
    }
}
