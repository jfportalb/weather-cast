package br.com.ejcm.weathercast;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class Utility {

    public static String getPreferredLocation(Context context){
        SharedPreferences preferences =
                PreferenceManager.getDefaultSharedPreferences(context);
        return preferences.getString(
                context.getResources().getString(R.string.pref_location_key),
                context.getResources().getString(R.string.pref_default_location));
    }

    public static String getPreferredTemperatureFormat(Context context){
        SharedPreferences preferences =
                PreferenceManager.getDefaultSharedPreferences(context);
        return preferences.getString(
                context.getResources().getString(R.string.pref_format_key),
                context.getResources().getString(R.string.pref_default_format));

    }

}
