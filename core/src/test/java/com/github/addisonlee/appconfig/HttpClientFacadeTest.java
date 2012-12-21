package com.github.addisonlee.appconfig;

import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;

public class HttpClientFacadeTest {
    private HttpClientFacade client = new HttpClientFacade();

    @Test
    public void shouldReturnPageContentOfUrl() throws Exception {
        assertThat(client.get("http://www.google.com", "testUsername", "testPassword"),
                containsString("I'm Feeling Lucky"));
    }
}
