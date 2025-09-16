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
    private static final String[] noteNames = {"C", "C#", "D", "D#", "E", "F", "F#", "G", "G#", "A", "A#", "B"};

    private float y2 = -1;
    private float x2 = -1;


    public int octaveOffset = 0;


    public SequenceActor() {
        this.setWidth(320);
        this.setHeight(280);
        this.addListener(new InputListener() {
            public boolean touchDown(InputEvent event, float x, float y,
                                     int pointer, int button) {
                int x1 = (int) (x / ((getWidth() / 16)));
                int y1 = (int) (y / (getHeight() / 31)) - 16 + (octaveOffset * 12);

                if (x1 >= 0 && x1 < 16 && y1 >= -16 && y1 < 16) {
                    boolean isNoteAtStep = !Statics.output.getSequencer().basslines[Statics.currentSynth].pause[x1];
                    byte noteAtStep = Statics.output.getSequencer().basslines[Statics.currentSynth].note[x1];
                    boolean accentAtStep = Statics.output.getSequencer().basslines[Statics.currentSynth].accent[x1];
                    boolean slideAtStep = Statics.output.getSequencer().basslines[Statics.currentSynth].slide[x1];

                    if (isNoteAtStep && noteAtStep == y1) {
                        // Tapped on an existing note, cycle through states
                        if (!accentAtStep && !slideAtStep) { // State 1: Note only -> State 2: Add Accent
                            Statics.output.getSequencer().basslines[Statics.currentSynth].accent[x1] = true;
                        } else if (accentAtStep && !slideAtStep) { // State 2: Accent only -> State 3: Add Slide, remove Accent
                            Statics.output.getSequencer().basslines[Statics.currentSynth].accent[x1] = false;
                            Statics.output.getSequencer().basslines[Statics.currentSynth].slide[x1] = true;
                        } else if (!accentAtStep && slideAtStep) { // State 3: Slide only -> State 4: Add Accent
                            Statics.output.getSequencer().basslines[Statics.currentSynth].accent[x1] = true;
                        } else { // State 4: Accent and Slide -> State 5: Pause
                            Statics.output.getSequencer().basslines[Statics.currentSynth].pause[x1] = true;
                            Statics.output.getSequencer().basslines[Statics.currentSynth].accent[x1] = false;
                            Statics.output.getSequencer().basslines[Statics.currentSynth].slide[x1] = false;
                        }
                    } else {
                        // Tapped on an empty spot or a different note in the same column.
                        // Create a new note in state 1.
                        Statics.output.getSequencer().basslines[Statics.currentSynth].note[x1] = (byte) y1;
                        Statics.output.getSequencer().basslines[Statics.currentSynth].pause[x1] = false;
                        Statics.output.getSequencer().basslines[Statics.currentSynth].accent[x1] = false;
                        Statics.output.getSequencer().basslines[Statics.currentSynth].slide[x1] = false;
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
            }

            public void touchDragged(InputEvent event, float x, float y,
                                     int pointer) {
                int x1 = (int) (x / ((getWidth() / 16)));
                int y1 = (int) (y / (getHeight() / 31)) - 16 + (octaveOffset * 12);
                if (x1 != x2 || y1 != y2) {
                    if (x1 >= 0 && x1 < 16 && y1 >= -16 && y1 < 16) {
                        // Set note properties on new note
                        Statics.output.getSequencer().basslines[Statics.currentSynth].note[x1] = (byte) y1;
                        Statics.output.getSequencer().basslines[Statics.currentSynth].pause[x1] = false;
                        Statics.output.getSequencer().basslines[Statics.currentSynth].accent[x1] = false;
                        Statics.output.getSequencer().basslines[Statics.currentSynth].slide[x1] = false;
                        x2 = x1;
                        y2 = y1;
                    }
                }
            }
        });
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        int skipx = (int) (getWidth() / 16);
        int skipy = (int) (getHeight() / 31);

        batch.end();

        Statics.renderer.setProjectionMatrix(batch.getProjectionMatrix());
        Statics.renderer.setTransformMatrix(batch.getTransformMatrix());
        Statics.renderer.translate(getX(), getY(), 0);
        Statics.renderer.scale(this.getScaleX(), this.getScaleY(), 1f);

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
        SequencerData.render(Statics.renderer, skipx, skipy, Statics.currentSynth, octaveOffset);

        batch.begin();
        for (int i = 0; i < 31; i++) {
            int noteValue = i - 16 + (octaveOffset * 12);
            int midiNote = noteValue + 36;
            if (midiNote % 12 == 0) { // Only draw C notes for clarity
                int octave = (midiNote / 12) - 1;
                String label = "C" + octave;
                Statics.font.draw(batch, label, getX() - 30, getY() + i * skipy + Statics.font.getLineHeight());
            }
        }
    }

}