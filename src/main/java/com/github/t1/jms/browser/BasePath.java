package com.github.t1.jms.browser;

import java.net.URI;

import javax.inject.Inject;
import javax.ws.rs.core.*;

public class BasePath {
    @Inject
    private UriInfo uriInfo;

    public URI resolve(String path) {
        return baseUri().resolve(path);
    }

    public URI baseUri() {
        URI baseUri = uriInfo.getBaseUri();
        String string = baseUri.toASCIIString();
        if (!string.endsWith("/"))
            string = string + "/";
        return URI.create(string);
    }

    public String link(String path, String label) {
        return "<a href=\"" + resolve(path) + "\">" + label + "</a>";
    }
}