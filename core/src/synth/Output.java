package synth;

import com.acid.Statics;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.AudioDevice;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class Output implements Runnable {
    private static final int BUFFER_SIZE = 2050;
    public static double volume = 1D;
    public static double SAMPLE_RATE = 44100;
    public static boolean running = false;
    private static Thread thread = null;
    private static Synthesizer[] tracks;
    public static boolean[] muteState = new boolean[5];
    private static Reverb reverb;
    private static Delay delay;
    private static boolean paused = false;
    private static boolean newAD;
    private AcidSequencer sequencer;
    private float[] buffer = new float[BUFFER_SIZE];
    private AudioDevice ad;

    public Output() {
        ad = Gdx.audio.newAudioDevice((int) SAMPLE_RATE, false);
        tracks = new Synthesizer[5];
        BasslineSynthesizer[] tbs = new BasslineSynthesizer[Statics.NUM_SYNTHS_TOTAL];
        for (int i = 0; i < Statics.NUM_SYNTHS_TOTAL; i++) {
            tbs[i] = new BasslineSynthesizer();
            tracks[i] = tbs[i];
        }
        RhythmSynthesizer tr = new RhythmSynthesizer();
        tracks[4] = tr;


        delay = new Delay();
        reverb = new Reverb();

        this.sequencer = new AcidSequencer(tbs, tr, this);

        thread = new Thread(this);
        thread.setPriority(10);

    }

    public static double getVolume() {
        return volume;
    }

    static void setVolume(double value) {
        volume = value;
    }

    public static Delay getDelay() {
        return delay;
    }

    public static Reverb getReverb() {
        return reverb;
    }

    public static boolean isPaused() {
        return paused;
    }

    public static void pause() {
        paused = true;
    }

    public static void resume() {
        paused = false;
        newAD = true;
    }

    public static byte[] FloatArray2ByteArray(float[] values) {
        ByteBuffer buffer = ByteBuffer.allocate(2 * values.length);
        buffer.order(ByteOrder.LITTLE_ENDIAN);
        float mult = 32767.0f;
        for (float value : values) {
//            buffer.putFloat(value);
            buffer.putShort((short) (value * mult));
        }

        return buffer.array();
    }

    public void start() {
        running = true;
        thread.start();
    }

    public void stop() {
        running = false;
    }

    public boolean isRunning() {
        return running;
    }

    public void run() {
        while (running) {
            if (newAD) {
                newAD = false;
                try {
                    ad = Gdx.audio.newAudioDevice((int) SAMPLE_RATE, false);
                } catch (Exception e) {
                }
            }
            if (paused) {
                try {
                    Thread.sleep(25L);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                continue;
            }
            for (int i = 0; i < buffer.length; i += 2) {
                double left = 0.0D;
                double right = 0.0D;

                this.sequencer.tick();

                for (int j = 0; j < tracks.length; j++) {
                    if (!muteState[j]) {
                        double[] tmp = tracks[j].stereoOutput();
                        delay.input(tmp[2]);
                        reverb.input(tmp[3]);
                        left += tmp[0];
                        right += tmp[1];
                    }
                }


                double[] del = delay.output();
                left += del[0];
                right += del[1];

                double[] rev = reverb.process();
                left += rev[0];
                right += rev[1];

                if (left > 1.0D)
                    left = 1.0D;
                else if (left < -1.0D)
                    left = -1.0D;
                if (right > 1.0D)
                    right = 1.0D;
                else if (right < -1.0D) {
                    right = -1.0D;
                }
                buffer[i] = (float) (left * volume);
                buffer[i + 1] = (float) (right * volume);
            }
            if (Statics.export) {
                if (Statics.exportFile != null)
                    Statics.exportFile.writeBytes(FloatArray2ByteArray(buffer), true);
                try {
                    Thread.sleep(1);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            } else {
                if (ad == null) {
                    ad = Gdx.audio.newAudioDevice((int) SAMPLE_RATE, false);
                }
                try {
                    ad.writeSamples(buffer, 0, BUFFER_SIZE);
                } catch (Exception e) {
                }
            }
        }
        dispose();
    }

    public void dispose() {
        running = false;
        try {
            ad.dispose();
        } catch (Exception e) {
        }
    }

    public Synthesizer getTrack(int i) {
        return tracks[i];
    }

    public Synthesizer[] getTracks() {
        return tracks;
    }

    public AcidSequencer getSequencer() {
        return this.sequencer;
    }

}
