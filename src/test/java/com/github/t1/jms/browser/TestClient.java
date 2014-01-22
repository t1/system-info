package com.github.t1.jms.browser;

import static javax.ws.rs.core.MediaType.*;

import javax.ws.rs.client.*;
import javax.ws.rs.core.Response;

public class TestClient {
    public static void main(String[] args) {
        Client client = ClientBuilder.newClient();
        WebTarget target = client.target("http://localhost:8080/-system/test/");
        WebTarget request = target.path("{domain}");
        request = request.matrixParam("name", "direct");
        request = request.matrixParam("type", "BufferPool");
        request = request.resolveTemplate("domain", "java.nio");
        System.out.println("request: " + request.getUri());
        Invocation.Builder invocation = request.request(APPLICATION_JSON);
        invocation.header("x", "y");
        Response response = invocation.get();
        System.out.println("status: " + response.getStatusInfo());
        System.out.println(response.readEntity(String.class));
    }
}
