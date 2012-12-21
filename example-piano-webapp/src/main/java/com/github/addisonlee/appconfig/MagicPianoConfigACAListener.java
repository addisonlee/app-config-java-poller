package com.github.addisonlee.appconfig;

import org.codehaus.jackson.map.ObjectMapper;

import java.util.logging.Logger;

import static java.util.logging.Level.INFO;
import static java.util.logging.Level.SEVERE;

public class MagicPianoConfigACAListener implements ACAListener {
    private static Logger logger = Logger.getLogger(MagicPianoConfigACAListener.class.getName());

    public static class Configuration {
        public String instrument;
    }

    @Override
    public void updateConfig(String configurationJson) {
        try {
            Configuration configuration = new ObjectMapper().readValue(configurationJson, Configuration.class);
            MagicPiano.changeInstrument(configuration.instrument);
        } catch (Exception e) {
            logger.log(SEVERE, "Error parsing Configuration from: " + configurationJson);
        }
        logger.log(INFO, "Successfully updated configuration: " + configurationJson);
    }
}
