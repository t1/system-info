package com.github.t1.jms.browser;

import javax.ws.rs.core.Response;

import com.github.t1.log.*;

@ConverterType(Response.class)
class ResponseConverter implements Converter {
    @Override
    public String convert(Object object) {
        Response response = (Response) object;
        return "Response[" + response.getStatusInfo() + ":" + response.getMediaType() + ":" + response.getLength()
                + "]:\n" + response.getEntity();
    }
}
