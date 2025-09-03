package com.acid.actors;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.SelectBox;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.utils.Array;
import synth.PatternGenerator;

public class PresetGridActor extends Group {

    private Skin skin;
    private Table container;
    private SelectBox<String> genreSelectBox;
    private Table gridTable;
    private ShapeRenderer shapeRenderer;
    private boolean isSynth = true;
    private TextButton selectedPatternButton = null;

    public PresetGridActor(Skin skin) {
        this.skin = skin;
        this.container = new Table();
        this.shapeRenderer = new ShapeRenderer();
        addActor(container);

        genreSelectBox = new SelectBox<>(skin);
        gridTable = new Table();

        container.add(genreSelectBox).left();
        container.row();
        container.add(gridTable).expand().fill();

        genreSelectBox.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                updateGrid();
            }
        });

        updateGenreList();
        updateGrid();
    }

    private void updateGenreList() {
        String[] genres = isSynth ? PatternGenerator.getGenres() : PatternGenerator.getDrumGenres();
        genreSelectBox.setItems(new Array<>(genres));
    }

    private void updateGrid() {
        gridTable.clear();
        gridTable.defaults().pad(2);

        String selectedGenre = genreSelectBox.getSelected();
        if (selectedGenre == null) return;

        String[] banks = isSynth ? PatternGenerator.getBanks(selectedGenre) : PatternGenerator.getDrumBanks(selectedGenre);
        for (final String bank : banks) {
            gridTable.add(new Label(bank, skin)).left();

            String[] patterns = isSynth ? PatternGenerator.getPatterns(selectedGenre, bank) : new String[]{bank};
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
                                applySynthPattern(selectedGenre, bank, patternName);
                        } else {
                            applyDrumPattern(selectedGenre, bank);
                        }
                        return true;
                    }
                });
                gridTable.add(button).width(20).height(20);
            }
            gridTable.row();
        }
    }

    private void applySynthPattern(String genre, String bank, String patternName) {
        int[] pattern = PatternGenerator.getPattern(genre, bank, patternName);
        PatternGenerator.applySynthPattern(pattern, sequencerView);
    }

    private void applyDrumPattern(String genre, String bank) {
        int[][] drumPattern = PatternGenerator.getDrumPattern(genre, bank);
        if (drumPattern != null) {
            for (int i = 0; i < 7; i++) {
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
        updateGenreList();
        updateGrid();
    }

    public void showDrumPresets() {
        this.isSynth = false;
        updateGenreList();
        updateGrid();
    }

    private int sequencerView;

    public void setSequencerView(int sequencerView) {
        this.sequencerView = sequencerView;
    }
}
