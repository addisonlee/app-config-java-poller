package com.github.addisonlee.appconfig;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.filter.HTTPBasicAuthFilter;

import java.io.IOException;

import static com.sun.jersey.api.client.ClientResponse.Status.OK;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON_TYPE;

public class HttpClientFacade {
    Client client = Client.create();

    public String get(String url, String username, String password) throws IOException {
        ClientResponse response = getResponse(url, username, password);
        assertStatus(OK, response);
        return response.getEntity(String.class);
    }

    private ClientResponse getResponse(String url, String username, String password) throws IOException {
        client.removeAllFilters();
        client.addFilter(new HTTPBasicAuthFilter(username, password));
        return client.resource(url).accept(APPLICATION_JSON_TYPE).get(ClientResponse.class);
    }

    private void assertStatus(ClientResponse.Status status, ClientResponse response) {
        if (status != response.getClientResponseStatus()) {
            throw new RuntimeException("Could not obtain configuration hash: HTTP Status " + response.getClientResponseStatus());
        }
    }
}
