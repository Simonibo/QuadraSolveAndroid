package de.jamesbeans.quadrasolve;

import android.app.Application;
import android.content.SharedPreferences;

import java.util.Locale;

/**
 * Saves the device locale on applicaiton startup
 * Created by Simon on 18.08.2017.
 */

public class QuadraSolve extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        LocaleHelper.devicedefault = Locale.getDefault();
        final SharedPreferences sd = getSharedPreferences("settings", 0);
        final SharedPreferences.Editor ed = sd.edit();
        final int to = sd.getInt("timesopened", 0);
        if(to == 1) {
            MainActivity.offerLanguageChange = true;
        }
        ed.putInt("timesopened", to + 1);
        ed.apply();
    }
}
