package com.github.addisonlee.appconfig;

import org.junit.Before;
import org.junit.Test;

import java.net.URL;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

public class PollerTest {
    private URL url;
    private URL md5Url;
    private int timeoutInMillis = 10;
    private ACAListener listener = mock(ACAListener.class);
    private HttpClientFacade client = mock(HttpClientFacade.class);
    private Poller poller;

    @Before
    public void before() throws Exception {
        url = new URL("http://acadomain:123/config");
        md5Url = new URL("http://acadomain:123/config.md5");
        poller = Poller.testPoller(url, "testuser", "testpassword", timeoutInMillis, listener, client);
    }

    @Test
    public void shouldTriggerCallbackTheFirstTime() throws Exception {
        given(client.getFirstLine(md5Url, "testuser", "testpassword"))
                .willReturn("firstMd5");

        whenThePollerRunsFor(poller, timeoutInMillis * 2);

        verify(listener, times(1)).updateConfig();
    }

    @Test
    public void shouldNotTriggerCallbackIfTheMd5HasNotChanged() throws Exception {
        given(client.getFirstLine(md5Url, "testuser", "testpassword"))
                .willReturn("firstMd5")
                .willReturn("firstMd5");

        whenThePollerRunsFor(poller, timeoutInMillis * 3);

        verify(listener, times(1)).updateConfig();
    }

    @Test
    public void shouldTriggerCallbackWhenTheMd5HasChanged() throws Exception {
        given(client.getFirstLine(md5Url, "testuser", "testpassword"))
                .willReturn("firstMd5")
                .willReturn("differentMd5");

        whenThePollerRunsFor(poller, timeoutInMillis * 3);

        verify(listener, times(2)).updateConfig();
    }

    @Test
    public void shouldTriggerCallbackEveryTimeTheMd5HasChanged() throws Exception {
        given(client.getFirstLine(md5Url, "testuser", "testpassword"))
                .willReturn("firstMd5")
                .willReturn("secondMd5")
                .willReturn("secondMd5")
                .willReturn("thirdMd5")
                .willReturn("thirdMd5");

        whenThePollerRunsFor(poller, timeoutInMillis * 5);

        verify(listener, times(3)).updateConfig();
    }

    private void whenThePollerRunsFor(Poller poller, int millis) throws InterruptedException {
        Thread daemon = new Thread(poller);
        daemon.setDaemon(true);
        daemon.start();
        Thread.sleep(millis);
        poller.stop();
    }
}
