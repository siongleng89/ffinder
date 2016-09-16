package com.ffinder.android.adapters;

import android.content.Context;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.v4.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import com.ffinder.android.R;
import com.ffinder.android.models.MyModel;
import com.ffinder.android.models.SubscriptionModel;

import java.util.List;

/**
 * Created by SiongLeng on 15/9/2016.
 */
public class VipAdapter extends ArrayAdapter<SubscriptionModel> {

    private Context context;
    private List<SubscriptionModel> vipItems;

    public VipAdapter(Context context, @LayoutRes int resource, @NonNull List<SubscriptionModel> objects) {
        super(context, resource, objects);
        this.vipItems = objects;
        this.context = context;
    }


    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        ViewHolder mViewHolder = null;
        //HashMap<String, String> song = null;

        if (convertView == null) {
            //  song = new HashMap <String, String>();
            mViewHolder = new ViewHolder();
            LayoutInflater vi = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = vi.inflate(R.layout.lvitem_vip, parent, false);

            mViewHolder.txtBig = (TextView) convertView.findViewById(R.id.txtBig);
            mViewHolder.txtSmall = (TextView) convertView.findViewById(R.id.txtSmall);

            convertView.setTag(mViewHolder);

        }
        updateDesign(convertView, vipItems.get(position));

        return convertView;
    }

    private void updateDesign(View view, SubscriptionModel subscriptionModel){
        ViewHolder mViewHolder = (ViewHolder) view.getTag();
        mViewHolder.txtBig.setText(subscriptionModel.getTitle());
        mViewHolder.txtSmall.setText(subscriptionModel.getContent());
    }


    static class ViewHolder {
        private TextView txtBig, txtSmall;
    }


}
