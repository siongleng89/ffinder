package com.ffinder.android.helpers;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Build;
import com.ffinder.android.models.UserInfoModel;

import java.util.Locale;

/**
 * Created by SiongLeng on 8/9/2016.
 */
public class LocaleHelper {

    public static void onCreate(Context context) {
        String lang = getPersistedData(context, getSystemCurrentLocale().getLanguage());
        setLocale(context, lang);
    }

    public static String getLanguage(Context context) {
        return getPersistedData(context, Locale.getDefault().getLanguage());
    }

    public static void setLocale(Context context, String language) {
        updateResources(context, language);
    }

    private static String getPersistedData(Context context, String defaultLanguage) {
        UserInfoModel userInfoModel = new UserInfoModel();
        userInfoModel.load(context);

        boolean noLanguageSet = Strings.isEmpty(userInfoModel.getLanguage());

        if(noLanguageSet){
            return defaultLanguage;
        }
        else{
            return userInfoModel.getLanguage();
        }
    }

    public static void persist(Context context, String language) {
        UserInfoModel userInfoModel = new UserInfoModel();
        userInfoModel.load(context);

        Logs.show(getSystemCurrentLocale().getLanguage());
        if(getSystemCurrentLocale().getLanguage().equals(language)){
            userInfoModel.setLanguage("");
        }
        else{
            userInfoModel.setLanguage(language);
        }


        userInfoModel.save(context);

    }

    private static void updateResources(Context context, String language) {
        Locale locale = new Locale(language);
        Locale.setDefault(locale);

        Resources resources = context.getResources();

        Configuration configuration = resources.getConfiguration();
        configuration.locale = locale;

        resources.updateConfiguration(configuration, resources.getDisplayMetrics());
    }


    @TargetApi(Build.VERSION_CODES.N)
    public static Locale getSystemCurrentLocale(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N){
            return Resources.getSystem().getConfiguration().getLocales().get(0);
        } else{
            //noinspection deprecation
            return Resources.getSystem().getConfiguration().locale;
        }
    }

}
