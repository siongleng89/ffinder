package com.ffinder.android.helpers;

import com.ffinder.android.absint.helpers.RestfulListener;
import com.ffinder.android.enums.Status;
import com.ffinder.android.statics.Constants;
import com.firebase.client.utilities.Pair;

import javax.net.ssl.HttpsURLConnection;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

/**
 * Created by SiongLeng on 8/9/2016.
 */
public class RestfulService {

    public static void getToken(String userId, RestfulListener<String> listener){
        ArrayList<Pair<String, String>> nameValuePairs = new ArrayList<Pair<String, String>>();
        nameValuePairs.add(new Pair<String, String>("userId", userId));
        callApi("login_user", nameValuePairs, listener);
    }

    public static void adsWatched(String userId, RestfulListener<String> listener){
        ArrayList<Pair<String, String>> nameValuePairs = new ArrayList<Pair<String, String>>();
        nameValuePairs.add(new Pair<String, String>("userId", userId));
        callApi("watched_ads", nameValuePairs, listener);
    }

    public static void usePromoCode(String userId, String promoCode, RestfulListener<String> listener){
        ArrayList<Pair<String, String>> nameValuePairs = new ArrayList<Pair<String, String>>();
        nameValuePairs.add(new Pair<String, String>("userId", userId));
        nameValuePairs.add(new Pair<String, String>("promoCode", promoCode));
        callApi("use_promo", nameValuePairs, listener);
    }

    public static void checkSubscriptionRemainingMs(String productId,
                                                   String token, RestfulListener<String> listener){

        ArrayList<Pair<String, String>> nameValuePairs = new ArrayList<Pair<String, String>>();
        nameValuePairs.add(new Pair<String, String>("productId", productId));
        nameValuePairs.add(new Pair<String, String>("token", token));
        callApi("subscription_check", nameValuePairs, listener);
    }

    private static void callApi(final String name,  final ArrayList<Pair<String, String>> nameValuePairs, final RestfulListener listener) {
        nameValuePairs.add(new Pair<String, String>("restSecret", Constants.RestfulKey));

        Threadings.runInBackground(new Runnable() {
            @Override
            public void run() {
                try {
                    String USER_AGENT = "Mozilla/5.0";
                    String url = Constants.RestfulUrl + name;
                    URL obj = new URL(url);

                    HttpURLConnection con;

                    if (url.startsWith("https")) {
                        con = (HttpsURLConnection) obj.openConnection();
                    } else {
                        con = (HttpURLConnection) obj.openConnection();
                    }

                    //add reuqest header
                    con.setRequestMethod("POST");
                    con.setRequestProperty("User-Agent", USER_AGENT);
                    con.setRequestProperty("Accept-Language", "en-US,en;q=0.5");

                    String urlParameters = "";

                    for (Pair<String, String> nameValuePair : nameValuePairs) {
                        urlParameters += nameValuePair.getFirst() + "=" + nameValuePair.getSecond() + "&";
                    }

                    if (nameValuePairs.size() > 0) {
                        urlParameters = urlParameters.substring(0, urlParameters.length() - 1);
                    }

                    // Send post request
                    con.setDoOutput(true);
                    DataOutputStream wr = new DataOutputStream(con.getOutputStream());
                    wr.writeBytes(urlParameters);
                    wr.flush();
                    wr.close();

                    int responseCode = con.getResponseCode();
                    Logs.show("\nSending 'POST' request to URL : " + url);
                    Logs.show("Post parameters : " + urlParameters);
                    Logs.show("Response Code : " + responseCode);

                    BufferedReader in = new BufferedReader(
                            new InputStreamReader(con.getInputStream()));
                    String inputLine;
                    StringBuffer response = new StringBuffer();

                    while ((inputLine = in.readLine()) != null) {
                        response.append(inputLine);
                    }
                    in.close();

                    //print result

                    String responseText = response.toString();
                    if (listener != null) listener.onResult(responseText, Status.Success);

                } catch (Exception e) {
                    e.printStackTrace();
                    if (listener != null) listener.onResult(null, Status.Failed);
                }
            }
        });

    }
}
