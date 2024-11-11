package com.thewhitewings.shaky;

import android.net.Uri;
import android.os.Build;

/**
 * Utility class containing methods used throughout the app
 */
public class Util {

    /**
     * Get the specifications of the device that is running the app
     *
     * @return the device's manufacturer, version name, and release number
     */
    public static String getDeviceSpecs() {
        String manufacturer = android.os.Build.MANUFACTURER.toUpperCase();
        String versionName = getVersionName();
        String versionRelease = Build.VERSION.RELEASE;
        return manufacturer + " • Android " + versionName + " " + versionRelease;
    }

    /**
     * Get the version name that corresponds to the version release number of the device
     */
    private static String getVersionName() {
        String versionRelease = Build.VERSION.RELEASE;

        switch (versionRelease) {
            case "5.0":
                return "Lollipop";
            case "5.1":
                return "Lollipop MR1";
            case "6.0":
                return "Marshmallow";
            case "7.0":
                return "Nougat";
            case "7.1":
                return "Nougat MR1";
            case "8.0":
                return "Oreo";
            case "8.1":
                return "Oreo MR1";
            case "9":
                return "Pie";
            case "10":
                return "Q";
            case "11":
                return "R";
            case "12":
                return "S";
            case "13":
                return "Tiramisu";
            case "14":
                return "Upside Down Cake";
            case "15":
                return "Vanilla Ice Cream";
            default:
                return "Android Version";
        }
    }

    /**
     * Get the website of the first guide
     * of handling app restrictions caused by system's battery optimization.
     *
     * @return the URI of the first guide
     */
    public static Uri getBatteryOptimizationGuideUri1() {
        // Get the manufacturer of the device to be added to the Url
        String manufacturer = Build.MANUFACTURER.toLowerCase();

        // Replace the special letter 'ı' with 'i'
        // The uppercase 'I' is interpreted in the Turkish language as 'ı' not 'i',
        // and this causes issues especially when the string is used in Url
        manufacturer = manufacturer.replace('ı', 'i');

        // Direct to the guide that is specified to the user's device
        String websiteUrl = "https://dontkillmyapp.com/" + manufacturer;

        return Uri.parse(websiteUrl);
    }

    /**
     * Get the website of the second guide
     * of handling app restrictions caused by system's battery optimization.
     *
     * @return the URI of the second guide
     */
    public static Uri getBatteryOptimizationGuideUri2() {
        String websiteUrl = "https://support.bark.us/hc/en-us/articles/11484413158669#find-your-kid-s-android-version-0";
        return Uri.parse(websiteUrl);
    }
}