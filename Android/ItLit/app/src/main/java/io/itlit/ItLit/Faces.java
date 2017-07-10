package io.itlit.ItLit;

import android.app.ActionBar;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;

import java.util.ArrayList;

public class Faces {
    private static ArrayList<String> fnames;
    private static ArrayList<Bitmap> faces;
    public static Bitmap nullpic;

    private static void init() {
        if (fnames == null)
            fnames = new ArrayList<String>();
        if (faces == null)
            faces = new ArrayList<Bitmap>();
    }

    public static boolean has(String fname) {
        init();
        if (fname == null)
            return false;

        for (String s : fnames) {
            if (fname.equals(s))
                return true;
        }
        return false;
    }

    public static Bitmap get(String fname) {
        init();
        int index = fnames.indexOf(fname);
        if (index == -1)
            return null;
        return faces.get(index);
    }

    public static void add(String fname, Bitmap bitmap) {
        init();
        fnames.add(fname);
        faces.add(bitmap);
    }

    public static void remove(String fname) {
        init();
        int index = fnames.indexOf(fname);
        if (index != -1) {
            fnames.remove(index);
            faces.remove(index);
        }
    }

    public static boolean isEmpty() {
        init();
        return fnames.isEmpty();
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

    public static int scaleSize(float zoom) {
        return (int)(40 + zoom * zoom / 3);
    }
}
