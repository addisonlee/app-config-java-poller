package com.github.addisonlee.appconfig;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.net.URL;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

@RunWith(MockitoJUnitRunner.class)
public class PollerTest {
    private URL md5Url;
    private URL configUrl;
    private int timeoutInMillis = 5;
    @Mock
    private ACAListener listener;
    @Mock
    private HttpClientFacade client;

    private Poller poller;

    @Before
    public void before() throws Exception {
        String url = "http://acadomain:123/config";
        md5Url = new URL(url + ".md5");
        configUrl = new URL(url + ".json");
        poller = Poller.testPoller(new URL(url), "testuser", "testpassword", timeoutInMillis, listener, client);
    }

    @Test
    public void shouldTriggerCallbackTheFirstTime() throws Exception {
        given(client.get(md5Url, "testuser", "testpassword"))
                .willReturn("firstMd5");
        given(client.get(configUrl, "testuser", "testpassword"))
                .willReturn("expected config content");

        whenThePollerRunsFor(poller, timeoutInMillis * 2);

        verify(listener, times(1)).updateConfig("expected config content");
        verifyNoMoreInteractions(listener);
    }

    @Test
    public void shouldNotTriggerCallbackIfTheMd5HasNotChanged() throws Exception {
        given(client.get(md5Url, "testuser", "testpassword"))
                .willReturn("firstMd5")
                .willReturn("firstMd5");
        given(client.get(configUrl, "testuser", "testpassword"))
                .willReturn("expected config content");

        whenThePollerRunsFor(poller, timeoutInMillis * 3);

        verify(listener, times(1)).updateConfig("expected config content");
        verifyNoMoreInteractions(listener);
    }

    @Test
    public void shouldTriggerCallbackEveryTimeTheMd5HasChanged() throws Exception {
        given(client.get(md5Url, "testuser", "testpassword"))
                .willReturn("firstMd5")
                .willReturn("secondMd5")
                .willReturn("secondMd5")
                .willReturn("thirdMd5")
                .willReturn("thirdMd5");
        given(client.get(configUrl, "testuser", "testpassword"))
                .willReturn("first config content")
                .willReturn("second config content")
                .willReturn("third config content");

        whenThePollerRunsFor(poller, timeoutInMillis * 6);

        verify(listener, times(1)).updateConfig("first config content");
        verify(listener, times(1)).updateConfig("second config content");
        verify(listener, times(1)).updateConfig("third config content");
        verifyNoMoreInteractions(listener);
    }

    private void whenThePollerRunsFor(Poller poller, int millis) throws InterruptedException {
        Thread daemon = new Thread(poller);
        daemon.setDaemon(true);
        daemon.start();
        Thread.sleep(millis);
        poller.stop();
    }
}
