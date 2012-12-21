package com.github.addisonlee.appconfig;

import javax.sound.midi.Instrument;
import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.MidiUnavailableException;
import javax.sound.midi.ShortMessage;
import javax.sound.midi.Synthesizer;
import java.util.logging.Logger;

import static java.util.logging.Level.INFO;
import static javax.sound.midi.ShortMessage.PROGRAM_CHANGE;

public class MagicPiano {
    private static Logger logger = Logger.getLogger(MagicPiano.class.getName());

    private static Synthesizer synth;

    public static void initSynth() throws MidiUnavailableException {
        logger.log(INFO, "Preparing the synthesizer for funky music.");
        synth = MidiSystem.getSynthesizer();
        synth.open();
    }

    public static void play(ShortMessage midi) throws MidiUnavailableException {
        synth.getReceiver().send(midi, -1);
    }

    public static void changeInstrument(String instrumentName) throws InvalidMidiDataException, MidiUnavailableException {
        ShortMessage message = new ShortMessage();
        message.setMessage(PROGRAM_CHANGE, 0, getInstrumentProgram(instrumentName), 0);
        synth.getReceiver().send(message, 0);
    }

    private static int getInstrumentProgram(final String instrumentName) {
        for (Instrument instrument : synth.getAvailableInstruments()) {
            if (instrumentName != null && instrumentName.equals(instrument.getName())) {
                return instrument.getPatch().getProgram();
            }
        }
        return 0;
    }

    public static void close() {
        synth.close();
    }
}
