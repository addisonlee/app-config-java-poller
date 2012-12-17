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
import java.net.URL;
import java.util.logging.Logger;

import static java.util.logging.Level.WARNING;

public class HttpClientFacade {
    private static Logger logger = Logger.getLogger(HttpClientFacade.class.getName());

    public String getFirstLine(URL url, String username, String password) throws IOException {
        HttpHost host = new HttpHost(url.getHost(), url.getPort(), url.getProtocol());
        DefaultHttpClient httpClient = new DefaultHttpClient();
        httpClient.getCredentialsProvider().setCredentials(new AuthScope(host.getHostName(), host.getPort()),
                new UsernamePasswordCredentials(username, password));
        AuthCache cache = new BasicAuthCache();
        cache.put(host, new BasicScheme());
        BasicHttpContext context = new BasicHttpContext();
        context.setAttribute(ClientContext.AUTH_CACHE, cache);

        HttpResponse response;
        try {
            response = httpClient.execute(host, new HttpGet(url.toString()), context);
        } catch (Exception e) {
            return e.getMessage();
        }
        int statusCode = response.getStatusLine().getStatusCode();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(response.getEntity().getContent()))) {
            if (statusCode == 200) {
                return reader.readLine();
            } else {
                logger.log(WARNING, "Could not obtain configuration hash: HTTP Status " + statusCode);
                return null;
            }
        }
    }
}