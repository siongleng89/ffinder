package com.ffinder.android.tasks;

import android.content.Context;
import android.os.AsyncTask;
import android.widget.Toast;
import com.ffinder.android.helpers.FirebaseDB;
import com.ffinder.android.utils.Strings;
import com.google.android.gms.ads.identifier.AdvertisingIdClient;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.tagmanager.Container;

import java.io.IOException;

/**
 * Created by SiongLeng on 19/9/2016.
 */
public class AdsIdTask extends AsyncTask<Void, Void, String> {

    private Context context;
    private String saveToDbUserId;

    public AdsIdTask(Context context, String saveToDbUserId) {
        this.context = context;
        this.saveToDbUserId = saveToDbUserId;
    }

    @Override
    protected String doInBackground(Void... params) {
        AdvertisingIdClient.Info idInfo = null;
        try {
            idInfo = AdvertisingIdClient.getAdvertisingIdInfo(context);
        } catch (GooglePlayServicesNotAvailableException e) {
            e.printStackTrace();
        } catch (GooglePlayServicesRepairableException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        String advertId = null;
        try{
            advertId = idInfo.getId();
            if(!Strings.isEmpty(saveToDbUserId)){
                FirebaseDB.saveIdentifier(saveToDbUserId, advertId, null);
            }

        }catch (NullPointerException e){
            e.printStackTrace();
        }

        return advertId;
    }

    @Override
    protected void onPostExecute(String advertId) {
        super.onPostExecute(advertId);
    }
}
