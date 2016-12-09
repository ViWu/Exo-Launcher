package myapplication.applauncher;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

public class DrawerAdapter extends BaseAdapter{
    Context mContext;
    protected static ArrayList<Application> apps = new ArrayList<>();

    public DrawerAdapter (Context c, ArrayList<Application> arr){
        mContext =c;
        apps = arr;
    }

    @Override
    public int getCount() {
        // TODO Auto-generated method stub
        return apps.size();
    }

    @Override
    public Object getItem(int arg0) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public long getItemId(int arg0) {
        // TODO Auto-generated method stub
        return 0;
    }

    static class ViewHolder{
        TextView text;
        ImageView icon;
    }
    @Override
    public View getView(int pos, View convertView, ViewGroup arg2) {
        // TODO Auto-generated method stub
        ViewHolder viewHolder;
        LayoutInflater li = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        if (convertView==null){
            convertView = li.inflate(R.layout.drawer_item, null);

            viewHolder = new ViewHolder();
            viewHolder.text= (TextView)convertView.findViewById(R.id.icon_text);
            viewHolder.icon= (ImageView)convertView.findViewById(R.id.icon_image);

            convertView.setTag(viewHolder);
        }
        else
            viewHolder = (ViewHolder) convertView.getTag();

        viewHolder.text.setText(apps.get(pos).label);
        viewHolder.icon.setImageDrawable(apps.get(pos).icon);

        return convertView;
    }

}