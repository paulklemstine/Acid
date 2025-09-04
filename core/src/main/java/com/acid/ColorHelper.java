package com.acid;

import com.badlogic.gdx.graphics.Color;

public class ColorHelper {

    public static final Color BACKGROUND = new Color(0.1f, 0.1f, 0.1f, 1);
    public static final Color UI_GRAY = new Color(0.3f, 0.3f, 0.3f, 1);
    public static final Color UI_LIGHT_GRAY = new Color(0.6f, 0.6f, 0.6f, 1);
    public static final Color UI_VERY_LIGHT_GRAY = new Color(0.8f, 0.8f, 0.8f, 1);
    public static final Color RED = new Color(1, 0, 0, 1);
    public static final Color YELLOW = new Color(1, 1, 0, 1);
    public static final Color GREEN = new Color(0, 1, 0, 1);


    public static Color numberToColorPercentage(double value) {
        return UI_LIGHT_GRAY;
    }

    public static Color rainbow() {
        return UI_LIGHT_GRAY;
    }
    public static Color rainbowInverse() {
        return UI_GRAY;
    }

    public static Color rainbowDark() {
        return UI_GRAY;
    }

    public static Color rainbowLight() {
        return UI_VERY_LIGHT_GRAY;
    }

}