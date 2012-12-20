package com.github.addisonlee.appconfig;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.filter.HTTPBasicAuthFilter;

import java.io.IOException;
import java.net.URL;
import java.util.logging.Logger;

import static java.util.logging.Level.WARNING;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON_TYPE;

public class HttpClientFacade {
    private static Logger logger = Logger.getLogger(HttpClientFacade.class.getName());
    Client client = Client.create();

    public String get(URL url, String username, String password) throws IOException {
        ClientResponse response = getResponse(url.toString(), username, password);
        if (ClientResponse.Status.OK == response.getClientResponseStatus()) {
            return response.getEntity(String.class);
        } else {
            logger.log(WARNING, "Could not obtain configuration hash: HTTP Status " + response.getClientResponseStatus());
            return null;
        }
    }

    private ClientResponse getResponse(String url, String username, String password) throws IOException {
        client.removeAllFilters();
        client.addFilter(new HTTPBasicAuthFilter(username, password));
        return client.resource(url).accept(APPLICATION_JSON_TYPE).get(ClientResponse.class);
    }
}
