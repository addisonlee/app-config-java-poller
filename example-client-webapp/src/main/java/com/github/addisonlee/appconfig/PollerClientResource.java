package com.github.addisonlee.appconfig;

import javax.sound.midi.Instrument;
import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MidiUnavailableException;
import javax.sound.midi.ShortMessage;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import java.util.ArrayList;
import java.util.List;

@Path("/midi")
public class PollerClientResource {
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

    @GET
    @Path("/list")
    @Produces("application/json")
    public InstrumentData[] list() throws InvalidMidiDataException, MidiUnavailableException {
        List<InstrumentData> instrumentsList = new ArrayList<>();
        for (Instrument instrument : MagicPiano.getAvailableInstruments()) {
            InstrumentData instrumentData = new InstrumentData(instrument.getName());
            instrumentsList.add(instrumentData);
        }
        return instrumentsList.toArray(new InstrumentData[instrumentsList.size()]);
    }

    public static class JsonMidiMessage {
        public int command;
        public int channel;
        public int note;
        public int velocity;
    }

    public static class InstrumentData {
        public String name;

        public InstrumentData(String name) {
            this.name = name;
        }
    }
}
