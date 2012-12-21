package com.github.addisonlee.appconfig;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.logging.Logger;

import static java.util.logging.Level.INFO;
import static java.util.logging.Level.SEVERE;

@WebListener
public class PollerServletLifeCycleListener implements ServletContextListener {
    private static Logger logger = Logger.getLogger(PollerServletLifeCycleListener.class.getName());

    private static Poller poller;

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
        poller.stop();
        logger.log(INFO, "Elvis has left the building.");
    }

    private void initConfigurator(ServletContextEvent event) throws MalformedURLException {
        ServletContext context = event.getServletContext();
        poller = new Poller(
                context.getInitParameter("CONFIG_URL"),
                context.getInitParameter("CONFIG_USERNAME"),
                context.getInitParameter("CONFIG_PASSWORD"),
                Integer.parseInt(context.getInitParameter("CONFIG_TIMEOUT")) * 1000,
                new ACAListener() {
                    @Override
                    public void updateConfig(String config) {
                        throw new UnsupportedOperationException("not implemented yet");
                    }
                });
        Thread daemon = new Thread(poller);
        daemon.setDaemon(true);
        daemon.start();
    }

    public static String getHash() {
        try {
            return poller.getMd5();
        } catch (IOException e) {
            return e.getMessage();
        }
    }
}
