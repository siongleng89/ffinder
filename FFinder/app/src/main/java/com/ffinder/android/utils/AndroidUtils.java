package com.ffinder.android.utils;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.location.Address;
import android.location.Geocoder;
import android.os.Build;
import android.support.design.widget.TextInputLayout;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.util.DisplayMetrics;
import android.util.Patterns;
import android.view.Display;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import com.ffinder.android.R;
import com.ffinder.android.enums.AlertDialogType;
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

    public static float getScreenDpHeight(Activity activity){
        Display display = activity.getWindowManager().getDefaultDisplay();
        DisplayMetrics outMetrics = new DisplayMetrics ();
        display.getMetrics(outMetrics);

        float density  = activity.getResources().getDisplayMetrics().density;
        float dpHeight = outMetrics.heightPixels / density;
        float dpWidth  = outMetrics.widthPixels / density;
        return dpHeight;
    }

    public static float getScreenDpWidth(Activity activity){
        Display display = activity.getWindowManager().getDefaultDisplay();
        DisplayMetrics outMetrics = new DisplayMetrics ();
        display.getMetrics(outMetrics);

        float density  = activity.getResources().getDisplayMetrics().density;
        float dpHeight = outMetrics.heightPixels / density;
        float dpWidth  = outMetrics.widthPixels / density;
        return dpWidth;
    }

    public static int pxToDp(Context context, int px) {
        DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
        int dp = Math.round(px / (displayMetrics.xdpi / DisplayMetrics.DENSITY_DEFAULT));
        return dp;
    }

    public static int dpToPx(Context context, int dp) {
        DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
        int px = Math.round(dp * (displayMetrics.xdpi / DisplayMetrics.DENSITY_DEFAULT));
        return px;
    }

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

    public static AlertDialog showDialog(Context context, String title, String content, final Runnable onPositivePress,
                                                                final Runnable onDismiss){
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        if(!Strings.isEmpty(title)) builder.setTitle(title);
        builder.setMessage(content);
        // Set up the buttons
        builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if(onPositivePress != null) onPositivePress.run();
                else{
                    dialog.dismiss();
                }
            }
        });

        builder.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialogInterface) {
                if(onDismiss != null) onDismiss.run();
            }
        });

        return builder.show();
    }

    public static AlertDialog showDialogWithButtonText(Context context, String title, String content, String buttonText,
                                                       final RunnableArgs<DialogInterface> onPositivePress, final Runnable onDismiss){
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        if(!Strings.isEmpty(title)) builder.setTitle(title);
        builder.setMessage(content);
        // Set up the buttons
        builder.setPositiveButton(buttonText, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if(onPositivePress != null) onPositivePress.run(dialog);
                else{
                    dialog.dismiss();
                }
            }
        });

        builder.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialogInterface) {
                if(onDismiss != null) onDismiss.run();
            }
        });

        return builder.show();
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

    public static void setButtonBackground(Context context, View button, int drawable){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            button.setBackground( ContextCompat.getDrawable(context, drawable));
        }
        else{
            button.setBackgroundDrawable( ContextCompat.getDrawable(context, drawable) );
        }
    }


    public static void geoDecode(final Context context, final String latitude, final String longitude, final RunnableArgs<String> onFinish){
        Threadings.runInBackground(new Runnable() {
            @Override
            public void run() {
                final ArrayList<String> addressResult = new ArrayList<String>();
                int sleepCount = 0;

                geoDecodeByGeocoder(context, latitude, longitude, new RunnableArgs<String>() {
                    @Override
                    public void run() {
                        if(Strings.isEmpty(this.getFirstArg())){
                            addressResult.add("");
                        }
                        else{
                            addressResult.add(this.getFirstArg());
                        }
                    }
                });

                geoDecodeByGoogleApi(latitude, longitude, new RunnableArgs<String>() {
                    @Override
                    public void run() {
                        if(Strings.isEmpty(this.getFirstArg())){
                            addressResult.add("");
                        }
                        else{
                            addressResult.add(this.getFirstArg());
                        }
                    }
                });


                while (addressResult.size() < 2){
                    Threadings.sleep(300);
                    sleepCount++;
                    if(sleepCount > 40){
                        break;
                    }
                }

                String finalAddress = "";
                for(String address : addressResult){
                    if(address.length() > finalAddress.length()){
                        finalAddress = address;
                    }
                }

                if(Strings.isEmpty(finalAddress)){
                    finalAddress = context.getString(R.string.unknown_address_msg);
                }

                onFinish.run(finalAddress);

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

                Logs.show("Geodecoding location using google api: " + latitude + ", " + longitude);

                String address = null;
                String url = "http://maps.googleapis.com/maps/api/geocode/json?latlng=" + latitude + ","
                        + longitude + "&sensor=true";

                try {
                    GoogleGeoCodeResponse response = Vars.getObjectMapper().readValue(new URL(url), GoogleGeoCodeResponse.class);
                    if(response.status.equalsIgnoreCase("ok")){
                        address = response.getFirstAddress();
                    }

                    Logs.show("Geodecoding result: " + address);

                } catch (IOException e) {
                    e.printStackTrace();
                }

                onFinish.run(address);

            }
        });

    }





}
