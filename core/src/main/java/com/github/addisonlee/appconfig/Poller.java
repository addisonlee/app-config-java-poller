package com.github.addisonlee.appconfig;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.logging.Logger;

import static java.util.logging.Level.SEVERE;

public class Poller implements Runnable {
    private static Logger logger = Logger.getLogger(Poller.class.getName());

    private final long timeoutInMillis;
    private final ACAListener listener;
    private final URL url;
    private final String username;
    private final String password;

    private HttpClientFacade client = new HttpClientFacade();
    private boolean keepGoing = true;
    private String hash = null;

    public Poller(URL url, String username, String password, int timeoutInMillis, ACAListener listener) throws MalformedURLException {
        this.timeoutInMillis = timeoutInMillis;

        this.url = url;
        this.username = username;
        this.password = password;

        this.listener = listener;
    }

    static Poller testPoller(URL url, String username, String password, int timeoutInMillis, ACAListener listener, HttpClientFacade client) throws MalformedURLException {
        Poller poller = new Poller(url, username, password, timeoutInMillis, listener);
        poller.client = client;
        return poller;
    }

    @Override
    public void run() {
        while (keepGoing) {
            try {
                String newHash = getMd5();
                System.out.println(newHash);
                if (newHash != null && !newHash.equals(hash)) {
                    hash = newHash;
                    listener.updateConfig(getConfig());
                }
            } catch (IOException e) {
                logger.log(SEVERE, null, e);
            }
            try {
                Thread.sleep(timeoutInMillis);
            } catch (InterruptedException exception) {
                // intentionally empty
            }
        }
    }

    public void stop() {
        keepGoing = false;
    }

    @Deprecated // will be removed shortly
    public String getMd5() throws IOException {
        return client.getFirstLine(new URL(url.toString() + ".md5"), username, password);
    }

    private String getConfig() throws IOException {
        return client.getAll(new URL(url.toString() + ".json"), username, password);
    }
}