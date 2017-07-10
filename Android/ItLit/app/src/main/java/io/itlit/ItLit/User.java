package io.itlit.ItLit;

import android.content.SharedPreferences;

public class User {
    public static String unull = "0000000000";
    public static String pnull = "0000000000000000000000000000000000000000000000000000000000000000";

    public static String  uname  = unull;
    public static String  passwd = pnull;
    public static boolean lit    = false;
    public static RoundedImageView rivSelfie;

    public static String selfiepng() { return User.uname + ".png"; }

    public static void setUser(String u, String p) {
        uname = u;
        passwd = p;
        lit = false;
    }

    public static void setUser(String u, String p, boolean litt) {
        uname = u;
        passwd = p;
        lit = litt;
    }

    public static void setLight(boolean b) {
        lit = b;
    }

    public static void rememberMe(SharedPreferences.Editor editor) {
        editor.putString("uname", uname);
        editor.putString("passwd", passwd);
        editor.commit();
    }

    public static void forgetMe(SharedPreferences.Editor editor) {
        editor.remove("uname");
        editor.remove("passwd");
        editor.commit();
    }
}
