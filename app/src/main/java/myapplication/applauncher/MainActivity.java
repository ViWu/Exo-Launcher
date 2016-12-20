package myapplication.applauncher;

import android.app.Activity;
import android.app.AlertDialog;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.appwidget.AppWidgetProviderInfo;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Bundle;
import android.os.Vibrator;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SlidingDrawer;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class MainActivity extends Activity {

    DrawerAdapter drawerAdapterObject;
    GridView drawerGrid;
    SlidingDrawer slidingDrawer;
    RelativeLayout homeView;

    protected static ArrayList<Application> apps;
    protected static ArrayList<Integer> widgets;
    static boolean appLaunchable = true;
    private static final int MAX_CLICK_DURATION = 100;
    private static final int MIN_LONG_CLICK_DURATION = 1000;
    private long startClickTime, clickDuration;
    public static Vibrator vibration;
    ImageView remove;

    AppWidgetManager mAppWidgetManager;
    LauncherAppWidgetHost mAppWidgetHost;
    private static final int REQUEST_CREATE_APPWIDGET = 900;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        widgets = new ArrayList<>();
        apps = new ArrayList<>();

        drawerGrid = (GridView) findViewById(R.id.content);
        slidingDrawer = (SlidingDrawer) findViewById(R.id.drawer);
        homeView = (RelativeLayout) findViewById(R.id.home_view);

        getWidgetIDs();
        getPackages();
        drawerAdapterObject = new DrawerAdapter(this, apps);
        drawerGrid.setAdapter(drawerAdapterObject);
        vibration = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);

        remove = (ImageView) findViewById(R.id.delete);
        remove.setVisibility(View.GONE);

        mAppWidgetManager = AppWidgetManager.getInstance(this);
        mAppWidgetHost = new LauncherAppWidgetHost(this, R.id.APPWIDGET_HOST_ID);

        setDrawerListeners();
        setReceiver();
        setTabs();
    }

    @Override
    public void onBackPressed() {
        if (slidingDrawer.isOpened()) {
            slidingDrawer.animateClose();
        } else {
            super.onBackPressed();
        }
    }


    void selectWidget() {
        int appWidgetId = this.mAppWidgetHost.allocateAppWidgetId();
        Intent pickIntent = new Intent(AppWidgetManager.ACTION_APPWIDGET_PICK);
        pickIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
        addEmptyData(pickIntent);
        startActivityForResult(pickIntent, R.id.REQUEST_PICK_APPWIDGET);
    }

    void addEmptyData(Intent pickIntent) {
        ArrayList customInfo = new ArrayList();
        pickIntent.putParcelableArrayListExtra(AppWidgetManager.EXTRA_CUSTOM_INFO, customInfo);
        ArrayList customExtras = new ArrayList();
        pickIntent.putParcelableArrayListExtra(AppWidgetManager.EXTRA_CUSTOM_EXTRAS, customExtras);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK ) {
            if (requestCode == R.id.REQUEST_PICK_APPWIDGET) {
                configureWidget(data);
            }
            else if (requestCode == REQUEST_CREATE_APPWIDGET) {
                createWidget(data);
            }
        }
        else if (resultCode == RESULT_CANCELED && data != null) {
            int appWidgetId = data.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, -1);
            if (appWidgetId != -1) {
                mAppWidgetHost.deleteAppWidgetId(appWidgetId);
            }
        }
    }

    private void configureWidget(Intent data) {
        Bundle extras = data.getExtras();
        int appWidgetId = extras.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID, -1);
        AppWidgetProviderInfo appWidgetInfo = mAppWidgetManager.getAppWidgetInfo(appWidgetId);
        if (appWidgetInfo.configure != null) {
            Intent intent = new Intent(AppWidgetManager.ACTION_APPWIDGET_CONFIGURE);
            intent.setComponent(appWidgetInfo.configure);
            intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
            startActivityForResult(intent, REQUEST_CREATE_APPWIDGET);
        } else {
            createWidget(data);
        }
    }

    public void createWidget(Intent data) {
        Bundle extras = data.getExtras();
        int appWidgetId = extras.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID, -1);
        AppWidgetProviderInfo appWidgetInfo = mAppWidgetManager.getAppWidgetInfo(appWidgetId);
        LauncherAppWidgetHostView hostView = (LauncherAppWidgetHostView) mAppWidgetHost.createView(this, appWidgetId, appWidgetInfo);
        hostView.setAppWidget(appWidgetId, appWidgetInfo);

        slidingDrawer.animateClose();
        homeView.addView(hostView);
        bringToBack(hostView);

    }

    public static void bringToBack(final View child) {
        final ViewGroup parent = (ViewGroup)child.getParent();
        if (parent != null) {
            parent.removeView(child);
            parent.addView(child, 0);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        mAppWidgetHost.startListening();
    }
    @Override
    protected void onStop() {
        super.onStop();
        mAppWidgetHost.stopListening();
    }

    public void removeWidget(LauncherAppWidgetHostView hostView) {
        mAppWidgetHost.deleteAppWidgetId(hostView.getAppWidgetId());
        homeView.removeView(hostView);
    }

    public void setTabs(){
        final Context mContext = getApplicationContext();
        Button appTab = (Button) findViewById(R.id.apps);
        Button widgetTab = (Button) findViewById(R.id.widgets);
        disableTabs(appTab, widgetTab);

        appTab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getPackages();
                drawerAdapterObject = new DrawerAdapter(mContext, apps);
                drawerGrid.setAdapter(drawerAdapterObject);
            }
        });

        widgetTab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //DrawerAdapter widgetAdapterObject = new DrawerAdapter(mContext, widgets);
                //drawerGrid.setAdapter(widgetAdapterObject);
                selectWidget();
            }
        });
    }

    public void setReceiver(){
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_PACKAGE_ADDED);
        filter.addAction(Intent.ACTION_PACKAGE_REMOVED);
        filter.addAction(Intent.ACTION_PACKAGE_CHANGED);
        filter.addDataScheme("package");
        registerReceiver(new Receiver(), filter);
    }

    public void getWidgetIDs(){
        AppWidgetManager manager = AppWidgetManager.getInstance(this);
        ArrayList<Integer> widgets = new ArrayList<>();
        int[] IDs;

        IDs = manager.getAppWidgetIds(new ComponentName(getApplicationContext(), AppWidgetProvider.class));
        for(int i=0; i < IDs.length;i++){
            //Toast.makeText(getBaseContext(),"IDS: " + IDs[i] ,Toast.LENGTH_LONG).show();
            widgets.add(IDs[i]);
        }
    }

    public void getPackages() {
        final Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
        mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);
        PackageManager pm = getPackageManager();
        List<ResolveInfo> packages = pm.queryIntentActivities(mainIntent, 0);

        String PACKAGE_NAME = getApplicationContext().getPackageName();
        apps.clear();

        for (int i = 0; i < packages.size(); i++) {
            Application p = new Application();
            p.icon = packages.get(i).loadIcon(pm);
            p.name = packages.get(i).activityInfo.packageName;
            p.label = packages.get(i).loadLabel(pm).toString();
            if(!(p.name.equals(PACKAGE_NAME)))
                apps.add(p);
        }
    }

    public RelativeLayout.LayoutParams createLayoutParams(View v, int x, int y){
        RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(v.getWidth(), v.getHeight());
        lp.leftMargin = x;
        lp.topMargin = y;
        return lp;
    }

    public void deleteShortcut(LinearLayout ll, RelativeLayout.LayoutParams lp, View v, boolean released){

        float xLowerBound = remove.getX() - remove.getWidth() / 2 - 150;
        float xUpperBound = remove.getX() - remove.getWidth() / 2 + 150;
        float yLowerBound = remove.getY() - remove.getWidth() / 2 - 150;
        float yUpperBound = remove.getY() - remove.getWidth() / 2 + 150;
        android.view.ViewGroup.LayoutParams layoutParams = remove.getLayoutParams();
        checkBounds(lp, v);

        //if app collides with delete icon
        if((lp.leftMargin > xLowerBound && lp.leftMargin < xUpperBound)
                && (lp.topMargin > yLowerBound && lp.topMargin < yUpperBound )){
            if(released)
                ll.removeAllViews();
            //if hovered but not released
            else{
                layoutParams.width = 250;
                layoutParams.height = 250;
                remove.setLayoutParams(layoutParams);
            }
        }
        //no collision
        else{
            layoutParams.width = 150;
            layoutParams.height = 150;
        }
    }

    //Prevents shortcuts from being dragged off screen
    public void checkBounds(RelativeLayout.LayoutParams lp, View v){
        View root = (View) v.getParent();
        int rootHeight = root.getHeight();
        int rootWidth = v.getRootView().getWidth();
        if (lp.leftMargin + v.getWidth() > rootWidth- 50)
            lp.leftMargin = rootWidth - v.getWidth() - 50;

        else if (lp.leftMargin < 50)
            lp.leftMargin = 50;

        else if (lp.topMargin + v.getHeight() > rootHeight - 100)
            lp.topMargin = rootHeight - v.getHeight() - 100;

        else if (lp.topMargin < 100)
            lp.topMargin = 100;
    }

    private void setHomeListeners(final LinearLayout ll, final int position){

        ll.setTag(apps.get(position).name);

        ll.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {

                int x = (int) event.getRawX() - v.getWidth() / 2;
                int y = (int) event.getRawY() - v.getHeight() / 2;
                RelativeLayout.LayoutParams lp = createLayoutParams(v, x, y);

                switch (event.getAction()){

                    case MotionEvent.ACTION_MOVE:
                        clickDuration = Calendar.getInstance().getTimeInMillis() - startClickTime;
                        checkBounds(lp, v);
                        if(clickDuration >= MIN_LONG_CLICK_DURATION) {

                            if(clickDuration < MIN_LONG_CLICK_DURATION + 75)
                                vibration.vibrate(25);

                            remove.setVisibility(View.VISIBLE);
                            v.setLayoutParams(lp);
                            deleteShortcut(ll, lp, v, false);
                        }
                        break;

                    //Start timer when view is touched
                    case MotionEvent.ACTION_DOWN:
                        startClickTime = Calendar.getInstance().getTimeInMillis();
                        break;

                    case MotionEvent.ACTION_UP:
                        clickDuration = Calendar.getInstance().getTimeInMillis() - startClickTime;
                        remove.setVisibility(View.GONE);

                        deleteShortcut(ll, lp, v, true);

                        if(clickDuration > MAX_CLICK_DURATION) {
                            //no click event
                            return true;
                        }
                        break;

                }
                return false;
            }
        });


        ll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PackageManager manager = getPackageManager();

                Intent i = manager.getLaunchIntentForPackage(v.getTag().toString());

                if (i != null) {
                    i.addCategory(Intent.CATEGORY_LAUNCHER);
                    startActivity(i);
                }
            }
        });
    }


    private void setDrawerListeners(){

        setSlidingListeners();
        //Active dragging mode when long click at each Grid view item
        drawerGrid.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView parent, View item, int position, long id) {
                MainActivity.appLaunchable=false;
                RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(item.getWidth(),item.getHeight());
                lp.leftMargin = (int) item.getX();
                lp.topMargin = (int) item.getY();

                vibration.vibrate(200);

                LayoutInflater li = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                LinearLayout ll = (LinearLayout) li.inflate(R.layout.drawer_item, null);

                ImageView img = ((ImageView)item.findViewById(R.id.icon_image));
                TextView txt = ((TextView)item.findViewById(R.id.icon_text));

                ((ImageView)ll.findViewById(R.id.icon_image)).setImageDrawable(img.getDrawable());
                ((TextView)ll.findViewById(R.id.icon_text)).setText(txt.getText());

                setHomeListeners(ll, position);

                homeView.addView(ll, lp);

                slidingDrawer.animateClose();
                slidingDrawer.bringToFront();
                return true;
            }
        });
        //Handling click event of each Grid view item
         drawerGrid.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView parent, View view, int position, long id) {

                PackageManager manager = getPackageManager();

                Intent i = manager.getLaunchIntentForPackage(apps.get(position).name);


                if (i != null) {
                    i.addCategory(Intent.CATEGORY_LAUNCHER);
                    startActivity(i);
                }
                else{
                    new AlertDialog.Builder(MainActivity.this)
                            .setTitle("Error")
                            .setMessage( apps.get(position).name + " could not open. Please try a different " +
                                    "app")
                            .setPositiveButton(android.R.string.yes, null)

                            .setIcon(apps.get(position).icon)
                            .show();
                }
            }
        });
    }

    public void setSlidingListeners(){

        final Button appTab = (Button) findViewById(R.id.apps);
        final Button widgetTab = (Button) findViewById(R.id.widgets);
        slidingDrawer.setOnDrawerScrollListener(new SlidingDrawer.OnDrawerScrollListener() {
            @Override
            public void onScrollStarted() {
                //disableTabs(apps, widgets);
            }

            @Override
            public void onScrollEnded() {
                if(slidingDrawer.isOpened() && (!(slidingDrawer.isMoving()) || slidingDrawer.isEnabled())){
                    enableTabs(appTab, widgetTab);
                }

            }
        });

        slidingDrawer.setOnDrawerOpenListener(new SlidingDrawer.OnDrawerOpenListener() {

            @Override
            public void onDrawerOpened() {
                enableTabs(appTab, widgetTab);
            }
        });

        slidingDrawer.setOnDrawerCloseListener(new SlidingDrawer.OnDrawerCloseListener() {

            @Override
            public void onDrawerClosed() {
                disableTabs(appTab, widgetTab);
            }
        });
    }

    public void disableTabs(Button appTab, Button widgetTab){
        appTab.setVisibility(View.GONE);
        widgetTab.setVisibility(View.GONE);
    }

    public void enableTabs(Button appTab, Button widgetTab){
        appTab.setVisibility(View.VISIBLE);
        widgetTab.setVisibility(View.VISIBLE);
    }

    public class Receiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            getPackages();
            drawerAdapterObject = new DrawerAdapter(MainActivity.this, apps);
            drawerGrid.setAdapter(drawerAdapterObject);
            setDrawerListeners();
        }

    }

}