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

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Date;
import java.util.logging.Logger;

import static java.lang.Thread.sleep;
import static java.util.logging.Level.*;

@WebListener
public class PollerService implements ServletContextListener {
    private static Logger logger = Logger.getLogger(PollerService.class.getName());

    private static Configurator configurator;

	@Override
	public void contextInitialized(ServletContextEvent event) {
		try {
            initConfigurator(event);
		} catch (MalformedURLException exception) {
            logger.log(SEVERE, null, exception);
		}
	}

    @Override
	public void contextDestroyed(ServletContextEvent event) {
        configurator.stop();
        logger.log(INFO, "Elvis has left the building.");
	}

    private void initConfigurator(ServletContextEvent event) throws MalformedURLException {
        ServletContext context = event.getServletContext();
        configurator = new Configurator(
            new URL(context.getInitParameter("CONFIG_URL")),
            context.getInitParameter("CONFIG_USERNAME"),
            context.getInitParameter("CONFIG_PASSWORD"),
            Integer.parseInt(context.getInitParameter("CONFIG_TIMEOUT")));
        Thread daemon = new Thread(configurator);
        daemon.setDaemon(true);
        daemon.start();
    }

    public static String getHash() throws IOException {
        return configurator.getHash();
    }

    public static String getStubHash() throws IOException {
        return "hash stub " + new Date();
    }

    private class Configurator implements Runnable {
        private String hash;
        private boolean keepGoing;
        private final DefaultHttpClient httpClient = new DefaultHttpClient();
        private final HttpHost host;
        private final BasicHttpContext context = new BasicHttpContext();
        private HttpGet hashGet;
        private HttpGet configGet;
        private long timeout;

        public Configurator(URL url, String username, String password, int timeoutSeconds) throws MalformedURLException {
            host = new HttpHost(url.getHost(), url.getPort(), url.getProtocol());
            httpClient.getCredentialsProvider().setCredentials(new AuthScope(host.getHostName(), host.getPort()),
                    new UsernamePasswordCredentials(username, password));
            AuthCache cache = new BasicAuthCache();
            cache.put(host, new BasicScheme());
            context.setAttribute(ClientContext.AUTH_CACHE, cache);
            hashGet = new HttpGet(url.toString().replace(".json", ".md5"));
            configGet = new HttpGet(url.toString());
            keepGoing = true;
            timeout = timeoutSeconds * 1000;
        }

        @Override
        public void run() {
            while (keepGoing) {
//                try {
//                    String newHash = getHash();
//                    if (newHash != null && !newHash.equals(hash)) {
//                        hash = newHash;
//                        updateConfiguration();
//                    }
//                } catch (IOException | MidiUnavailableException | InvalidMidiDataException exception) {
//                    logger.log(SEVERE, null, exception);
//                }
                try {
                    sleep(timeout);
                } catch (InterruptedException exception) {
                    // intentionally empty
                }
            }
        }

        public void stop() {
            keepGoing = false;
        }

        private String getHash() throws IOException {
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
                    logger.log(WARNING, "Could not obtain configuration hash: HTTP Status " + statusCode);
                    return null;
                }
            }
        }

    }
}
