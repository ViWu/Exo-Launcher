package myapplication.applauncher;

import android.graphics.drawable.Drawable;
import android.widget.LinearLayout;

public class Application implements Comparable<Application>{
    Drawable icon;
    String name;
    String label;
    int uid;
    LinearLayout ll;
    int x,y;
    int drawerIndex;

    @Override
    public int compareTo(Application app) {
        String temp = app.label.toUpperCase();
        return label.toUpperCase().compareTo(temp);
    }
}
