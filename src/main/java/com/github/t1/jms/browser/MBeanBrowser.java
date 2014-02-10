package com.github.t1.jms.browser;

import static com.github.t1.jms.browser.MBeanBrowser.*;
import static javax.ws.rs.core.MediaType.*;
import static javax.ws.rs.core.Response.Status.*;

import java.lang.management.ManagementFactory;
import java.util.*;

import javax.management.*;
import javax.management.openmbean.CompositeData;
import javax.ws.rs.*;
import javax.ws.rs.core.*;

import org.slf4j.*;

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
        List<String> out = new ArrayList<>();
        for (String domain : server.getDomains()) {
            out.add(domain);
        }
        return Response.ok(out).build();
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
            return badRequest(BEAN_HELP);
        return Response.ok(queryDomains(beanName.getPath(), uri.getQueryParameters())).build();
    }

    public static Response badRequest(String message) {
        return Response.status(BAD_REQUEST).encoding(TEXT_PLAIN).entity(message).build();
    }

    private List<String> queryDomains(String domain, MultivaluedMap<String, String> queryParameters)
            throws MalformedObjectNameException {
        List<String> out = new ArrayList<>();
        String query = toMbeanQuery(queryParameters);
        log.debug("query {} for mbeans: {}", domain, query);
        ObjectName name = new ObjectName(domain + ":" + query);
        for (ObjectName objectName : server.queryNames(name, null)) {
            out.add(objectName.getKeyPropertyListString());
        }
        return out;
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

    @GET
    @Path("{beanName}/-attributes")
    public Response getBeanAttributes(@PathParam("beanName") PathSegment beanName) throws JMException {
        List<String> out = new ArrayList<>();
        for (MBeanAttributeInfo attributeInfo : beanInfo(beanName).getAttributes()) {
            out.add(attributeInfo.getName());
        }
        return Response.ok(out).build();
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
            return notFound("bean " + beanName + " has not attribute " + attributeName);
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

    public static Response notFound(String message) {
        return Response.status(NOT_FOUND).encoding(TEXT_PLAIN).entity(message).build();
    }

    @GET
    @Path("{beanName}/{attributeName:" + NON_META + "}")
    public Response getBeanAttributeByName(@PathParam("beanName") PathSegment beanName,
            @PathParam("attributeName") String attributeName) throws JMException {
        if (log.isDebugEnabled())
            log.debug("get value of {}", findAttributeInfo(beanName, attributeName, beanInfo(beanName)));
        Object value = attributeValue(beanName, attributeName);
        if (value instanceof CompositeData[]) {
            CompositeData[] compositeDatas = (CompositeData[]) value;
            if (compositeDatas.length == 1)
                return Response.ok(compositeDatas[0].getCompositeType().keySet()).build();
            return Response.ok(list(compositeDatas)).build();
        }
        if (value instanceof CompositeData)
            return Response.ok(composite((CompositeData) value)).build();
        return Response.ok(value).build();
    }

    private Object attributeValue(PathSegment beanName, String attributeName) throws JMException {
        try {
            return server.getAttribute(beanName(beanName), attributeName);
        } catch (RuntimeException e) {
            throw new ClientErrorException(badRequest("can't read the attribute '" + attributeName + "' from ["
                    + beanName + "]: " + e));
        }
    }

    private List<String> list(CompositeData[] array) {
        List<String> out = new ArrayList<>();
        for (CompositeData data : array) {
            out.add(data.getCompositeType().keySet().toString());
        }
        return out;
    }

    private List<String> composite(CompositeData composite) {
        List<String> out = new ArrayList<>();
        for (Object key : composite.getCompositeType().keySet()) {
            out.add(Objects.toString(key));
        }
        return out;
    }

    @GET
    @Path("{beanName}/{attributeName:" + NON_META + "}/{elementName}")
    public Response getAttributeElement( //
            @PathParam("beanName") PathSegment beanName, //
            @PathParam("attributeName") String attributeName, //
            @PathParam("elementName") String elementName //
    ) throws JMException {
        Object value = attributeValue(beanName, attributeName);
        log.debug("get {}/{}/{} -> {}", beanName, attributeName, elementName, value);
        if (value instanceof CompositeData[]) {
            CompositeData[] compositeDatas = (CompositeData[]) value;
            if (compositeDatas.length == 1) {
                return Response.ok(compositeDatas[0].get(elementName)).build();
            }
            return Response.ok(list(compositeDatas)).build();
        }
        if (value instanceof CompositeData) {
            return Response.ok(((CompositeData) value).get(elementName)).build();
        }
        return Response.ok(value).build();
    }
}
