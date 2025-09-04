package com.acid.actors;

import com.acid.ColorHelper;
import com.acid.KnobImpl;
import com.acid.Statics;
import com.badlogic.gdx.Files;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;


public class KnobActor extends Actor {

    private final BitmapFont font;
    private final int id;
    private final String name;
    private final int synthIndex;

    public KnobActor(String name, final int id, final int synthIndex) {
        font = new BitmapFont(Gdx.app.getFiles().getFileHandle("data/font.fnt",
                Files.FileType.Internal), false);

        font.getData().setScale(.75f);
        this.id = id;
        this.name = name;
        this.synthIndex = synthIndex;
        this.setWidth(60);
        this.setHeight(60);
        this.addListener(new InputListener() {
            private float distx;
            private float disty;

            public boolean touchDown(InputEvent event, float x, float y,
                                     int pointer, int button) {
                distx = x;
                disty = y;
                KnobImpl.touchDown(synthIndex, id);
                return true;
            }

            public void touchUp(InputEvent event, float x, float y,
                                int pointer, int button) {
                KnobImpl.touchReleased(synthIndex, id);
            }

            public void touchDragged(InputEvent event, float x, float y,
                                     int pointer) {

                KnobImpl.touchDragged(synthIndex, id, (distx - x) + (disty - y));
            }
        });
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        int xc = 20;
        int yc = 20;
        float rotation = KnobImpl.getRotation(synthIndex, id);
        font.setColor(ColorHelper.UI_VERY_LIGHT_GRAY);
        GlyphLayout gl1 = new GlyphLayout(font, name);
        font.draw(batch, name, this.getX() + xc - gl1.width / 2, this.getY() + this.getHeight() - gl1.height);


        batch.end();

        Statics.renderer.setProjectionMatrix(batch.getProjectionMatrix());
        Statics.renderer.setTransformMatrix(batch.getTransformMatrix());
        Statics.renderer.translate(getX(), getY(), 0);

        Statics.renderer.begin(ShapeType.Filled);
        Statics.renderer.setColor(ColorHelper.UI_GRAY);
        Statics.renderer.circle(xc, yc, 15, 20);
        Statics.renderer.end();

        Statics.renderer.begin(ShapeType.Line);
        Statics.renderer.setColor(ColorHelper.UI_LIGHT_GRAY);
        Statics.renderer.circle(xc, yc, 15, 20);
        Statics.renderer.end();


        Statics.renderer.begin(ShapeType.Filled);
        Statics.renderer.setColor(ColorHelper.UI_VERY_LIGHT_GRAY);
        Statics.renderer.rectLine(xc, yc, xc + MathUtils.cosDeg(rotation) * 15, yc + MathUtils.sinDeg(rotation) * 15, 2);
        Statics.renderer.end();

        batch.begin();

    }

}