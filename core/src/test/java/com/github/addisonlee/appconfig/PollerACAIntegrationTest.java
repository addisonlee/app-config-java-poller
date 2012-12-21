package com.github.addisonlee.appconfig;

import com.github.addisonlee.appconfig.testclasses.ClientConfiguration;
import com.github.addisonlee.appconfig.testclasses.ClientConfigurationACAListener;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.filter.HTTPBasicAuthFilter;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.node.ObjectNode;
import org.junit.Test;

import javax.ws.rs.core.MediaType;
import java.io.IOException;

import static java.lang.String.format;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

/**
 * Will probably want to eventually move this to a separate module which starts up the ACA as part of the build.
 */
public class PollerACAIntegrationTest {
    private final String url = "http://localhost:9292/dev/instruments_configuration";
    private final String username = "alee";
    private final String password = "alee";
    private final int pollerTimeoutInMillis = 10;

    @Test
    public void pollerShouldUpdateClientConfigurationViaACAListenerWhenACAConfigurationIsChanged() throws Exception {
        Client client = Client.create();
        // TODO: create a perforce user as part of test setup
        client.addFilter(new HTTPBasicAuthFilter(username, password));
        ClientConfiguration clientConfiguration = new ClientConfiguration();
        ClientConfigurationACAListener listener = new ClientConfigurationACAListener(clientConfiguration);
        Poller poller = new Poller(url, username, password, pollerTimeoutInMillis, listener);

        setACAConfiguration(client, "Tuba");
        startPoller(poller);
        Thread.sleep(pollerTimeoutInMillis * 10);
        assertConfig(clientConfiguration, "Tuba");
        setACAConfiguration(client, "Applause");
        Thread.sleep(pollerTimeoutInMillis * 10);
        assertConfig(clientConfiguration, "Applause");

        poller.stop();
    }

    private void setACAConfiguration(Client client, String instrumentName) {
        client.resource(url + ".json")
                .accept(MediaType.APPLICATION_JSON_TYPE)
                .entity(format("{\"instrument\":\"%s\"}", instrumentName))
                .post();
    }

    private void startPoller(Poller poller) {
        Thread daemon = new Thread(poller);
        daemon.setDaemon(true);
        daemon.start();
    }

    private void assertConfig(ClientConfiguration clientConfiguration, String expectedValue) throws IOException {
        ObjectNode node = new ObjectMapper().readValue(clientConfiguration.getValue(), ObjectNode.class);
        assertThat(node.get("instrument").asText(), is(expectedValue));
    }
}
