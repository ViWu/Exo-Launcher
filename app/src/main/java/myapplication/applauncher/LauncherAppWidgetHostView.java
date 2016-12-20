package myapplication.applauncher;

import android.appwidget.AppWidgetHostView;
import android.content.Context;
import android.os.Vibrator;
import android.view.MotionEvent;
import android.widget.RelativeLayout;

import java.util.Calendar;

public class LauncherAppWidgetHostView extends AppWidgetHostView {

    private int xDelta = this.getWidth() / 2;
    private int yDelta = this.getHeight() / 2;
    private static final int MAX_CLICK_DURATION = 100;
    private static final int MIN_LONG_CLICK_DURATION = 1000;
    private long startClickTime, clickDuration;
    public static Vibrator vibration;

    public LauncherAppWidgetHostView(Context context) {
        super(context);
    }

    public boolean onInterceptTouchEvent(MotionEvent ev) {

        final int xPos = (int) ev.getRawX();
        final int yPos = (int) ev.getRawY();
        switch (ev.getAction()) {
            case MotionEvent.ACTION_MOVE:
                clickDuration = Calendar.getInstance().getTimeInMillis() - startClickTime;
                RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) this.getLayoutParams();
                layoutParams.leftMargin = xPos - xDelta;
                layoutParams.topMargin = yPos - yDelta;
                layoutParams.rightMargin = -250;
                layoutParams.bottomMargin = -250;
                vibration = MainActivity.vibration;
                if(clickDuration >= MIN_LONG_CLICK_DURATION) {

                    if (clickDuration < MIN_LONG_CLICK_DURATION + 75)
                        vibration.vibrate(25);
                    this.setLayoutParams(layoutParams);
                }
                break;
            case MotionEvent.ACTION_DOWN: {
                startClickTime = Calendar.getInstance().getTimeInMillis();
                RelativeLayout.LayoutParams lParams = (RelativeLayout.LayoutParams) this.getLayoutParams();
                xDelta = xPos - lParams.leftMargin;
                yDelta = yPos - lParams.topMargin;
                break;
            }

            case MotionEvent.ACTION_UP:
                clickDuration = Calendar.getInstance().getTimeInMillis() - startClickTime;
                if(clickDuration > MAX_CLICK_DURATION) {
                    //no click event
                    return true;
                }

        }

        return false;
    }

}