package com.acid;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.utils.ScreenUtils;

import java.util.Stack;

class DrumPatterns {
    public int[][] tr808 = new int[9][16];
    public int[][] tr909 = new int[9][16];
}

/**
 * Created by Paul on 1/10/2017.
 */
public class DrumData extends InstrumentData  {
    public static final String[] TR808_DRUM_NAMES = new String[]{"BD", "SD", "CH", "OH", "CP", "CR", "HC", "MC", "CB"};
    public static final String[] TR909_DRUM_NAMES = new String[]{"BD", "SD", "CH", "OH", "CL", "CR", "HT", "MT"};

    private final DrumPatterns patterns = new DrumPatterns();

    public DrumData() {
        for (int y1 = 0; y1 < 9; y1++) {
            for (int x1 = 0; x1 < 16; x1++) {
                patterns.tr909[y1][x1] = 0;
                patterns.tr808[y1][x1] = 0;
            }
        }
        pixmap = drawPixmap(300, 300);
        region = new TextureRegion(new Texture(pixmap));
        region.flip(false, true);
    }

    public void refresh() {
        int[][] rhythmToLoad;
        String[] drumNames;
        if (Statics.currentDrumMachine == DrumMachine.TR808) {
            rhythmToLoad = patterns.tr808;
            drumNames = TR808_DRUM_NAMES;
        } else {
            rhythmToLoad = patterns.tr909;
            drumNames = TR909_DRUM_NAMES;
        }

        for (int y1 = 0; y1 < drumNames.length; y1++) {
            for (int x1 = 0; x1 < 16; x1++) {
                Statics.output.getSequencer().rhythm[y1][x1] = rhythmToLoad[y1][x1];
            }
        }
    }

    public void saveCurrentPattern() {
        int[][] rhythmToSave;
        String[] drumNames;
        if (Statics.currentDrumMachine == DrumMachine.TR808) {
            rhythmToSave = patterns.tr808;
            drumNames = TR808_DRUM_NAMES;
        } else {
            rhythmToSave = patterns.tr909;
            drumNames = TR909_DRUM_NAMES;
        }

        for (int y1 = 0; y1 < drumNames.length; y1++) {
            for (int x1 = 0; x1 < 16; x1++) {
                rhythmToSave[y1][x1] = Statics.output.getSequencer().rhythm[y1][x1];
            }
        }
    }

    public void setRhythms(DrumPatterns patterns) {
        this.patterns.tr808 = patterns.tr808;
        this.patterns.tr909 = patterns.tr909;
    }

    public DrumPatterns getPatterns() {
        return patterns;
    }

    @Override
    public String toString() {
        String s = "";
        s += "808:\n";
        for (int i = 0; i < 16; i++) {
            for (int j = 0; j < TR808_DRUM_NAMES.length; j++) {
                if (patterns.tr808[j][i] != 0) {
                    s += j + "";
                }
            }
            s += " ";
        }
        s += "\n909:\n";
        for (int i = 0; i < 16; i++) {
            for (int j = 0; j < TR909_DRUM_NAMES.length; j++) {
                if (patterns.tr909[j][i] != 0) {
                    s += j + "";
                }
            }
            s += " ";
        }
        return s;
    }

    public Pixmap drawPixmap(int w, int h) {
        FrameBuffer drawBuffer = new FrameBuffer(Pixmap.Format.RGBA8888, w, h, false);
        drawBuffer.begin();
        Color c = ColorHelper.rainbowDark();
        Gdx.gl.glClearColor(c.r, c.g, c.b, c.a);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_STENCIL_BUFFER_BIT);
        ShapeRenderer renderer = new ShapeRenderer();


        renderer.getProjectionMatrix().setToOrtho2D(0, 0, w, h);


        float skipx = ((float) w / 16f);
        float skipy = (float) h / 7f;
        render(renderer, skipx, skipy);
        renderer.begin(ShapeRenderer.ShapeType.Line);
        renderer.setColor(ColorHelper.rainbowInverse());
        for (int i=0;i<5;i++) {
            renderer.rect(i, i, w-i*2, h-i*2);
        }
        renderer.end();

        Pixmap pixmap1 = ScreenUtils.getFrameBufferPixmap(0, 0, w, h);
        Pixmap pixmap = new Pixmap((int) w, (int) h, Pixmap.Format.RGBA8888);
        pixmap.setColor(Color.RED);
        pixmap.fill();
        pixmap.drawPixmap(pixmap1, 0, 0);
        drawBuffer.end();
        drawBuffer.dispose();

        return pixmap;
    }

    public static void render(ShapeRenderer renderer1, float skipx, float skipy) {
        renderer1.begin(ShapeRenderer.ShapeType.Filled);
        renderer1.setColor(ColorHelper.rainbowLight());
        for (int r = 0; r < Statics.output.getSequencer().rhythm.length; r++) {
            renderer1.setColor(ColorHelper.rainbowLight());
            for (int r1 = 0; r1 < 16; r1++) {
                if (Statics.output.getSequencer().rhythm[r][r1] > 0) {
                    renderer1.rect(r1 * skipx + 2, (r)
                            * skipy + 2, skipx - 4, skipy - 4);
                }
            }
        }
        renderer1.end();
    }

    static Stack<DrumData> sequences = new Stack<DrumData>();

    public static DrumData peekStack() {
        if (sequences.empty()) return null;
        DrumData peek = sequences.peek();
        return peek;
    }

    public static DrumData popStack() {
        if (sequences.empty()) return null;
        return sequences.pop();
    }

    public static void pushStack(DrumData sd) {
        sequences.push(sd);
    }


}
