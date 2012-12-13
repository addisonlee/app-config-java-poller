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
import org.codehaus.jackson.map.ObjectMapper;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;
import javax.sound.midi.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.logging.Logger;

import static java.lang.Thread.sleep;
import static java.util.logging.Level.*;
import static javax.sound.midi.ShortMessage.PROGRAM_CHANGE;

@WebListener
public class Application implements ServletContextListener {
    private static Logger logger = Logger.getLogger(Application.class.getName());

	private static Synthesizer synth;

	public static Synthesizer getSynth() {
		return synth;
	}

    private Configurator configurator;

	@Override
	public void contextInitialized(ServletContextEvent event) {
		try {
            initSynth();
            initConfigurator(event);
		} catch (MidiUnavailableException | MalformedURLException exception) {
            logger.log(SEVERE, null, exception);
		}
	}

    @Override
	public void contextDestroyed(ServletContextEvent event) {
        configurator.stop();
		synth.close();
        logger.log(INFO, "Elvis has left the building.");
	}

    private int getInstrument(String name) {
        for (Instrument instrument : synth.getAvailableInstruments()) {
            if (name != null && name.equals(instrument.getName())) {
                return instrument.getPatch().getProgram();
            }
        }
        return 0;
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

    private void initSynth() throws MidiUnavailableException {
        logger.log(INFO, "Preparing the synthesizer for funky music.");
        synth = MidiSystem.getSynthesizer();
        synth.open();
    }

    public static class Configuration {
        private String instrument;

        public String getInstrument() {
            return instrument;
        }

        @Override
        public String toString() {
            return getClass().getSimpleName() + "{instrument='" + instrument + "'}";
        }
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
                try {
                    String newHash = getHash();
                    if (newHash != null && !newHash.equals(hash)) {
                        hash = newHash;
                        updateConfiguration();
                    }
                } catch (IOException | MidiUnavailableException | InvalidMidiDataException exception) {
                    logger.log(SEVERE, null, exception);
                }
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
            HttpResponse response = httpClient.execute(host, hashGet, context);
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

        private void updateConfiguration() throws IOException, MidiUnavailableException, InvalidMidiDataException {
            HttpResponse response = httpClient.execute(host, configGet, context);
            int statusCode = response.getStatusLine().getStatusCode();
            try (Reader reader = new InputStreamReader(response.getEntity().getContent())) {
                if (statusCode == 200) {
                    Configuration config = new ObjectMapper().readValue(reader, Configuration.class);
                    ShortMessage message = new ShortMessage();
                    message.setMessage(PROGRAM_CHANGE, 0, getInstrument(config.getInstrument()), 0);
                    synth.getReceiver().send(message, 0);
                    logger.log(INFO, "Updated configuration: " + config);
                } else {
                    logger.log(WARNING, "Could not obtain configuration: HTTP Status " + statusCode);
                }
            }
        }
    }
}
