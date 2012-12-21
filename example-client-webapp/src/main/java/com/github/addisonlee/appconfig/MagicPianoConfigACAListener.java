package com.github.addisonlee.appconfig;

import org.codehaus.jackson.map.ObjectMapper;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MidiUnavailableException;
import java.io.IOException;
import java.util.logging.Logger;

import static java.util.logging.Level.INFO;
import static java.util.logging.Level.SEVERE;

public class MagicPianoConfigACAListener implements ACAListener {
    private static Logger logger = Logger.getLogger(MagicPianoConfigACAListener.class.getName());

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

    @Override
    public void updateConfig(String configurationJson) {
        Configuration configuration = null;
        try {
            configuration = new ObjectMapper().readValue(configurationJson, Configuration.class);
        } catch (IOException e) {
            logger.log(INFO, "Error parsing Configuration from: " + configurationJson);
        }
        try {
            MagicPiano.changeInstrument(configuration.getInstrument());
        } catch (InvalidMidiDataException | MidiUnavailableException e) {
            logger.log(SEVERE, null, e);
        }
        logger.log(INFO, "Updated configuration: " + configurationJson);
    }
}
