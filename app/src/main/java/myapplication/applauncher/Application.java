package myapplication.applauncher;

import android.graphics.drawable.Drawable;

public class Application implements Comparable<Application>{
    Drawable icon;
    String name;
    String label;

    @Override
    public int compareTo(Application app) {
        String temp = app.label.toUpperCase();
        return label.toUpperCase().compareTo(temp);
    }
}
