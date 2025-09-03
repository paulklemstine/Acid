package com.acid;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.headless.HeadlessApplication;
import com.badlogic.gdx.backends.headless.HeadlessApplicationConfiguration;
import org.junit.BeforeClass;
import org.junit.Test;
import synth.PatternGenerator;

import static org.junit.Assert.assertNotNull;

public class PatternGeneratorTest {

    @BeforeClass
    public static void setUp() {
        HeadlessApplicationConfiguration config = new HeadlessApplicationConfiguration();
        new HeadlessApplication(new Acid(), config);
        Gdx.app.postRunnable(() -> {
            Statics.output = new synth.Output();
        });
    }

//    @Test
//    public void testGenerateBassline() {
//        Gdx.app.postRunnable(() -> {
//            PatternGenerator.generateBassline(0);
//            assertNotNull(Statics.output.getSequencer().basslines[0]);
//        });
//    }
//
//    @Test
//    public void testGenerateMelody() {
//        Gdx.app.postRunnable(() -> {
//            PatternGenerator.generateMelody(0);
//            assertNotNull(Statics.output.getSequencer().basslines[0]);
//        });
//    }
//
//    @Test
//    public void testGenerateHarmony() {
//        Gdx.app.postRunnable(() -> {
//            PatternGenerator.generateHarmony(0);
//            assertNotNull(Statics.output.getSequencer().basslines[0]);
//        });
//    }
//
//    @Test
//    public void testGenerateArpeggio() {
//        Gdx.app.postRunnable(() -> {
//            PatternGenerator.generateArpeggio(0);
//            assertNotNull(Statics.output.getSequencer().basslines[0]);
//        });
//    }
//
//    @Test
//    public void testNewKey() {
//        Gdx.app.postRunnable(() -> {
//            PatternGenerator.newKey();
//        });
//    }
//
//    @Test
//    public void testSetGenre() {
//        Gdx.app.postRunnable(() -> {
//            PatternGenerator.setGenre("dubstep");
//            PatternGenerator.generateBassline(0);
//            assertNotNull(Statics.output.getSequencer().basslines[0]);
//        });
//    }
}
