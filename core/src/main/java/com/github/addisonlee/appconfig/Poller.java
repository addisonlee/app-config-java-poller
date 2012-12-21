package com.github.addisonlee.appconfig;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.logging.Logger;

import static java.lang.String.format;
import static java.util.logging.Level.INFO;
import static java.util.logging.Level.SEVERE;

public class Poller implements Runnable {
    private static Logger logger = Logger.getLogger(Poller.class.getName());

    private final long timeoutInMillis;
    private final ACAListener listener;
    private final String url;
    private final String username;
    private final String password;

    private HttpClientFacade client = new HttpClientFacade();
    private boolean keepGoing = true;
    private String hash = null;

    public Poller(String url, String username, String password, int timeoutInMillis, ACAListener listener) throws MalformedURLException {
        this.timeoutInMillis = timeoutInMillis;

        this.url = url;
        this.username = username;
        this.password = password;

        this.listener = listener;
    }

    static Poller testPoller(String url, String username, String password, int timeoutInMillis, ACAListener listener, HttpClientFacade client) throws MalformedURLException {
        Poller poller = new Poller(url, username, password, timeoutInMillis, listener);
        poller.client = client;
        return poller;
    }

    @Override
    public void run() {
        while (keepGoing) {
            try {
                processACAUpdates();
            } catch (Exception e) {
                logger.log(SEVERE, null, e);
            }
            try {
                Thread.sleep(timeoutInMillis);
            } catch (InterruptedException exception) {
                // intentionally empty
            }
        }
    }

    private void processACAUpdates() throws IOException {
        String newHash = client.get(url + ".md5", username, password);
        if (!newHash.equals(hash)) {
            String newConfig = client.get(url + ".json", username, password);
            logger.log(INFO, format("Hash has changed from '%s' to '%s'.\nUpdating ACAListener with new config: '%s'", hash, newHash, newConfig));
            hash = newHash;
            listener.updateConfig(newConfig);
        }
    }

    public void stop() {
        keepGoing = false;
    }
}