package com.acid;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Stack;

import synth.Output;

public class SaveObject implements Serializable {

    private static final long serialVersionUID = -1216569043511623845L;
    private ArrayList<ArrayList<SequencerData>> sequencerDataArrayList = new ArrayList<ArrayList<SequencerData>>();
    private ArrayList<DrumData> drumDataArrayList = new ArrayList<DrumData>();
    private ArrayList<ArrayList<KnobData>> knobsArrayList = new ArrayList<ArrayList<KnobData>>();
    private int songPosition = 0;
    private int maxSongPosition = 0;
    private int minSongPosition = 0;
    private double bpm = 120;
    private double vol = 1f;
    private double delayTime = 44100 / 10f;
    private double delayFeedback = .1f;
    private ArrayList<SequencerData>[] sequencerStack = new ArrayList[Statics.NUM_SYNTHS];
    private ArrayList<KnobData> knobStack = new ArrayList<>();
    private ArrayList<DrumData> drumStack = new ArrayList<>();

    SaveObject(Acid acid) {
        this.sequencerDataArrayList = acid.sequencerDataArrayList;
        this.drumDataArrayList = acid.drumDataArrayList;
        this.knobsArrayList = acid.knobsArrayList;
        for (int i = 0; i < Statics.NUM_SYNTHS; i++) {
            if (SequencerData.sequences[i] != null) {
                this.sequencerStack[i] = new ArrayList<>(Collections.list(SequencerData.sequences[i].elements()));
            }
        }
        this.drumStack = new ArrayList<>(Collections.list(DrumData.sequences.elements()));
        if (KnobData.sequences != null) {
            this.knobStack = new ArrayList<>(Collections.list(KnobData.sequences.elements()));
        }
        this.songPosition = acid.songPosition;
        this.maxSongPosition = acid.maxSongPosition;
        this.minSongPosition = acid.minSongPosition;
        this.bpm = Statics.output.getSequencer().bpm;
        this.vol = (float) Output.getVolume();
        this.delayTime = Output.getDelay().getTime();
        this.delayFeedback = Output.getDelay().getFeedback();
    }

    public void restore(Acid acid) {
        for (int i = 0; i < Statics.NUM_SYNTHS; i++) {
            SequencerData.sequences[i] = new Stack<>();
            if (sequencerStack[i] != null) {
                for (InstrumentData data : sequencerStack[i]) {
                    data.refresh();
                    SequencerData.sequences[i].add(new SequencerData(i));
                }
            }
        }
        DrumData.sequences = new Stack<>();
        if (drumStack != null) {
            for (InstrumentData data : drumStack) {
                data.refresh();
                DrumData.sequences.add(new DrumData());
            }
        }
        KnobData.sequences = new Stack<>();
        if (knobStack != null) {
            for (InstrumentData data : knobStack) {
                data.refresh();
                KnobData.sequences.add(new KnobData(((KnobData)data).synthIndex));
            }
        }
        acid.sequencerDataArrayList = sequencerDataArrayList;
        acid.drumDataArrayList = drumDataArrayList;
        acid.knobsArrayList = knobsArrayList;
        acid.songPosition = songPosition;
        acid.maxSongPosition = maxSongPosition;
        acid.minSongPosition = minSongPosition;
        acid.swapPattern(songPosition, songPosition);
        Statics.output.getSequencer().setBpm(bpm);
        Output.volume = vol;
        Output.getDelay().setTime(delayTime);
        Output.getDelay().setFeedback(delayFeedback);
        for (int i = 0; i < Statics.NUM_SYNTHS; i++) {
            if (acid.knobsArrayList.get(i).size() > 0) {
                acid.knobsArrayList.get(i).get(0).refresh();
            }
            KnobData.currentSequences[i] = new KnobData(i);
        }
    }
}
