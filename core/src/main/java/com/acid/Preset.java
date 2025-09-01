package com.acid;

import java.io.Serializable;

public class Preset implements Serializable {
    public KnobData knobData;
    public SequencerData sequencerData;

    public Preset(KnobData knobData, SequencerData sequencerData) {
        this.knobData = knobData;
        this.sequencerData = sequencerData;
    }
}
