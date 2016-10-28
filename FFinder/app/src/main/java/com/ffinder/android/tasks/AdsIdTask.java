package com.ffinder.android.tasks;

import android.content.Context;
import android.os.AsyncTask;
import com.ffinder.android.helpers.FirebaseDB;
import com.ffinder.android.helpers.Strings;
import com.google.android.gms.ads.identifier.AdvertisingIdClient;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;

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

        try{
            AdvertisingIdClient.Info adInfo = AdvertisingIdClient.getAdvertisingIdInfo(context);
            String adId = adInfo != null ? adInfo.getId() : null;
            // Use the advertising id
            if(!Strings.isEmpty(saveToDbUserId) && !Strings.isEmpty(adId)){
                FirebaseDB.saveIdentifier(saveToDbUserId, adId, null);
            }
            return adId;

        } catch (IOException
                | GooglePlayServicesRepairableException
                | GooglePlayServicesNotAvailableException exception) {
            // Error handling if needed
        }

        return null;
    }

    @Override
    protected void onPostExecute(String advertId) {
        super.onPostExecute(advertId);
    }
}
