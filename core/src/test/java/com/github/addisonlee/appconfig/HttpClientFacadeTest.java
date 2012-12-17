package com.github.addisonlee.appconfig;

import org.junit.Test;

import java.net.URL;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;

public class HttpClientFacadeTest {
    private HttpClientFacade client = new HttpClientFacade();

    @Test
    public void shouldGetFirstLineOfPageContentOfUrl() throws Exception {
        assertThat(client.getFirstLine(new URL("http://www.google.com"), "testUsername", "testPassword"),
                containsString("<title>Google</title>"));
    }
}