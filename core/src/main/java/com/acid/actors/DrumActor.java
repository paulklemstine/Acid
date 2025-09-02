package com.acid.actors;

import com.acid.ColorHelper;
import com.acid.DrumData;
import com.acid.Statics;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;

public class DrumActor extends Actor {

    private float y2;
    private float x2;
    private final int numRows;
    private final String[] drumNames;
    private final BitmapFont font;


    public DrumActor(int numRows, String[] drumNames, BitmapFont font) {
        this.numRows = numRows;
        this.drumNames = drumNames;
        this.font = font;
        this.setWidth(320);
        this.setHeight(numRows * 40);
        this.addListener(new InputListener() {
            public boolean touchDown(InputEvent event, float x, float y,
                                     int pointer, int button) {
                int x1 = (int) (x / ((getWidth() / 16)));
                int y1 = (int) (y / (getHeight() / numRows));
                ttouch(x1, y1);
                return true;
            }

            public void touchUp(InputEvent event, float x, float y,
                                int pointer, int button) {
                new DrumData();
            }

            public void touchDragged(InputEvent event, float x, float y,
                                     int pointer) {
                int x1 = (int) (x / ((getWidth() / 16)));
                int y1 = (int) (y / (getHeight() / numRows));
                if (x1 != x2 || y1 != y2) {
                    ttouch(x1, y1);
                }
            }
        });
    }


    public void ttouch(int x1, int y1) {
        if (x1 < 16 && x1 > -1 && y1 >= 0 && y1 < numRows) {
            if (Statics.output.getSequencer().rhythm[y1][x1] > 0) {
                Statics.output.getSequencer().rhythm[y1][x1] = 0;
            } else
                Statics.output.getSequencer().rhythm[y1][x1] = 127;
        }

        x2 = x1;
        y2 = y1;
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {

        batch.end();
        Statics.renderer.setProjectionMatrix(batch.getProjectionMatrix());
        Statics.renderer.setTransformMatrix(batch.getTransformMatrix());
        Statics.renderer.translate(getX(), getY(), 0);
        Statics.renderer.scale(this.getScaleX(), this.getScaleY(), 1f);

        int skipx = (int) (getWidth() / 16);
        int skipy = (int) (getHeight() / numRows);
        // grid
        Statics.renderer.begin(ShapeType.Line);
        Statics.renderer.setColor(ColorHelper.rainbowDark());
        for (int r = 0; r < 16; r += 4) {
            Statics.renderer.line(r * skipx, 0, r * skipx, getHeight());
        }
        for (int r = 0; r < numRows + 1; r++) {
            Statics.renderer.line(0, r * skipy, getWidth(), r * skipy);
        }
        Statics.renderer.end();

        Statics.renderer.begin(ShapeType.Line);
        Statics.renderer.setColor(ColorHelper.rainbow());
        Statics.renderer.line(
                (Statics.output.getSequencer().step) % 16 * skipx, 0,
                (Statics.output.getSequencer().step) % 16 * skipx, getHeight());
        Statics.renderer.end();

        Statics.renderer.begin(ShapeType.Line);
        Statics.renderer.setColor(ColorHelper.rainbow());
        Statics.renderer.rect(0, 0, this.getWidth(), this.getHeight());
        Statics.renderer.end();

        DrumData.render(Statics.renderer, skipx, skipy);

        batch.begin();
        for (int i = 0; i < numRows; i++) {
            font.draw(batch, drumNames[i], -30, i * skipy + skipy / 2f);
        }
    }

}