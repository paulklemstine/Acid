package com.acid;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.headless.HeadlessApplication;
import com.badlogic.gdx.backends.headless.HeadlessApplicationConfiguration;
import org.junit.BeforeClass;
import org.junit.Test;
import synth.Harmony;
import synth.MelodyGenerator;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class MelodyGeneratorTest {

    @BeforeClass
    public static void setUp() {
        HeadlessApplicationConfiguration config = new HeadlessApplicationConfiguration();
        new HeadlessApplication(new Acid(), config);
    }

    @Test
    public void testGenerateMelody() {
        Gdx.app.postRunnable(() -> {
            int[] progression = Harmony.Pop;
            int[] scale = Harmony.SCALE_MAJOR;
            int[] melody = MelodyGenerator.generateMelody(progression, scale, 16);

            assertEquals(16, melody.length);
            for (int note : melody) {
                // Assuming -1 is a rest
                if (note != -1) {
                    assertTrue("Note is out of expected range", note >= 0 && note < 128);
                }
            }
        });
    }

    @Test
    public void testGenerateBassline() {
        Gdx.app.postRunnable(() -> {
            int[] progression = Harmony.Pop;
            int[] scale = Harmony.SCALE_MAJOR;
            int[] bassline = MelodyGenerator.generateBassline(progression, scale, 16);

            assertEquals(16, bassline.length);
            for (int note : bassline) {
                if (note != -1) {
                    assertTrue("Note is out of expected range", note >= 0 && note < 128);
                }
            }
        });
    }
}
