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

/**
 * Created by Paul on 1/10/2017.
 */
public class SequencerData extends InstrumentData {
    static Stack<SequencerData>[] sequences = new Stack[Statics.NUM_SYNTHS];
    static {
        for (int i = 0; i < Statics.NUM_SYNTHS; i++) {
            sequences[i] = new Stack<SequencerData>();
        }
    }
    public final byte[] note = new byte[16];
    public final boolean[] pause = new boolean[16];
    public final boolean[] slide = new boolean[16];
    //    public SequencerData parent;
//    public SequencerData child;
//    public static SequencerData currentSequence;
    public final boolean[] accent = new boolean[16];
    private int synthIndex;

    public SequencerData(int synthIndex) {
        this.synthIndex = synthIndex;
        for (int x1 = 0; x1 < 16; x1++) {
            note[x1] = Statics.output.getSequencer().basslines[synthIndex].note[x1];
            pause[x1] = Statics.output.getSequencer().basslines[synthIndex].pause[x1];
            slide[x1] = Statics.output.getSequencer().basslines[synthIndex].slide[x1];
            accent[x1] = Statics.output.getSequencer().basslines[synthIndex].accent[x1];
        }
        pixmap = drawPixmap(300, 300);
        region = new TextureRegion(new Texture(pixmap));
        region.flip(false, true);
    }

    public SequencerData() {
        this(Statics.currentSynth);
    }

    public static void render(ShapeRenderer renderer1, float skipx, float skipy, int synthIndex, int octaveOffset) {
        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);

        // Draw notes (circles)
        renderer1.begin(ShapeRenderer.ShapeType.Filled);
        for (int i = 0; i < 16; i++) {
            if (Statics.output.getSequencer().basslines[synthIndex].pause[i]) {
                continue;
            }
            if (Statics.output.getSequencer().basslines[synthIndex].accent[i]) {
                renderer1.setColor(ColorHelper.rainbowInverse());
                renderer1.circle(i * skipx + skipx / 2, (Statics.output.getSequencer().basslines[synthIndex].note[i] - (octaveOffset * 12) + 16) * skipy + skipy / 2, Math.min(skipx, skipy) / 2);
                renderer1.setColor(Color.BLACK);
                renderer1.circle(i * skipx + skipx / 2, (Statics.output.getSequencer().basslines[synthIndex].note[i] - (octaveOffset * 12) + 16) * skipy + skipy / 2, Math.min(skipx, skipy) / 3);

            } else {
                renderer1.setColor(ColorHelper.rainbowLight());
                renderer1.circle(i * skipx + skipx / 2, (Statics.output.getSequencer().basslines[synthIndex].note[i] - (octaveOffset * 12) + 16) * skipy + skipy / 2, Math.min(skipx, skipy) / 2);
            }
        }
        renderer1.end();

        // Draw slides
        renderer1.begin(ShapeRenderer.ShapeType.Filled);
        for (int i = 0; i < 16; i++) {
            if (Statics.output.getSequencer().basslines[synthIndex].slide[i]) {
                Color c = Statics.output.getSequencer().basslines[synthIndex].accent[i] ? ColorHelper.rainbowInverse() : ColorHelper.rainbowLight();
                renderer1.setColor(c.r, c.g, c.b, 0.5f);

                float radius = Math.min(skipx, skipy) / 2;
                float x1 = i * skipx + skipx / 2;
                float y1 = (Statics.output.getSequencer().basslines[synthIndex].note[i] - (octaveOffset * 12) + 16) * skipy + skipy / 2;
                int nextNoteIndex = (i + 1) % 16;
                float x2 = (i + 1) * skipx + skipx / 2;
                float y2 = (Statics.output.getSequencer().basslines[synthIndex].note[nextNoteIndex] - (octaveOffset * 12) + 16) * skipy + skipy / 2;

                renderer1.triangle(x1, y1 - radius, x1, y1 + radius, x2, y2 + radius);
                renderer1.triangle(x1, y1 - radius, x2, y2 + radius, x2, y2 - radius);
            }
        }
        renderer1.end();

        Gdx.gl.glDisable(GL20.GL_BLEND);
    }

    public static void render(ShapeRenderer renderer1, float skipx, float skipy) {
        render(renderer1, skipx, skipy, Statics.currentSynth, 0);
    }

    public static SequencerData peekStack(int synthIndex) {
        if (sequences[synthIndex].empty()) return null;
        return sequences[synthIndex].peek();
    }

    public static SequencerData peekStack() {
        return peekStack(Statics.currentSynth);
    }

    public static SequencerData popStack(int synthIndex) {
        if (sequences[synthIndex].empty()) return null;
        return sequences[synthIndex].pop();
    }

    public static SequencerData popStack() {
        return popStack(Statics.currentSynth);
    }

    public static void pushStack(SequencerData sd) {
        sequences[sd.synthIndex].push(sd);
    }

    public void refresh() {
        for (int x1 = 0; x1 < 16; x1++) {
            Statics.output.getSequencer().basslines[this.synthIndex].note[x1] = note[x1];
            Statics.output.getSequencer().basslines[this.synthIndex].pause[x1] = pause[x1];
            Statics.output.getSequencer().basslines[this.synthIndex].slide[x1] = slide[x1];
            Statics.output.getSequencer().basslines[this.synthIndex].accent[x1] = accent[x1];
        }
    }

    public void randomize() {
        for (int i = 0; i < 16; i++) {
            note[i] = (byte) (Math.random() * 12);
            pause[i] = Math.random() > 0.5;
            slide[i] = Math.random() > 0.8;
            accent[i] = Math.random() > 0.8;
        }
    }

    @Override
    public String toString() {
        String s = "";
        for (int i = 0; i < 16; i++) {
            s += note[i] + (pause[i] ? "p" : "") + (slide[i] ? "s" : "") + (accent[i] ? "a" : "") + " ";
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
        float skipy = ((float) h / 31f);
        render(renderer, skipx, skipy, this.synthIndex, 0);
        renderer.begin(ShapeRenderer.ShapeType.Line);
        renderer.setColor(ColorHelper.rainbowInverse());
        for (int i = 0; i < 5; i++) {
            renderer.rect(i, i, w - i * 2, h - i * 2);
        }
        renderer.end();
        Pixmap pixmap1 = ScreenUtils.getFrameBufferPixmap(0, 0, w, h);
        Pixmap pixmap = new Pixmap((int) w, (int) h, Pixmap.Format.RGBA8888);
        pixmap.setColor(ColorHelper.rainbowInverse());
        pixmap.fill();
        pixmap.drawPixmap(pixmap1, 0, 0);
        drawBuffer.end();
        drawBuffer.dispose();

        return pixmap;
    }

}
