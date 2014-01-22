package com.github.t1.jms.browser;

import static com.github.t1.jms.browser.TestResource.*;
import static javax.ws.rs.core.MediaType.*;
import static javax.ws.rs.core.Response.Status.*;

import java.lang.management.ManagementFactory;
import java.util.List;

import javax.management.*;
import javax.ws.rs.*;
import javax.ws.rs.core.*;

import org.slf4j.*;

import com.google.common.collect.ImmutableList;

@Path(ROOT)
public class TestResource {
    public static final String ROOT = "test";

    private final Logger log = LoggerFactory.getLogger(TestResource.class);

    @javax.ws.rs.core.Context
    private UriInfo uri;

    MBeanServer server = ManagementFactory.getPlatformMBeanServer();

    @GET
    public Response getDomains() throws JMException {
        ImmutableList.Builder<String> out = ImmutableList.builder();
        for (String domain : server.getDomains()) {
            out.add(domain);
        }
        return Response.ok(out.build()).build();
    }

    @GET
    @Produces(TEXT_PLAIN)
    public Response getDomainsAsTextPlain() throws JMException {
        StringBuilder out = new StringBuilder();
        for (String domain : server.getDomains()) {
            out.append(domain).append("\n");
        }
        return Response.ok(out.toString()).build();
    }

    @GET
    @Path("{beanName}")
    public Response getBeanInfo(@PathParam("beanName") PathSegment beanName) throws MalformedObjectNameException,
            IntrospectionException, InstanceNotFoundException, ReflectionException {
        if (beanName.getMatrixParameters().isEmpty())
            return Response.ok(queryDomains(beanName.getPath(), uri.getQueryParameters())).build();
        return Response.ok(info(beanName)).build();
    }

    private List<String> queryDomains(String domain, MultivaluedMap<String, String> queryParameters)
            throws MalformedObjectNameException {
        ImmutableList.Builder<String> out = ImmutableList.builder();
        String query = toMbeanQuery(queryParameters);
        log.debug("query {} for mbeans: {}", domain, query);
        ObjectName name = new ObjectName(domain + ":" + query);
        for (ObjectName objectName : server.queryNames(name, null)) {
            out.add(objectName.getKeyPropertyListString());
        }
        return out.build();
    }

    private String toMbeanQuery(MultivaluedMap<String, String> urlQuery) {
        if (urlQuery.isEmpty())
            return "*";
        StringBuilder out = new StringBuilder();
        for (String key : urlQuery.keySet()) {
            if (out.length() > 0)
                out.append(",");
            out.append(key);
            String value = urlQuery.getFirst(key);
            if (!isFullWildcard(key, value)) {
                out.append("=").append(value);
            }
        }
        return out.toString();
    }

    private boolean isFullWildcard(String key, String value) {
        return "*".equals(key) && value.isEmpty();
    }

    @GET
    @Path("{beanName}/description")
    public Response getBeanDescription(@PathParam("beanName") PathSegment beanName) throws JMException {
        return Response.ok(info(beanName).getDescription()).build();
    }

    @GET
    @Path("{beanName}/attributes")
    public Response getBeanAttributes(@PathParam("beanName") PathSegment beanName) throws JMException {
        return Response.ok(info(beanName).getAttributes()).build();
    }

    @GET
    @Path("{beanName}/attributes/{attribute}")
    public Response getBeanAttributeByName(@PathParam("beanName") PathSegment beanName,
            @PathParam("attribute") String attribute) throws JMException {
        for (MBeanAttributeInfo info : info(beanName).getAttributes()) {
            if (attribute.equals(info.getName())) {
                return Response.ok(info).build();
            }
        }
        return Response.status(NOT_FOUND).build();
    }

    private MBeanInfo info(PathSegment pathSegment) throws MalformedObjectNameException, IntrospectionException,
            InstanceNotFoundException, ReflectionException {
        String beanName = beanName(pathSegment);
        System.out.println("get info for " + beanName);
        ObjectName objectName = ObjectName.getInstance(beanName);
        return server.getMBeanInfo(objectName);
    }

    private String beanName(PathSegment segment) {
        StringBuilder beanName = new StringBuilder();
        String domain = segment.getPath();
        beanName.append(domain).append(":");
        MultivaluedMap<String, String> matrix = segment.getMatrixParameters();
        boolean first = true;
        for (String key : matrix.keySet()) {
            if (first) {
                first = false;
            } else {
                beanName.append(",");
            }
            beanName.append(key).append("=").append(matrix.getFirst(key));
        }
        return beanName.toString();
    }
}
