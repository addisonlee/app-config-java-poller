package com.github.addisonlee.appconfig;

import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.AuthCache;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.protocol.ClientContext;
import org.apache.http.impl.auth.BasicScheme;
import org.apache.http.impl.client.BasicAuthCache;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.BasicHttpContext;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Poller implements Runnable {
    private static Logger logger = Logger.getLogger(Poller.class.getName());

    private String hash;
    private boolean keepGoing;
    private final DefaultHttpClient httpClient;
    private final HttpHost host;
    private final BasicHttpContext context = new BasicHttpContext();
    private HttpGet hashGet;
    private HttpGet configGet;
    private long timeout;
    private ACAListener listener;
    private HttpClientFacade client;
    private URL url;
    private String username;
    private String password;
    private URL md5Url;

    @Deprecated // will be removed shortly
    public Poller(URL url, String username, String password, int timeoutSeconds) throws MalformedURLException {
        host = new HttpHost(url.getHost(), url.getPort(), url.getProtocol());
        httpClient = new DefaultHttpClient();
        httpClient.getCredentialsProvider().setCredentials(new AuthScope(host.getHostName(), host.getPort()),
                new UsernamePasswordCredentials(username, password));
        AuthCache cache = new BasicAuthCache();
        cache.put(host, new BasicScheme());
        context.setAttribute(ClientContext.AUTH_CACHE, cache);
        hashGet = new HttpGet(url.toString().replace(".json", ".md5"));
        md5Url = new URL(url.toString().replace(".json", ".md5"));
        configGet = new HttpGet(url.toString());
        keepGoing = true;
        timeout = timeoutSeconds * 1000;
    }

    public Poller(URL url, String username, String password, int timeoutSeconds, ACAListener listener) throws MalformedURLException {
        this(url, username, password, timeoutSeconds);
        this.listener = listener;
        this.url = url;
        this.username = username;
        this.password = password;
    }

    static Poller testPoller(URL url, String username, String password, int timeoutInMillis, ACAListener listener, HttpClientFacade client) throws MalformedURLException {
        Poller poller = new Poller(url, username, password, 0, listener);
        poller.timeout = timeoutInMillis;
        poller.client = client;
        return poller;
    }

    @Override
    public void run() {
        while (keepGoing) {
            String newHash = client.get(md5Url, username, password);
//                    String newHash = getHash();
            System.out.println(newHash);
            if (newHash != null && !newHash.equals(hash)) {
                hash = newHash;
                listener.updateConfig();
            }
            try {
                Thread.sleep(timeout);
            } catch (InterruptedException exception) {
                // intentionally empty
            }
        }
    }

    public void stop() {
        keepGoing = false;
    }

    @Deprecated // will be removed shortly
    public String getHash() throws IOException {
        HttpResponse response;
        try {
            response = httpClient.execute(host, hashGet, context);
        } catch (Exception e) {
            return e.getMessage();
        }
        int statusCode = response.getStatusLine().getStatusCode();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(response.getEntity().getContent()))) {
            if (statusCode == 200) {
                return reader.readLine();
            } else {
                logger.log(Level.WARNING, "Could not obtain configuration hash: HTTP Status " + statusCode);
                return null;
            }
        }
    }
}