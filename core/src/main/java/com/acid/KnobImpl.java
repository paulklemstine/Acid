package com.acid;

import com.badlogic.gdx.Gdx;
import synth.BasslineSynthesizer;
import synth.Output;
import synth.RhythmSynthesizer;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * Created by Paul on 1/8/2017.
 */
public class KnobImpl {
    static double[][][] knobs = new double[4][16][10];

    static {
        for (int j = 0; j < 4; j++) {
            for (int i = 0; i < 16; i++) {
                knobs[j][i] = getControls(j);
            }
        }
    }

    static boolean[] touched = new boolean[12];

    static boolean isTouched() {
        for (int i = 0; i < touched.length; i++) {
            if (touched[i]) return true;
        }
        return false;
    }

    public static float getRotation(int id, double val) {
        float rotation = 0f;
        switch (id) {
            case 0:
                rotation = (int) (((val - .5f) * 400.0) / 1.5f);
                break;
            case 1:
                rotation = (float) ((val - 1200) * 5.0f) / 50f;
                break;

            case 2:
                rotation = (float) (val * 500f) - 100f;
                break;

            case 3:
                rotation = (int) ((val * 500) - 100);
                break;

            case 4:
                rotation = (float) (((20 - val) * 640) / 20.0f) - 100f;
                break;

            case 5:
                rotation = (float) val * 360f;
                break;
            case 6:
                //accent
                rotation = (float) val;
                break;
            case 7:
                rotation = (float) val * 360f;
                break;
            case 8:

                rotation = (float)val/44100f * 360f;

                break;
            case 9:
                rotation = (float) val * 360f;
                break;
        }
        return rotation;
    }


    public static float getRotation(int synthIndex, int id) {
        float rotation = 0f;
        switch (id) {
            case 0:
                rotation = (int) (((((BasslineSynthesizer) Statics.synths[synthIndex])
                        .tune - .5f) * 400.0) / 1.5f);

                break;
            case 1:
                rotation = (float) ((((BasslineSynthesizer) Statics.synths[synthIndex]).cutoff
                        .getValue() - 1200) * 5.0f) / 50f;
                break;

            case 2:
                rotation = (float) (((BasslineSynthesizer) Statics.synths[synthIndex]).resonance
                        .getValue() * 500f) - 100f;
                break;

            case 3:
                rotation = (int) ((((BasslineSynthesizer) Statics.synths[synthIndex])
                        .envMod * 500) - 100);
                break;

            case 4:
                rotation = (float) ((((20 - ((BasslineSynthesizer) Statics.synths[synthIndex])
                        .decay)) * 640) / 20.0f) - 100f;
                break;

            case 5:
                //accent
                rotation = (float) ((BasslineSynthesizer) Statics.synths[synthIndex]).accent * 360f;
                break;
            case 6:

                rotation = (float) Statics.output.getSequencer().bpm;
                break;
            case 7:
                rotation = (float) Statics.output.getVolume() * 360f;
                break;
            case 8:
                rotation = (float) Output.getDelay().getTime()/44100f * 360f;
                break;
            case 9:
                rotation = (float) Output.getDelay().getFeedback() * 360f;
                break;
            case 10:
                rotation = (float) ((BasslineSynthesizer) Statics.synths[synthIndex]).volume * 360f;
                break;
            case 11:
                rotation = (float) ((RhythmSynthesizer) Statics.drums).volume * 360f;
                break;

        }
        return rotation;
    }

    public static float getRotation(int id) {
        return getRotation(Statics.currentSynth, id);
    }

    //    public static int[] knobVals=new int[8];
    public static void touchDragged(int synthIndex, int id, float offset) {
        int cc = (int) (127f / 2f - offset);

        switch (id) {
            case 0:
                // tune
                Statics.synths[synthIndex].controlChange(33, cc);
                break;
            case 1:
                //cutoff
                Statics.synths[synthIndex].controlChange(34, cc);
                break;

            case 2:
                //resonance
                Statics.synths[synthIndex].controlChange(35, cc);
                break;

            case 3:
                //envelope
                Statics.synths[synthIndex].controlChange(36, cc);
                break;

            case 4:
                //decay
                Statics.synths[synthIndex].controlChange(37, cc);
                break;

            case 5:
                //accent
                Statics.synths[synthIndex].controlChange(38, cc);
                break;
            case 6:
                //bpm
                if (cc >= -100 & cc <= 260) {
                    Statics.output.getSequencer().setBpm(cc + 100);
//                    knobVals[id]=cc+100;
                }
                break;
            case 7:
                //volume
                Statics.synths[synthIndex].controlChange(39, cc);
                break;
            case 8:
                //Delay time
                Output.getDelay().controlChange(40, cc);
                break;
            case 9:
                Output.getDelay().controlChange(41, cc);
                break;
            case 10:
                Statics.synths[synthIndex].controlChange(BasslineSynthesizer.MSG_CC_SYNTH_VOLUME, cc);
                break;
            case 11:
                ((RhythmSynthesizer) Statics.drums).controlChange(RhythmSynthesizer.MSG_CC_DRUM_VOLUME, cc);
        }
        KnobData.factory(synthIndex);
    }

