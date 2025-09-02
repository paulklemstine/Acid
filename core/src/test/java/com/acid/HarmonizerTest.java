package com.acid;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.headless.HeadlessApplication;
import com.badlogic.gdx.backends.headless.HeadlessApplicationConfiguration;
import org.junit.BeforeClass;
import org.junit.Test;
import synth.Harmony;
import synth.Harmonizer;
import synth.MelodyGenerator;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class HarmonizerTest {

    @BeforeClass
    public static void setUp() {
        HeadlessApplicationConfiguration config = new HeadlessApplicationConfiguration();
        new HeadlessApplication(new Acid(), config);
    }

    @Test
    public void testHarmonize() {
        Gdx.app.postRunnable(() -> {
            int[] progression = Harmony.Pop;
            int[] scale = Harmony.SCALE_MAJOR;
            int[] melody = MelodyGenerator.generateMelody(progression, scale, 16);
            int[] harmony = Harmonizer.harmonize(melody, progression, scale, 16);

            assertEquals(16, harmony.length);
            for (int note : harmony) {
                if (note != -1) {
                    assertTrue("Note is out of expected range", note >= 0 && note < 128);
                }
            }
        });
    }

    @Test
    public void testCreateCounterMelody() {
        Gdx.app.postRunnable(() -> {
            int[] progression = Harmony.Pop;
            int[] scale = Harmony.SCALE_MAJOR;
            int[] melody = MelodyGenerator.generateMelody(progression, scale, 16);
            int[] counterMelody = Harmonizer.createCounterMelody(melody, progression, scale, 16);

            assertEquals(16, counterMelody.length);
            for (int note : counterMelody) {
                if (note != -1) {
                    assertTrue("Note is out of expected range", note >= 0 && note < 128);
                }
            }
        });
    }
}
