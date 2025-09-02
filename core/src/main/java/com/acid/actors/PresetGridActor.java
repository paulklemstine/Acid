package com.acid.actors;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import synth.PatternGenerator;
import java.util.ArrayList;

public class PresetGridActor extends Group {

    private Skin skin;
    private Table table;
    private ShapeRenderer shapeRenderer;
    private ArrayList<String> navigationPath = new ArrayList<>();
    private boolean isSynth = true;

    public PresetGridActor(Skin skin) {
        this.skin = skin;
        this.table = new Table();
        this.shapeRenderer = new ShapeRenderer();
        addActor(table);
        updateGrid();
    }

    private void updateGrid() {
        table.clear();

        if (!navigationPath.isEmpty()) {
            TextButton backButton = new TextButton("..Back", skin);
            backButton.addListener(new InputListener() {
                public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                    if (!navigationPath.isEmpty()) {
                        String removed = navigationPath.remove(navigationPath.size() - 1);
                        if (navigationPath.size() == 0) {
                            selectedGenre = null;
                        }
                        selectedBank = null;
                        updateGrid();
                    }
                    return true;
                }
            });
            table.add(backButton).colspan(4).left();
            table.row();
        }

        if (navigationPath.isEmpty()) {
            displayGenres();
        } else if (navigationPath.size() == 1) {
            displayBanks();
        } else if (navigationPath.size() == 2) {
            displayPatterns();
        }
    }

    private String selectedGenre = null;
    private String selectedBank = null;

    private void displayGenres() {
        String[] genres = isSynth ? PatternGenerator.getGenres() : PatternGenerator.getDrumGenres();
        int col = 0;
        for (final String genre : genres) {
            TextButton button = new TextButton(genre, skin);
            if (genre.equals(selectedGenre)) {
                button.setColor(Color.RED);
            }
            button.addListener(new InputListener() {
                public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                    selectedGenre = genre;
                    navigationPath.add(genre);
                    updateGrid();
                    return true;
                }
            });
            table.add(button).pad(5);
            if (++col % 4 == 0) {
                table.row();
            }
        }
    }

    private void displayBanks() {
        String genre = navigationPath.get(0);
        String[] banks = isSynth ? PatternGenerator.getBanks(genre) : PatternGenerator.getDrumBanks(genre);
        int col = 0;
        for (final String bank : banks) {
            TextButton button = new TextButton(bank, skin);
            if (bank.equals(selectedBank)) {
                button.setColor(Color.RED);
            }
            button.addListener(new InputListener() {
                public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                    selectedBank = bank;
                    navigationPath.add(bank);
                    updateGrid();
                    return true;
                }
            });
            table.add(button).pad(5);
            if (++col % 4 == 0) {
                table.row();
            }
        }
    }

    private void displayPatterns() {
        String genre = navigationPath.get(0);
        String bank = navigationPath.get(1);
        String[] patterns = isSynth ? PatternGenerator.getPatterns(genre, bank) : new String[]{}; // Drums handled differently

        if (!isSynth) {
            TextButton button = new TextButton(bank, skin);
            button.addListener(new InputListener() {
                public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                    int[][] pattern = PatternGenerator.getDrumPattern(genre, bank);
                    if (pattern != null) {
                        for (int i = 0; i < 4; i++) {
                            for (int j = 0; j < 16; j++) {
                                com.acid.Statics.output.getSequencer().rhythm[i][j] = pattern[i][j];
                            }
                        }
                    }
                    navigationPath.clear();
                    updateGrid();
                    return true;
                }
            });
            table.add(button).pad(5);
            return;
        }

        int col = 0;
        for (final String pattern : patterns) {
            TextButton button = new TextButton(pattern, skin);
            button.addListener(new InputListener() {
                public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                    if (pattern.toLowerCase().contains("bassline")) {
                        PatternGenerator.generateBassline(sequencerView);
                    } else if (pattern.toLowerCase().contains("melody")) {
                        PatternGenerator.generateMelody(sequencerView);
                    } else if (pattern.toLowerCase().contains("pad")) {
                        PatternGenerator.generateHarmony(sequencerView);
                    } else if (pattern.toLowerCase().contains("arp")) {
                        PatternGenerator.generateArpeggio(sequencerView);
                    } else {
                        PatternGenerator.generateMusical(sequencerView);
                    }
                    navigationPath.clear();
                    updateGrid();
                    return true;
                }
            });
            table.add(button).pad(5);
            if (++col % 4 == 0) {
                table.row();
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
        this.navigationPath.clear();
        this.selectedGenre = null;
        this.selectedBank = null;
        updateGrid();
    }

    public void showDrumPresets() {
        this.isSynth = false;
        this.navigationPath.clear();
        this.selectedGenre = null;
        this.selectedBank = null;
        updateGrid();
    }

    private int sequencerView;

    public void setSequencerView(int sequencerView) {
        this.sequencerView = sequencerView;
    }
}
