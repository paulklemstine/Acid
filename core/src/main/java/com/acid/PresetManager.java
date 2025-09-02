package com.acid;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;

import java.util.ArrayList;

public class PresetManager {

    private static final String PRESET_DIR = "presets/";

    public static void savePreset(Preset preset, String name, String genre) {
        String path = PRESET_DIR + genre + "/" + name + ".preset";
        try {
            Serializer.save(preset, path);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static Preset loadPreset(String name, String genre) {
        String path = PRESET_DIR + genre + "/" + name + ".preset";
        try {
            Object o = Serializer.load(path);
            if (o instanceof Preset) {
                return (Preset) o;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String[] getPresetNames(String genre) {
        String path = PRESET_DIR + genre + "/";
        FileHandle dir = Gdx.files.local(path);
        if (dir.exists() && dir.isDirectory()) {
            FileHandle[] files = dir.list(".preset");
            String[] names = new String[files.length];
            for (int i = 0; i < files.length; i++) {
                names[i] = files[i].nameWithoutExtension();
            }
            return names;
        }
        return new String[0];
    }

    public static String[] getGenres() {
        FileHandle dir = Gdx.files.local(PRESET_DIR);
        if (dir.exists() && dir.isDirectory()) {
            FileHandle[] files = dir.list();
            ArrayList<String> genres = new ArrayList<>();
            for (FileHandle file : files) {
                if (file.isDirectory()) {
                    genres.add(file.name());
                }
            }
            return genres.toArray(new String[0]);
        }
        return new String[0];
    }
}
