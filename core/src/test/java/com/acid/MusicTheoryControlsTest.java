package com.acid;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.headless.HeadlessApplication;
import com.badlogic.gdx.backends.headless.HeadlessApplicationConfiguration;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import synth.MelodyGenerator;
import synth.PatternGenerator;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class MusicTheoryControlsTest {

    private static Acid acid;

    @BeforeClass
    public static void setUp() {
        HeadlessApplicationConfiguration config = new HeadlessApplicationConfiguration();
        acid = new Acid();
        new HeadlessApplication(acid, config);
        Gdx.app.postRunnable(() -> {
            Statics.output = new synth.Output();
        });
    }

    @Before
    public void resetSequencer() {
        Gdx.app.postRunnable(() -> {
            for (int j = 0; j < Statics.NUM_SYNTHS; j++) {
                for (int i = 0; i < 16; i++) {
                    Statics.output.getSequencer().basslines[j].note[i] = 0;
                    Statics.output.getSequencer().basslines[j].pause[i] = true;
                    Statics.output.getSequencer().basslines[j].accent[i] = false;
                    Statics.output.getSequencer().basslines[j].slide[i] = false;
                }
            }
        });
    }

    @Test
    public void testShiftPattern() {
        Gdx.app.postRunnable(() -> {
            Statics.output.getSequencer().basslines[0].note[0] = 12;
            Statics.output.getSequencer().basslines[0].pause[0] = false;
            acid.shiftPattern(1);
            assertEquals(13, Statics.output.getSequencer().basslines[0].note[0]);
        });
    }

    @Test
    public void testTranspose() {
        Gdx.app.postRunnable(() -> {
            Statics.output.getSequencer().basslines[0].note[0] = 12; // C1
            Statics.output.getSequencer().basslines[0].note[1] = 15; // D#1
            Statics.output.getSequencer().basslines[0].pause[0] = false;
            Statics.output.getSequencer().basslines[0].pause[1] = false;

            // Transpose to D (2)
            acid.shiftPattern(2 - (12 % 12));

            assertEquals(14, Statics.output.getSequencer().basslines[0].note[0]);
            assertEquals(17, Statics.output.getSequencer().basslines[0].note[1]);
        });
    }

    @Test
    public void testMutateRhythm() {
        Gdx.app.postRunnable(() -> {
            boolean[] pauses = {true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true};
            PatternGenerator.mutateRhythm(pauses, 1.0f);
            for (boolean pause : pauses) {
                assertEquals(false, pause);
            }
        });
    }

    @Test
    public void testMutateAccents() {
        Gdx.app.postRunnable(() -> {
            boolean[] accents = {false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false};
            PatternGenerator.mutateAccents(accents, 1.0f);
            for (boolean accent : accents) {
                assertEquals(true, accent);
            }
        });
    }

    @Test
    public void testMutateSlides() {
        Gdx.app.postRunnable(() -> {
            boolean[] slides = {false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false};
            PatternGenerator.mutateSlides(slides, 1.0f);
            for (boolean slide : slides) {
                assertEquals(true, slide);
            }
        });
    }

    @Test
    public void testArpeggiate() {
        Gdx.app.postRunnable(() -> {
            int[] pattern = {12, 0, 16, 0, 19, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
            int[] arpeggiated = PatternGenerator.arpeggiate(pattern, 1, "up");
            assertEquals(12, arpeggiated[0]);
            assertEquals(0, arpeggiated[1]);
            assertEquals(16, arpeggiated[2]);
            assertEquals(0, arpeggiated[3]);
            assertEquals(19, arpeggiated[4]);
            assertEquals(0, arpeggiated[5]);
        });
    }

    @Test
    public void testShiftPatternWithSlide() {
        Gdx.app.postRunnable(() -> {
            Statics.output.getSequencer().basslines[0].note[0] = 12;
            Statics.output.getSequencer().basslines[0].pause[0] = false;
            Statics.output.getSequencer().basslines[0].slide[0] = true;
            Statics.output.getSequencer().basslines[0].note[1] = 13;
            Statics.output.getSequencer().basslines[0].pause[1] = false;
            acid.shiftPattern(1);
            assertEquals(13, Statics.output.getSequencer().basslines[0].note[0]);
            assertEquals(14, Statics.output.getSequencer().basslines[0].note[1]);
        });
    }

    @Test
    public void testEuclideanPatternGenerator() {
        Gdx.app.postRunnable(() -> {
            int[] pattern = PatternGenerator.generateEuclideanPattern(5, 16);
            int[] expected = {36, 0, 0, 36, 0, 0, 36, 0, 0, 36, 0, 0, 36, 0, 0, 0};
            for (int i = 0; i < 16; i++) {
                assertEquals(expected[i], pattern[i]);
            }
        });
    }
}
