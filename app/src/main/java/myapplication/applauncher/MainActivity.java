package myapplication.applauncher;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Bundle;
import android.widget.GridView;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends Activity {

    DrawerAdapter drawerAdapterObject;
    GridView drawerGrid;



    PackageManager pm;

    protected static ArrayList<Application> apps;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        apps = new ArrayList<>();

        drawerGrid = (GridView) findViewById(R.id.content);
        pm = getPackageManager();
        getPackages();
        drawerAdapterObject = new DrawerAdapter(this, apps);
        drawerGrid.setAdapter(drawerAdapterObject);

        
    }

    public void getPackages() {
        final Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
        mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);
        List<ResolveInfo> packages = pm.queryIntentActivities(mainIntent, 0);
        for (int I = 0; I < packages.size(); I++) {
            Application p = new Application();
            p.icon = packages.get(I).loadIcon(pm);
            p.name = packages.get(I).activityInfo.packageName;
            p.label = packages.get(I).loadLabel(pm).toString();
            apps.add(p);
        }

    }





}