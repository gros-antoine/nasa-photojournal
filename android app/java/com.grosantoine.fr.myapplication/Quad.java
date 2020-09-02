package com.grosantoine.fr.myapplication;

import android.graphics.Bitmap;

import java.util.ArrayList;

public class Quad {

    ArrayList<String> first;
    ArrayList<Bitmap> second;
    ArrayList<String> third;
    ArrayList<String> fourth;

    public Quad(ArrayList<String> first, ArrayList<Bitmap> second, ArrayList<String> third, ArrayList<String> fourth) {

        this.first = first;
        this.second = second;
        this.third = third;
        this.fourth = fourth;

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

    public ArrayList<String> getFourth() {
        return this.fourth;
    }
}
