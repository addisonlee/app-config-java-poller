package com.github.addisonlee.appconfig;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MidiUnavailableException;
import javax.sound.midi.ShortMessage;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

@Path("/midi")
public class MagicPianoResource {
    @POST
    @Path("/send")
    @Consumes("application/json")
    @Produces("application/json")
    public JsonMidiMessage send(JsonMidiMessage json) throws InvalidMidiDataException, MidiUnavailableException {
        ShortMessage midi = new ShortMessage();
        midi.setMessage(json.command, json.channel, json.note, json.velocity);
        MagicPiano.play(midi);
        return json;
    }

    public static class JsonMidiMessage {
        public int command;
        public int channel;
        public int note;
        public int velocity;
    }
}
