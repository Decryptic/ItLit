package io.itlit.ItLit;

import java.security.MessageDigest;
import java.util.HashMap;
import java.util.ArrayList;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.location.Location;

import com.google.android.gms.maps.GoogleMap;

public class Const {
    public static String ptal = "Please try again later";
    public static String userprefs = "io.itlit.ItLit.USER";
    public static String selfiepng() { return uname + ".png"; }
    public static String server(String ep) {
        return "https://www.itlit.io/" + ep;
    }
    public static RoundedImageView rivSelfie;
    public static GoogleMap googleMap;

    public static int activateAttempts = 0;

    public static Location lastKnown; // The last known location of the user
    private static String nullHash = "0000000000000000000000000000000000000000000000000000000000000000";
    public static String uname = "0000000000";
    public static String passwd = nullHash;
    public static Boolean lit = false;

    // These are for AddActivity when opened by "Edit"
    public static String ecName;
    public static String ecFname;
    public static Integer ecOldIndex;

    public static Bitmap nullpic;

    public static String phonify(String number) {
        String result = "";
        for (char c : number.toCharArray()) {
            if ("0123456789".contains(Character.toString(c))) {
                result += c;
            }
        }
        if (result.charAt(0) == '1') {
            result = result.substring(1);
        }
        return result;
    }

    public static String sha256(String data) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            md.update(data.getBytes());
            return bytesToHex(md.digest());
        }
        catch (Exception e) {
            return nullHash;
        }
    }
    private static String bytesToHex(byte[] bytes) {
        StringBuffer result = new StringBuffer();
        for (byte byt : bytes) result.append(Integer.toString((byt & 0xff) + 0x100, 16).substring(1));
        return result.toString();
    }

    // This turns a square image into a circular one
    public static Bitmap getCroppedBitmap(Bitmap bmp, int radius) {
        Bitmap sbmp;

        if (bmp.getWidth() != radius || bmp.getHeight() != radius) {
            float smallest = Math.min(bmp.getWidth(), bmp.getHeight());
            float factor = smallest / radius;
            sbmp = Bitmap.createScaledBitmap(bmp, (int) (bmp.getWidth() / factor), (int) (bmp.getHeight() / factor), false);
        } else {
            sbmp = bmp;
        }

        Bitmap output = Bitmap.createBitmap(radius, radius,
                Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(output);

        Paint paint = new Paint();
        Rect rect = new Rect(0, 0, radius, radius);
        paint.setAntiAlias(true);
        paint.setFilterBitmap(true);
        paint.setDither(true);
        canvas.drawARGB(0, 0, 0, 0);
        paint.setColor(Color.parseColor("#BAB399"));
        float a = radius / 2 + 0.7f;
        float b = radius / 2 + 0.01f;
        canvas.drawCircle(a, a, b, paint);
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(sbmp, rect, rect, paint);

        // outline
        paint = new Paint();
        paint.setColor(Color.WHITE);
        paint.setStyle(Paint.Style.STROKE);
        paint.setAntiAlias(true);
        paint.setStrokeWidth(9);
        canvas.drawCircle(a, a, b, paint);

        return output;
    }

    // Given a GoogleMap zoon, return some width
    public static int scaleSize(float zoom) {
        return (int)(40 + zoom * zoom / 3);
    }
}
