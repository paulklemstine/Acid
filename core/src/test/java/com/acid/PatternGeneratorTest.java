package com.acid;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.headless.HeadlessApplication;
import com.badlogic.gdx.backends.headless.HeadlessApplicationConfiguration;
import org.junit.BeforeClass;
import org.junit.Test;
import synth.PatternGenerator;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotEquals;

public class PatternGeneratorTest {

    @BeforeClass
    public static void setUp() {
        HeadlessApplicationConfiguration config = new HeadlessApplicationConfiguration();
        new HeadlessApplication(new Acid(), config);
        Gdx.app.postRunnable(() -> {
            Statics.output = new synth.Output();
        });
    }

    @Test
    public void testGetPattern() {
        Gdx.app.postRunnable(() -> {
            int[] pattern = PatternGenerator.getPattern("House", "A", "Bassline 1");
            assertNotNull(pattern);
            assertEquals(16, pattern.length);
        });
    }

    @Test
    public void testGetDrumPattern() {
        Gdx.app.postRunnable(() -> {
            int[][] pattern = PatternGenerator.getDrumPattern("House", "A");
            assertNotNull(pattern);
            assertEquals(4, pattern.length);
        });
    }

    @Test
    public void testMutatePattern() {
        Gdx.app.postRunnable(() -> {
            int[] pattern = new int[]{12, 14, 16, 17, 19, 21, 23, 24, 12, 14, 16, 17, 19, 21, 23, 24};
            int[] scale = new int[]{0, 2, 4, 5, 7, 9, 11};
            int[] mutatedPattern = PatternGenerator.mutatePattern(pattern, scale, 1.0f);
            assertNotEquals(pattern, mutatedPattern);
        });
    }
}
