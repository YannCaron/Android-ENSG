package eu.ensg.forester.data;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.List;

/**
 * Created by cyann on 20/02/16.
 */
public class ListAdapter extends BaseAdapter {

    private final Context context;
    private final List<String> data;
    private final int columnCount;

    public ListAdapter(Context context, List<String> data, int columnCount) {
        this.context = context;
        this.data = data;
        this.columnCount = columnCount;
    }

    @Override
    public int getCount() {
        return data.size();
    }

    @Override
    public Object getItem(int position) {
        return data.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position / columnCount;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        TextView view;
        if (convertView == null) {
             view = new TextView(context);
        } else {
            view = (TextView)convertView;
        }

        view.setText(data.get(position));
        return view;
    }
}
