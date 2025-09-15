package com.acid;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.headless.HeadlessApplication;
import com.badlogic.gdx.backends.headless.HeadlessApplicationConfiguration;
import org.junit.BeforeClass;
import org.junit.Test;
import synth.BasslineSynthesizer;

import static org.junit.Assert.assertEquals;

public class KnobImplTest {

    @BeforeClass
    public static void setUp() {
        HeadlessApplicationConfiguration config = new HeadlessApplicationConfiguration();
        new HeadlessApplication(new com.badlogic.gdx.ApplicationAdapter() {
        }, config);
        Gdx.app.postRunnable(() -> {
            Statics.output = new synth.Output();
            Statics.synths = new BasslineSynthesizer[Statics.NUM_SYNTHS];
            for (int i = 0; i < Statics.NUM_SYNTHS; i++) {
                Statics.synths[i] = new BasslineSynthesizer();
                Statics.output.getTracks()[i] = Statics.synths[i];
            }
        });
    }

    @Test
    public void testTuneKnob() {
        Gdx.app.postRunnable(() -> {
            BasslineSynthesizer synth = (BasslineSynthesizer) Statics.synths[0];
            // Test at 25%
            KnobImpl.touchDragged(0, 0, 90);
            assertEquals(1.22, synth.tune, 0.01);
            // Test at 75%
            KnobImpl.touchDragged(0, 0, 270);
            assertEquals(1.74, synth.tune, 0.01);
        });
    }

    @Test
    public void testCutoffKnob() {
        Gdx.app.postRunnable(() -> {
            BasslineSynthesizer synth = (BasslineSynthesizer) Statics.synths[0];
            // Test at 25%
            KnobImpl.touchDragged(0, 1, 90);
            assertEquals(1400, synth.cutoff.getValue(), 100);
            // Test at 75%
            KnobImpl.touchDragged(0, 1, 270);
            assertEquals(3800, synth.cutoff.getValue(), 100);
        });
    }

    @Test
    public void testResonanceKnob() {
        Gdx.app.postRunnable(() -> {
            BasslineSynthesizer synth = (BasslineSynthesizer) Statics.synths[0];
            // Test at 25%
            KnobImpl.touchDragged(0, 2, 90);
            assertEquals(0.25, synth.resonance.getValue(), 0.1);
            // Test at 75%
            KnobImpl.touchDragged(0, 2, 270);
            assertEquals(0.75, synth.resonance.getValue(), 0.1);
        });
    }

    @Test
    public void testEnvModKnob() {
        Gdx.app.postRunnable(() -> {
            BasslineSynthesizer synth = (BasslineSynthesizer) Statics.synths[0];
            // Test at 25%
            KnobImpl.touchDragged(0, 3, 90);
            assertEquals(0.25, synth.envMod, 0.1);
            // Test at 75%
            KnobImpl.touchDragged(0, 3, 270);
            assertEquals(0.75, synth.envMod, 0.1);
        });
    }

    @Test
    public void testDecayKnob() {
        Gdx.app.postRunnable(() -> {
            BasslineSynthesizer synth = (BasslineSynthesizer) Statics.synths[0];
            // Test at 25%
            KnobImpl.touchDragged(0, 4, 90);
            assertEquals(15, synth.decay, 1);
            // Test at 75%
            KnobImpl.touchDragged(0, 4, 270);
            assertEquals(5, synth.decay, 1);
        });
    }

    @Test
    public void testAccentKnob() {
        Gdx.app.postRunnable(() -> {
            BasslineSynthesizer synth = (BasslineSynthesizer) Statics.synths[0];
            // Test at 25%
            KnobImpl.touchDragged(0, 5, 90);
            assertEquals(0.25, synth.accent, 0.1);
            // Test at 75%
            KnobImpl.touchDragged(0, 5, 270);
            assertEquals(0.75, synth.accent, 0.1);
        });
    }

    @Test
    public void testDistortionKnob() {
        Gdx.app.postRunnable(() -> {
            BasslineSynthesizer synth = (BasslineSynthesizer) Statics.synths[0];
            // Test at 25%
            KnobImpl.touchDragged(0, 12, 90);
            assertEquals(3.5, synth.distortion.getGain(), 0.5);
            // Test at 75%
            KnobImpl.touchDragged(0, 12, 270);
            assertEquals(8.5, synth.distortion.getGain(), 0.5);
        });
    }

    @Test
    public void testAux1Knob() {
        Gdx.app.postRunnable(() -> {
            BasslineSynthesizer synth = (BasslineSynthesizer) Statics.synths[0];
            // Test at 25%
            KnobImpl.touchDragged(0, 13, 90);
            assertEquals(0.25, synth.aux1Amt, 0.1);
            // Test at 75%
            KnobImpl.touchDragged(0, 13, 270);
            assertEquals(0.75, synth.aux1Amt, 0.1);
        });
    }

    @Test
    public void testAux2Knob() {
        Gdx.app.postRunnable(() -> {
            BasslineSynthesizer synth = (BasslineSynthesizer) Statics.synths[0];
            // Test at 25%
            KnobImpl.touchDragged(0, 14, 90);
            assertEquals(0.25, synth.aux2Amt, 0.1);
            // Test at 75%
            KnobImpl.touchDragged(0, 14, 270);
            assertEquals(0.75, synth.aux2Amt, 0.1);
        });
    }
}
