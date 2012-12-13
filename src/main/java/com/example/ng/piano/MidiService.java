package com.example.ng.piano;

import javax.sound.midi.Instrument;
import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MidiUnavailableException;
import javax.sound.midi.ShortMessage;
import javax.ws.rs.*;
import java.util.ArrayList;
import java.util.List;

@Path("/midi")
public class MidiService {
	@POST
	@Path("/send")
	@Consumes("application/json")
	@Produces("application/json")
	public JsonMidiMessage send(JsonMidiMessage json) throws InvalidMidiDataException, MidiUnavailableException {
		ShortMessage midi = new ShortMessage();
		midi.setMessage(json.command, json.channel, json.note, json.velocity);
        Application.getSynth().getReceiver().send(midi, -1);
        return json;
	}

    @GET
    @Path("/list")
    @Produces("application/json")
    public InstrumentData[] list() throws InvalidMidiDataException, MidiUnavailableException {
        List<InstrumentData> instrumentsList = new ArrayList<>();
        for (Instrument instrument : Application.getSynth().getAvailableInstruments()) {
            InstrumentData instrumentData = new InstrumentData();
            instrumentData.name = instrument.getName();
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
    }
}
