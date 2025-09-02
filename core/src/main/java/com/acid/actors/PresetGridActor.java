package com.acid.actors;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import synth.PatternGenerator;

public class PresetGridActor extends Group {

    private Skin skin;
    private Table table;
    private ShapeRenderer shapeRenderer;
    private boolean isSynth = true;
    private TextButton selectedPatternButton = null;

    public PresetGridActor(Skin skin) {
        this.skin = skin;
        this.table = new Table();
        this.shapeRenderer = new ShapeRenderer();
        addActor(table);
        buildGrid();
    }

    private void buildGrid() {
        table.clear();
        table.defaults().pad(2);

        String[] genres = isSynth ? PatternGenerator.getGenres() : PatternGenerator.getDrumGenres();

        for (final String genre : genres) {
            table.add(new Label(genre, skin)).colspan(16).left();
            table.row();

            String[] banks = isSynth ? PatternGenerator.getBanks(genre) : PatternGenerator.getDrumBanks(genre);
            for (final String bank : banks) {
                table.add(new Label(bank, skin)).left();

                String[] patterns = isSynth ? PatternGenerator.getPatterns(genre, bank) : new String[]{bank};
                for (final String patternName : patterns) {
                    final TextButton button = new TextButton("", skin);
                    button.addListener(new InputListener() {
                        public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                            if (selectedPatternButton != null) {
                                selectedPatternButton.setColor(Color.WHITE);
                            }
                            selectedPatternButton = (TextButton)event.getListenerActor();
                            selectedPatternButton.setColor(Color.RED);

                            if (isSynth) {
                                applySynthPattern(patternName);
                            } else {
                                applyDrumPattern(genre, bank);
                            }
                            return true;
                        }
                    });
                    table.add(button).width(20).height(20);
                }
                table.row();
            }
        }
    }

    private void applySynthPattern(String patternName) {
        if (patternName.toLowerCase().contains("bassline")) {
            PatternGenerator.generateBassline(sequencerView);
        } else if (patternName.toLowerCase().contains("melody")) {
            PatternGenerator.generateMelody(sequencerView);
        } else if (patternName.toLowerCase().contains("pad")) {
            PatternGenerator.generateHarmony(sequencerView);
        } else if (patternName.toLowerCase().contains("arp")) {
            PatternGenerator.generateArpeggio(sequencerView);
        } else {
            PatternGenerator.generateMusical(sequencerView);
        }
    }

    private void applyDrumPattern(String genre, String bank) {
        int[][] drumPattern = PatternGenerator.getDrumPattern(genre, bank);
        if (drumPattern != null) {
            for (int i = 0; i < 4; i++) {
                for (int j = 0; j < 16; j++) {
                    com.acid.Statics.output.getSequencer().rhythm[i][j] = drumPattern[i][j];
                }
            }
        }
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        batch.end();
        shapeRenderer.setProjectionMatrix(batch.getProjectionMatrix());
        shapeRenderer.setTransformMatrix(batch.getTransformMatrix());
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        shapeRenderer.setColor(Color.RED);
        shapeRenderer.rect(getX(), getY(), getWidth(), getHeight());
        shapeRenderer.end();
        batch.begin();
        super.draw(batch, parentAlpha);
    }

    public void showSynthPresets() {
        this.isSynth = true;
        buildGrid();
    }

    public void showDrumPresets() {
        this.isSynth = false;
        buildGrid();
    }

    private int sequencerView;

    public void setSequencerView(int sequencerView) {
        this.sequencerView = sequencerView;
    }
}