    public static void touchDragged(int id, float offset) {
        touchDragged(Statics.currentSynth, id, offset);
    }

    public static void refill(int synthIndex) {
        double[] contrls = KnobImpl.getControls(synthIndex);

        for (int i = 0; i < 16; i++) {
            for (int j = 0; j < 6; j++) {
                KnobImpl.knobs[synthIndex][i][j] = contrls[j];
            }
        }
        KnobData.factory(synthIndex);
    }

    public static void refill() {
        refill(Statics.currentSynth);
    }

    public static void setControl(int synthIndex, int step, int id) {
        if (Statics.free) {
            for (int i = 0; i < 16; i++) {
                knobs[synthIndex][i % 16][id] = getControls(synthIndex)[id];
            }
        } else {
            knobs[synthIndex][step % 16][id] = getControls(synthIndex)[id];
        }
    }

    public static void setControl(int step, int id) {
        setControl(Statics.currentSynth, step, id);
    }

    public static double[] getControl(int synthIndex, int step) {
        return knobs[synthIndex][step % 16];
    }

    public static double[] getControl(int step) {
        return getControl(Statics.currentSynth, step);
    }

    public static double[] getControls(int synthIndex) {
        double[] vals = new double[10];
        vals[0] = Statics.synths[synthIndex].tune;
        vals[1] = Statics.synths[synthIndex].cutoff.getValue();
        vals[2] = Statics.synths[synthIndex].resonance.getValue();
        vals[3] = Statics.synths[synthIndex].envMod;
        vals[4] = Statics.synths[synthIndex].decay;
        vals[5] = Statics.synths[synthIndex].accent;
        vals[6] = Statics.output.getSequencer().bpm;
        vals[7] = Output.volume;
        vals[8] = Output.getDelay().getTime();
        vals[9] = Output.getDelay().getFeedback();
        return vals;
    }

    public static double[] getControls() {
        return getControls(Statics.currentSynth);
    }

    public static void setControls(int synthIndex, double[] vals) {
        Statics.synths[synthIndex].tune = vals[0];
        Statics.synths[synthIndex].cutoff.setValue(vals[1]);
        Statics.synths[synthIndex].resonance.setValue(vals[2]);
        Statics.synths[synthIndex].envMod = vals[3];
        Statics.synths[synthIndex].decay = vals[4];
        Statics.synths[synthIndex].accent = vals[5];
        //Statics.output.getSequencer().setBpm(vals[6]);
        //Statics.output.volume=vals[7];
    }

    public static void setControls(double[] vals) {
        setControls(Statics.currentSynth, vals);
    }

    public static void setControls(int synthIndex, double vals, int id) {
        if (id == 0) Statics.synths[synthIndex].tune = vals;
        if (id == 1) Statics.synths[synthIndex].cutoff.setValue(vals);
        if (id == 2) Statics.synths[synthIndex].resonance.setValue(vals);
        if (id == 3) Statics.synths[synthIndex].envMod = vals;
        if (id == 4) Statics.synths[synthIndex].decay = vals;
        if (id == 5) Statics.synths[synthIndex].accent = vals;
        //Statics.output.getSequencer().setBpm(vals[6]);
        //Statics.output.volume=vals[7];
    }

    public static void setControls(double vals, int id) {
        setControls(Statics.currentSynth, vals, id);
    }


    static int idd = 8;
    static float max = Float.MIN_VALUE;
    static float min = Float.MAX_VALUE;

    public static float percent(int id, float val) {
        if (id == idd) {
            max = Math.max(max, val);
            min = Math.min(min, val);
        }
        float dx = 0;
        float dy = 0;
        switch (id) {
            default:
                return 0;

            case 0:
                dx = 10f;
                dy = 4110f;
                break;
            case 1:
                dx = -118.89f;
                dy = 277.95f;
                break;
            case 2:
                dx = -198.42f;
                dy = 400f;
                break;
            case 3:
                dx = -182f;
                dy = 541f;
                break;
            case 4:
                dx = -94f;
                dy = 525f;
//                dx=-77.46f;
//               dy=2739.46f;
                break;
            case 5:
                dx = 0f;
                dy = 360f;
                break;
            case 6:
                dx = 0f;
                dy = 360f;
                break;
            case 7:
                dx = 0f;
                dy = 720f;
                break;
            case 8:
                dx = 0f;
                dy = 360f;
                break;
            case 9:
                dx = 0f;
                dy = 360f;
                break;
        }

        return (val - dx) / (dy - dx);
    }

    public static void touchReleased(int synthIndex, int id) {
        touched[id] = false;
        KnobData.factory(synthIndex);
    }

    public static void touchReleased(int id) {
        touchReleased(Statics.currentSynth, id);
    }

    public static void touchDown(int synthIndex, int id) {
        touched[id] = true;
        KnobData.factory(synthIndex);
    }

    public static void touchDown(int id) {
        touchDown(Statics.currentSynth, id);
    }
}
