package com.acid;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import synth.BasslineSynthesizer;
import synth.Output;
import synth.Synthesizer;

public class Statics {


    public static ShapeRenderer renderer;
    public static String sdcard;
    public static FileHandle exportConvertedWav;
    public static FileHandle exportConvertedFlac;

    public static BasslineSynthesizer[] synths;
    public static int currentSynth = 0;
    public static Synthesizer drums;
    public static Output output;

    public static boolean recording = true;
    public static boolean free = true;
    public static boolean export;
    public static FileHandle exportFile;
    public static FileHandle saveName;

//    public static FileHandle getFileHandle(String selected) {
//        //if (Gdx.files.isExternalStorageAvailable()) return Gdx.files.external(selected);
//        if (Gdx.files.isLocalStorageAvailable()) return Gdx.files.local(selected);
//        return null;
//    }
}
