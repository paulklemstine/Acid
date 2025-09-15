package com.acid.actors;

import com.acid.ColorHelper;
import com.acid.DrumData;
import com.acid.SequencerData;
import com.acid.Statics;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;

public class SequenceActor extends Actor {

    private float y2 = -1;
    private float x2 = -1;


    public boolean noteAccent = false;
    public boolean noteSlide = false;


    public SequenceActor() {
        this.setWidth(320);
        this.setHeight(280);
        this.addListener(new InputListener() {
            public boolean touchDown(InputEvent event, float x, float y,
                                     int pointer, int button) {
                int x1 = (int) (x / ((getWidth() / 16)));
                int y1 = (int) (y / (getHeight() / 31)) - 16;

                if (x1 >= 0 && x1 < 16 && y1 >= -16 && y1 < 16) {
                    if (x1 == x2 && y1 == y2) {
                        // Toggle pause on tap
                        Statics.output.getSequencer().basslines[Statics.currentSynth].pause[x1] = !Statics.output.getSequencer().basslines[Statics.currentSynth].pause[x1];
                    } else {
                        // Set note properties on new note
                        Statics.output.getSequencer().basslines[Statics.currentSynth].note[x1] = (byte) y1;
                        Statics.output.getSequencer().basslines[Statics.currentSynth].pause[x1] = false;
                        Statics.output.getSequencer().basslines[Statics.currentSynth].accent[x1] = noteAccent;
                        Statics.output.getSequencer().basslines[Statics.currentSynth].slide[x1] = noteSlide;
                    }
                }
                x2 = x1;
                y2 = y1;
                return true;
            }

            public void touchUp(InputEvent event, float x, float y,
                                int pointer, int button) {
                x2 = -1;
                y2 = -1;
                new SequencerData();
            }

            public void touchDragged(InputEvent event, float x, float y,
                                     int pointer) {
                int x1 = (int) (x / ((getWidth() / 16)));
                int y1 = (int) (y / (getHeight() / 31)) - 16;
                if (x1 != x2 || y1 != y2) {
                    if (x1 >= 0 && x1 < 16 && y1 >= -16 && y1 < 16) {
                        // Set note properties on new note
                        Statics.output.getSequencer().basslines[Statics.currentSynth].note[x1] = (byte) y1;
                        Statics.output.getSequencer().basslines[Statics.currentSynth].pause[x1] = false;
                        Statics.output.getSequencer().basslines[Statics.currentSynth].accent[x1] = noteAccent;
                        Statics.output.getSequencer().basslines[Statics.currentSynth].slide[x1] = noteSlide;
                        x2 = x1;
                        y2 = y1;
                    }
                }
            }
        });
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {

        batch.end();
        Statics.renderer.setProjectionMatrix(batch.getProjectionMatrix());
        Statics.renderer.setTransformMatrix(batch.getTransformMatrix());
        Statics.renderer.translate(getX(), getY(), 0);
        Statics.renderer.scale(this.getScaleX(), this.getScaleY(), 1f);

        int skipx = (int) (getWidth() / 16);
        int skipy = (int) (getHeight() / 31);
        // grid
        Statics.renderer.begin(ShapeType.Line);
        Statics.renderer.setColor(ColorHelper.rainbowDark());
        for (int r = 0; r < 16; r += 4) {
            Statics.renderer.line(r * skipx, 0, r * skipx, getHeight());
        }
        for (int r = 0; r < 32; r++) {
            Statics.renderer.line(0, r * skipy, getWidth(), r * skipy);
        }
        Statics.renderer.end();

        Statics.renderer.begin(ShapeType.Line);
        Statics.renderer.setColor(ColorHelper.rainbowInverse());
        Statics.renderer.line(
                (Statics.output.getSequencer().step) % 16 * skipx, 0,
                (Statics.output.getSequencer().step) % 16 * skipx, getHeight());
        Statics.renderer.end();

        Statics.renderer.begin(ShapeType.Line);
        Statics.renderer.setColor(ColorHelper.rainbowInverse());
        Statics.renderer.rect(0, 0, this.getWidth(), this.getHeight());
        Statics.renderer.end();
        SequencerData.render(Statics.renderer, skipx, skipy);
        batch.begin();
    }

}