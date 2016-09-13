package com.ffinder.android.utils;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.location.Address;
import android.location.Geocoder;
import android.support.design.widget.TextInputLayout;
import android.util.Patterns;
import android.widget.EditText;
import com.ffinder.android.R;
import com.ffinder.android.models.GoogleGeoCodeResponse;
import com.ffinder.android.statics.Vars;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

/**
 * Created by SiongLeng on 1/9/2016.
 */
public class AndroidUtils {

    public static boolean validateEditText(EditText editText, TextInputLayout textInputLayout, String emptyMsg){
        if(Strings.isEmpty(editText.getText().toString())){
            textInputLayout.setError(emptyMsg);
            textInputLayout.setErrorEnabled(true);
            return false;
        }
        else{
            textInputLayout.setError("");
            textInputLayout.setErrorEnabled(false);
            return true;
        }
    }

    public static ProgressDialog loading(String text, Context context, final Runnable onDismiss){
        ProgressDialog pd = new ProgressDialog(context);
        pd.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                if(onDismiss != null) onDismiss.run();
            }
        });
        pd.setMessage(text);
        pd.show();
        return pd;
    }

    public static String getUsername(Context context){
        String name = getUsername1(context);

        if(Strings.isEmpty(name)){
            name = getUsername2(context);
        }

        return name;
    }

    private static String getUsername1(Context context){
        AccountManager manager = AccountManager.get(context);
        Account[] accounts = manager.getAccountsByType("com.google");
        List<String> possibleEmails = new LinkedList<String>();

        for (Account account : accounts) {
            possibleEmails.add(account.name);
        }

        if (!possibleEmails.isEmpty() && possibleEmails.get(0) != null) {
            String email = possibleEmails.get(0);
            String[] parts = email.split("@");
            if (parts.length > 0 && parts[0] != null)
                return parts[0];
            else
                return "";
        } else
            return "";
    }

    private static String getUsername2(Context context){
        Pattern emailPattern = Patterns.EMAIL_ADDRESS; // API level 8+
        Account[] accounts = AccountManager.get(context).getAccounts();
        for (Account account : accounts) {
            if (emailPattern.matcher(account.name).matches()) {
                String possibleEmail = account.name;
                String[] parts = possibleEmail.split("@");
                if (parts.length > 0 && parts[0] != null)
                    return parts[0];
                else
                    return "";
            }
        }
        return "";
    }


    public static void geoDecode(final Context context, final String latitude, final String longitude, final RunnableArgs<String> onFinish){
        geoDecodeByGeocoder(context, latitude, longitude, new RunnableArgs<String>() {
            @Override
            public void run() {
                if(Strings.isEmpty(this.getFirstArg())){
                    geoDecodeByGoogleApi(latitude, longitude, new RunnableArgs<String>() {
                        @Override
                        public void run() {
                            if(Strings.isEmpty(this.getFirstArg())){
                                onFinish.run(context.getString(R.string.unknown_address_msg));
                            }
                            else{
                                onFinish.run(this.getFirstArg());
                            }
                        }
                    });
                }
                else{
                    onFinish.run(this.getFirstArg());
                }
            }
        });
    }



    private static void geoDecodeByGeocoder(final Context context, final String latitude, final String longitude, final RunnableArgs<String> onFinish){
        Threadings.runInBackground(new Runnable() {
            @Override
            public void run() {
                if(!Geocoder.isPresent()){
                    onFinish.run(null);
                    return;
                }

                Geocoder geocoder = new Geocoder(context, Locale.getDefault());

                Logs.show("Geodecoding location using geocoder: " + latitude + ", " + longitude);

                List<Address> addresses = null;

                try {
                    addresses = geocoder.getFromLocation(
                            Double.valueOf(latitude),
                            Double.valueOf(longitude),
                            1);
                } catch (IOException ioException) {
                    // Catch network or other I/O problems.

                } catch (IllegalArgumentException illegalArgumentException) {
                    // Catch invalid latitude or longitude values.
                }

                String finalAddress = null;

                // Handle case where no address was found.
                if (addresses == null || addresses.size()  == 0) {

                } else {
                    Address address = addresses.get(0);
                    ArrayList<String> addressFragments = new ArrayList<String>();

                    for (int i = 0; i < address.getMaxAddressLineIndex(); i++) {
                        addressFragments.add(address.getAddressLine(i));
                    }

                    if(!Strings.isEmpty(address.getCountryName())){
                        addressFragments.add(address.getCountryName());
                    }

                    finalAddress = Strings.joinArr(addressFragments, ", ");

                    Logs.show("Geodecoding success: " + finalAddress);
                }

                onFinish.run(finalAddress);
            }
        });
    }

    private static void geoDecodeByGoogleApi(final String latitude, final String longitude, final RunnableArgs<String> onFinish){

        Threadings.runInBackground(new Runnable() {
            @Override
            public void run() {
                String address = null;
                String url = "http://maps.googleapis.com/maps/api/geocode/json?latlng=" + latitude + ","
                        + longitude + "&sensor=true";

                try {
                    GoogleGeoCodeResponse response = Vars.getObjectMapper().readValue(new URL(url), GoogleGeoCodeResponse.class);
                    if(response.status.equalsIgnoreCase("ok")){
                        address = response.getFirstAddress();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }

                onFinish.run(address);

            }
        });

    }

}
