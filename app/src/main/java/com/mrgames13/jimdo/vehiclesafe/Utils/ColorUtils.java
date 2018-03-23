package com.mrgames13.jimdo.vehiclesafe.Utils;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.support.v7.graphics.Palette;

import com.mrgames13.jimdo.vehiclesafe.R;

public class ColorUtils {

    //Konstanten

    //Variablen als Objekte
    private Resources res;

    //Variablen

    public ColorUtils(Resources res) {
        this.res = res;
    }

    public int getVibrantColor(Bitmap image) {
        try{
            Palette palette = Palette.from(image).generate();
            return palette.getVibrantColor(res.getColor(R.color.colorPrimary));
        } catch (Exception e) {}
        return 0;
    }

    public int getMutedColor(Bitmap image) {
        Palette palette = Palette.from(image).generate();
        return palette.getMutedColor(res.getColor(R.color.colorPrimary));
    }

    public int getDarkMutedColor(Bitmap image) {
        Palette palette = Palette.from(image).generate();
        return palette.getDarkMutedColor(res.getColor(R.color.colorPrimary));
    }

    public int darkenColor(int color) {
        float[] hsv = new float[3];
        Color.colorToHSV(color, hsv);
        hsv[2] *= 0.8f;
        return Color.HSVToColor(hsv);
    }

    public int brighterColor(int color) {
        float[] hsv = new float[3];
        Color.colorToHSV(color, hsv);
        hsv[2] *= 1.2f;
        return Color.HSVToColor(hsv);
    }

    public int addTransparency(int color) {
        int alpha = Math.round(Color.alpha(color) * 0.6f);
        int red = Color.red(color);
        int green = Color.green(color);
        int blue = Color.blue(color);
        return Color.argb(alpha, red, green, blue);
    }
}