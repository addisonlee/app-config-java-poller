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
 * This test only runs within IntelliJ. I'm purposely omitting it from the command line mvn build because
 * it requires a running ACA with the correct Perforce user set up. Currently the perforce user is hardcoded to "alee"
 * on my machine, but the perforce user should be created as part of the test setup.
 *
 * My current thinking is to create a deploy/test script which:
 * 1. builds app-config-app and app-config-java-poller/core
 * 2. configures a perforce user
 * 3. runs this test
 *
 * In the meantime, this test simply serves as a convenient way to manually check that you didn't break anything, and
 * it is a stepping stone towards automated test coverage of the ACA suite of products.
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
        assertThat(false, is(true));
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
