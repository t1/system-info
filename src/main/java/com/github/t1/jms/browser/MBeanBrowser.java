package com.github.t1.jms.browser;

import static com.github.t1.jms.browser.MBeanBrowser.*;
import static javax.ws.rs.core.MediaType.*;
import static javax.ws.rs.core.Response.Status.*;

import java.lang.management.ManagementFactory;
import java.util.List;

import javax.management.*;
import javax.ws.rs.*;
import javax.ws.rs.core.*;

import org.slf4j.*;

import com.google.common.collect.ImmutableList;

/**
 * Exposes MBeans with their attributes and meta data, but not (yet) notifications, operations, or constructors. Paths
 * that refer to some meta data have path segment starting with a minus.
 */
@Path(MBEANS)
public class MBeanBrowser {
    public static final String MBEANS = "mbeans";

    private static final String BEAN_HELP = "" //
            + "You have requested an MBean directly using matrix parameters.\n" //
            + "To search for beans, use query parameters.\n" //
            + "To list all possible attributes, append '-attributes' to the path.\n" //
            + "To get the value of one attribute, append it's name to the path.\n" //
            + "To get all meta data about the bean, append '-info' to the path.\n" //
            + "To get the textual description of the bean, append '-description' to the path.\n" //
    ;

    private static final String NON_META = "[^-/][^/]*";

    private final Logger log = LoggerFactory.getLogger(MBeanBrowser.class);

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
    public Response queryBeanNames(@PathParam("beanName") PathSegment beanName) throws JMException {
        if (!beanName.getMatrixParameters().isEmpty())
            return Response.status(BAD_REQUEST).entity(BEAN_HELP).encoding(TEXT_PLAIN).build();
        return Response.ok(queryDomains(beanName.getPath(), uri.getQueryParameters())).build();
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
        log.debug("url query: {}", urlQuery);
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
    @Path("{beanName}/-description")
    public Response getBeanDescription(@PathParam("beanName") PathSegment beanName) throws JMException {
        return Response.ok(beanInfo(beanName).getDescription()).build();
    }

    @GET
    @Path("{beanName}/-attributes")
    public Response getBeanAttributes(@PathParam("beanName") PathSegment beanName) throws JMException {
        ImmutableList.Builder<String> out = ImmutableList.builder();
        for (MBeanAttributeInfo attributeInfo : beanInfo(beanName).getAttributes()) {
            out.add(attributeInfo.getName());
        }
        return Response.ok(out.build()).build();
    }

    @GET
    @Path("{beanName}/-info")
    public Response getBeanInfo(@PathParam("beanName") PathSegment beanName) throws JMException {
        return Response.ok(beanInfo(beanName)).build();
    }

    @GET
    @Path("{beanName}/{attributeName:" + NON_META + "}/-info")
    public Response getAttributeInfo(@PathParam("beanName") PathSegment beanName,
            @PathParam("attributeName") String attributeName) throws JMException {
        MBeanInfo beanInfo = beanInfo(beanName);
        log.debug("get info for attribute {} of {}", attributeName, beanName);
        MBeanAttributeInfo attributeInfo = findAttributeInfo(beanName, attributeName, beanInfo);
        if (attributeInfo == null)
            return Response.status(NOT_FOUND).build();
        return Response.ok(attributeInfo).build();
    }

    private MBeanAttributeInfo findAttributeInfo(PathSegment beanName, String attributeName, MBeanInfo beanInfo) {
        for (MBeanAttributeInfo attributeInfo : beanInfo.getAttributes()) {
            if (attributeName.equals(attributeInfo.getName())) {
                return attributeInfo;
            }
        }
        return null;
    }

    @GET
    @Path("{beanName}/{attributeName:" + NON_META + "}")
    public Response getBeanAttributeByName(@PathParam("beanName") PathSegment beanName,
            @PathParam("attributeName") String attributeName) throws JMException {
        Object value = server.getAttribute(beanName(beanName), attributeName);
        return Response.ok(value).build();
    }

    private MBeanInfo beanInfo(PathSegment pathSegment) throws JMException {
        ObjectName beanName = beanName(pathSegment);
        log.debug("get bean info for " + beanName);
        return server.getMBeanInfo(beanName);
    }

    private ObjectName beanName(PathSegment segment) throws MalformedObjectNameException {
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
        return ObjectName.getInstance(beanName.toString());
    }
}
