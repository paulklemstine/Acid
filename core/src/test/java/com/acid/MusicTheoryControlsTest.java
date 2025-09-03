package com.acid;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.headless.HeadlessApplication;
import com.badlogic.gdx.backends.headless.HeadlessApplicationConfiguration;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import synth.MarkovChainGenerator;
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

    @Test
    public void testMarkovChainGenerator() {
        int[] pattern = {12, 14, 12, 15, 12, 14, 12, 15, 12, 14, 12, 15, 12, 14, 12, 15};
        MarkovChainGenerator generator = new MarkovChainGenerator();
        generator.train(pattern);
        int[] newPattern = generator.generate(16, 12);
        assertEquals(12, newPattern[0]);
        for (int i = 1; i < 16; i++) {
            if (newPattern[i-1] == 12) {
                assertTrue(newPattern[i] == 14 || newPattern[i] == 15);
            }
        }
    }
}
