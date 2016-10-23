package com.ffinder.android.adapters;

import android.content.Context;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import com.ffinder.android.R;
import com.ffinder.android.models.MyModel;
import com.ffinder.android.models.SettingsModel;

import java.util.List;

/**
 * Created by SiongLeng on 8/9/2016.
 */
public class SettingsAdapter extends ArrayAdapter<SettingsModel> {

    private Context context;
    private List<SettingsModel> settingsModels;
    private MyModel myModel;

    public SettingsAdapter(Context context, @LayoutRes int resource, @NonNull List<SettingsModel> objects,
                            MyModel myModel) {
        super(context, resource, objects);
        this.context = context;
        this.settingsModels = objects;
        this.myModel = myModel;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        ViewHolder mViewHolder = null;
        //HashMap<String, String> song = null;

        if (convertView == null) {
            //  song = new HashMap <String, String>();
            mViewHolder = new ViewHolder();
            LayoutInflater vi = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = vi.inflate(R.layout.lvitem_single_text, parent, false);

            mViewHolder.textView = (TextView) convertView.findViewById(R.id.textView);

            convertView.setTag(mViewHolder);

            setListeners(convertView, position);

        }
        updateDesign(convertView, settingsModels.get(position));

        return convertView;
    }

    private void updateDesign(View convertView, SettingsModel settingsModel){
        ViewHolder viewHolder = (ViewHolder) convertView.getTag();
        viewHolder.textView.setText(settingsModel.getTitle());
    }


    private void setListeners(final View convertView, final int position){
        final ViewHolder viewHolder = (ViewHolder) convertView.getTag();


    }

    static class ViewHolder {
        private TextView textView;
    }

}
