package de.jamesbeans.quadrasolve;

import android.app.Application;

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
    }
}
