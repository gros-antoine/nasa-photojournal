package com.grosantoine.fr.myapplication;

import android.graphics.Bitmap;

import java.util.ArrayList;

public class Triple {

    ArrayList<String> first;
    ArrayList<Bitmap> second;
    ArrayList<String> third;

    public Triple(ArrayList<String> first, ArrayList<Bitmap> second, ArrayList<String> third) {

        this.first = first;
        this.second = second;
        this.third = third;
    }

    public ArrayList<String> getFirst() {
        return this.first;
    }

    public ArrayList<Bitmap> getSecond() {
        return this.second;
    }

    public ArrayList<String> getThird() {
        return this.third;
    }

}
