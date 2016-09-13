package com.ffinder.android.adapters;

import android.content.Context;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import com.ffinder.android.R;
import com.ffinder.android.models.LanguageModel;
import com.ffinder.android.models.MyModel;

import java.util.List;

/**
 * Created by SiongLeng on 8/9/2016.
 */
public class LanguagesAdapter extends ArrayAdapter<LanguageModel>{

    private Context context;
    private List<LanguageModel> languageModels;
    private MyModel myModel;

    public LanguagesAdapter(Context context, @LayoutRes int resource, @NonNull List<LanguageModel> objects,
                          MyModel myModel) {
        super(context, resource, objects);
        this.context = context;
        this.languageModels = objects;
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
            convertView = vi.inflate(R.layout.language_item, parent, false);

            mViewHolder.txtLanguage = (TextView) convertView.findViewById(R.id.txtLanguage);
            mViewHolder.radioLanguage = (RadioButton) convertView.findViewById(R.id.radioLanguage);

            convertView.setTag(mViewHolder);

            setListeners(convertView, position);

        }
        updateDesign(convertView, languageModels.get(position));

        return convertView;
    }

    private void updateDesign(View convertView, LanguageModel languageModel){
        ViewHolder viewHolder = (ViewHolder) convertView.getTag();
        viewHolder.txtLanguage.setText(languageModel.getName());

        viewHolder.radioLanguage.setChecked(languageModel.isSelected());
    }


    private void setListeners(final View convertView, final int position){
        final ViewHolder viewHolder = (ViewHolder) convertView.getTag();

        viewHolder.radioLanguage.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

            }
        });
    }

    static class ViewHolder {
        private TextView txtLanguage;
        private RadioButton radioLanguage;
    }

}
