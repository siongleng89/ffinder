package com.ffinder.android.helpers;

import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import com.ffinder.android.models.UserInfoModel;

import java.util.Locale;

/**
 * Created by SiongLeng on 8/9/2016.
 */
public class LocaleHelper {

    private static String currentLang;

    public static void onCreate(Context context) {
        String lang = getPersistedData(context, Locale.getDefault().getLanguage());
        if(currentLang == null || !currentLang.equals(lang)){
            setLocale(context, lang);
            currentLang = lang;
        }
    }

    public static void onCreate(Context context, String defaultLanguage) {
        String lang = getPersistedData(context, defaultLanguage);
        setLocale(context, lang);
    }

    public static String getLanguage(Context context) {
        return getPersistedData(context, Locale.getDefault().getLanguage());
    }

    public static void setLocale(Context context, String language) {
        persist(context, language);
        updateResources(context, language);
    }

    private static String getPersistedData(Context context, String defaultLanguage) {
        UserInfoModel userInfoModel = new UserInfoModel();
        userInfoModel.load(context);
        if(!Strings.isEmpty(userInfoModel.getLanguage())){
            return userInfoModel.getLanguage();
        }
        else{
            return defaultLanguage;
        }
    }

    public static void persist(Context context, String language) {
        UserInfoModel userInfoModel = new UserInfoModel();
        userInfoModel.load(context);
        userInfoModel.setLanguage(language);
        userInfoModel.save(context);
        currentLang = null;
    }

    private static void updateResources(Context context, String language) {
        Locale locale = new Locale(language);
        Locale.setDefault(locale);

        Resources resources = context.getResources();

        Configuration configuration = resources.getConfiguration();
        configuration.locale = locale;

        resources.updateConfiguration(configuration, resources.getDisplayMetrics());
    }
}
