package ub.edu.pis2014.pis12.utils;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.DisplayMetrics;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import ub.edu.pis2014.pis12.R;

public class Utils {

    // Whether there is a Wi-Fi connection.
    private static boolean wifiConnected = false;
    // Whether there is a mobile connection.
    private static boolean mobileConnected = false;

    /**
     * Anomenem expandedMode al mode de vista per tablets, on mes d'un fragment
     * es mostra en una sola activity.
     *
     * @param activityContext Context de l'aplicacio
     * @return True si ho estem
     */
    public static boolean isExpandedMode(Context activityContext) {
        return inLandscape(activityContext) && isTabletDevice(activityContext);
    }

    /**
     * Comproba si esta en mode horitzontal
     *
     * @param activityContext Context de l'activity
     * @return Retorna true si ho esta
     */
    public static boolean inLandscape(Context activityContext) {
        return activityContext.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE;
    }

    /**
     * Comproba si el dispositiu es un mobil o una tablet
     *
     * @param activityContext The Activity Context.
     * @return Returns true if the device is a Tablet
     */
    @TargetApi(Build.VERSION_CODES.GINGERBREAD)
    public static boolean isTabletDevice(Context activityContext) {
        // Verifies if the Generalized Size of the device is XLARGE to be
        // considered a Tablet
        boolean xlarge = ((activityContext.getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK) == Configuration.SCREENLAYOUT_SIZE_XLARGE);

        // If XLarge, checks if the Generalized Density is at least MDPI
        // (160dpi)
        if (xlarge) {
            DisplayMetrics metrics = new DisplayMetrics();
            Activity activity = (Activity) activityContext;
            activity.getWindowManager().getDefaultDisplay().getMetrics(metrics);

            // MDPI=160, DEFAULT=160, DENSITY_HIGH=240, DENSITY_MEDIUM=160,
            // DENSITY_TV=213, DENSITY_XHIGH=320
            if (metrics.densityDpi == DisplayMetrics.DENSITY_DEFAULT
                    || metrics.densityDpi == DisplayMetrics.DENSITY_HIGH
                    || metrics.densityDpi == DisplayMetrics.DENSITY_MEDIUM
                    || metrics.densityDpi == DisplayMetrics.DENSITY_TV
                    || metrics.densityDpi == DisplayMetrics.DENSITY_XHIGH) {

                // Yes, this is a tablet!
                return true;
            }
        }

        // No, this is not a tablet!
        return false;
    }

    public static void loadFragment(FragmentActivity activity, int container,
                                    Fragment newFragment) {
        FragmentManager manager = activity.getSupportFragmentManager();
        Fragment fragment = manager.findFragmentById(R.id.main_left_fragment);

        if (!fragment.getClass().equals(newFragment)) {
            // Execute a transaction, replacing any existing fragment
            // with this one inside the frame.
            FragmentTransaction ft = manager.beginTransaction();
            ft.replace(R.id.main_left_fragment, newFragment);

            ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
            ft.commit();
        }
    }

    public static int getIdioma() {
        String[] llenguatges = {"ca", "es", "zh", "en"};
        for (int i = 0; i < llenguatges.length; i++) {
            if (Locale.getDefault().getLanguage().equals(llenguatges[i]))
                return i;
        }
        return 0;
    }

    public static boolean checkEmail(String email) {

        Pattern pattern = Pattern.compile("[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,4}");
        Matcher mat = pattern.matcher(email);

        if (mat.matches()) {
            return true;
        } else {

            return false;
        }
    }

    /**
     * For copy files
     *
     * @param in
     * @param out
     * @throws IOException
     */
    public static void copyFile(InputStream in, OutputStream out) throws IOException {
        byte[] buffer = new byte[1024];
        int read;
        while ((read = in.read(buffer)) != -1) {
            out.write(buffer, 0, read);
        }
    }

    /**
     * Check whether the device is connected, and if so, whether the connection
     * is wifi or mobile (it could be something else).
     */
    private static void checkNetworkConnection(ConnectivityManager conectManager) {
        NetworkInfo activeInfo = conectManager.getActiveNetworkInfo();
        if (activeInfo != null && activeInfo.isConnected()) {
            wifiConnected = activeInfo.getType() == ConnectivityManager.TYPE_WIFI;
            mobileConnected = activeInfo.getType() == ConnectivityManager.TYPE_MOBILE;
//          if(wifiConnected) {
//              System.out.println("Conectat al wifi");
//          } else if (mobileConnected){
//        	  System.out.println("Conectat a red mòvil");
//          }
        } else {
//    	  System.out.println("No hi ha conexió mòvil ni wifi");
        }
    }

    public static boolean checkNetworkConnectionState(ConnectivityManager conectManager) {
        checkNetworkConnection(conectManager);
        if (wifiConnected || mobileConnected) {
            return true;
        }
        return false;
    }

    /**
     * Check if this device has a camera
     */
    public static boolean checkCameraHardware(Context context) {
        if (context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA)) {
            // this device has a camera
            return true;
        } else {
            // no camera on this device
            return false;
        }
    }
}
