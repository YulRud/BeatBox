package com.company;

import javax.sound.midi.*;
import java.util.ArrayList;
import java.util.Map;

public class MusicPlayer {
    private static final int NOTE_START = 144;
    private static final int NOTE_FINISH = 128;
    private static final int DEFAULT_CHANNEL = 9;
    private static final int NOTE_LENGTH = 2;
    private Track track = null;
    private Sequencer sequencer = null;
    private Sequence seq = null;
    private int noteSpeed;

    public MusicPlayer() {
        this.noteSpeed = 100;
    }

    public void setUpPlayer() {
        try {
            sequencer = MidiSystem.getSequencer();
            sequencer.open();
            seq = new Sequence(Sequence.PPQ, 4);
            track = seq.createTrack();
            sequencer.setTempoInBPM(120);
        } catch (MidiUnavailableException e) {
            e.printStackTrace();
        } catch (InvalidMidiDataException e) {
            e.printStackTrace();
        }
    }

    public void startPlayer() {
        try {
            sequencer.setSequence(seq);
            sequencer.setLoopCount(sequencer.LOOP_CONTINUOUSLY);
            sequencer.start();
            sequencer.setTempoInBPM(120);
        } catch (InvalidMidiDataException e) {
            e.printStackTrace();
        }
    }

    public void stopPlayer() {
        sequencer.stop();
    }

    public void createTrack(Map<Integer, ArrayList<Integer>> trackSelected) {
        for (Integer key : trackSelected.keySet()) {
            for (Integer i : trackSelected.get(key)) {
                addNoteToTrack(key, i + 1);
            }
        }
        track.add(makeEvent(192, 9, 1, 0, 15));
    }

    public void tempUp() {
        setTemp(1.03);
    }

    public void tempDown() {
        setTemp(0.97);
    }

    //Work with Midi events and track creation
    private MidiEvent makeEvent(int msgType, int channel, int note, int noteSpeed, int tick) {
        MidiEvent event = null;
        try {
            ShortMessage a = new ShortMessage();
            a.setMessage(msgType, channel, note, noteSpeed);
            event = new MidiEvent(a, tick);
        } catch (InvalidMidiDataException e) {
            e.printStackTrace();
        }
        return event;
    }

    private void addNoteToTrack(int instrumentNote, int tick) {
        track.add(makeEvent(NOTE_START, DEFAULT_CHANNEL, instrumentNote, noteSpeed, tick));
        track.add(makeEvent(NOTE_FINISH, DEFAULT_CHANNEL, instrumentNote, noteSpeed, tick + NOTE_LENGTH));
    }

    private void setTemp(double tempFactor) {
        sequencer.setTempoFactor((float) (sequencer.getTempoFactor() * tempFactor));
    }
}
