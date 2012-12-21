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

    public static Instrument[] getAvailableInstruments() {
        return synth.getAvailableInstruments();
    }

    public static int getInstrument(String name) {
        for (Instrument instrument : getAvailableInstruments()) {
            if (name != null && name.equals(instrument.getName())) {
                return instrument.getPatch().getProgram();
            }
        }
        return 0;
    }

    public static void changeInstrument(String instrument) throws InvalidMidiDataException, MidiUnavailableException {
        ShortMessage message = new ShortMessage();
        message.setMessage(PROGRAM_CHANGE, 0, getInstrument(instrument), 0);
        synth.getReceiver().send(message, 0);
    }

    public static void close() {
        synth.close();
    }
}
