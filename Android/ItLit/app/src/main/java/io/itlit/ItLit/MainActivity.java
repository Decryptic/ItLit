package io.itlit.ItLit;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;
import android.graphics.Bitmap;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;
import org.json.JSONArray;
import org.json.JSONObject;
import android.graphics.Matrix;
import java.io.FileOutputStream;

public class MainActivity extends AppCompatActivity {

    private int TIME = 3000;

    // Instantiated by TabFragment, every 3 seconds, get the names and faces of lit friends
    private TimerTask getlit() {
        return new TimerTask() {
            @Override
            public void run() {
                new GetLit().execute(MainActivity.this);
            }
        };
    }

    TabAdapter mTabAdapter;
    ViewPager mViewPager;
    Timer getlitTimer;

    public void destroyTimer() {
        if (getlitTimer != null) {
            getlitTimer.cancel();
            getlitTimer.purge();
            getlitTimer = null;
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        destroyTimer();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mTabAdapter = new TabAdapter(getSupportFragmentManager());

        mViewPager = (ViewPager)findViewById(R.id.pager);
        mViewPager.setAdapter(mTabAdapter);
        mViewPager.setCurrentItem(1);

        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {}
            @Override
            public void onPageSelected(int position) {
                if (getlitTimer != null) {
                    destroyTimer();
                }
                if (position == 2) {
                    getlitTimer = new Timer();
                    getlitTimer.scheduleAtFixedRate(getlit(), 0, TIME);
                }

                InputMethodManager inputMethodManager =
                        (InputMethodManager) MainActivity.this.getSystemService(
                                android.app.Activity.INPUT_METHOD_SERVICE);
                if (MainActivity.this.getCurrentFocus() != null) {
                    inputMethodManager.hideSoftInputFromWindow(
                            MainActivity.this.getCurrentFocus().getWindowToken(), 0);
                }
            }
            @Override
            public void onPageScrollStateChanged(int state) {}
        });

        Friends.init();
    }

    @Override
    public void onBackPressed() {
        if (mViewPager.getCurrentItem() == 1) {
            destroyTimer();
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        JSONObject auth = new JSONObject();
                        auth.put("uname", Const.uname);
                        auth.put("passwd", Const.passwd);

                        JSONObject respout = new JSONObject(Network.logout(auth.toString()));
                        if (respout.has("error")) {
                            final String error = respout.getString("error");
                            MainActivity.this.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(getApplicationContext(), error, Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                    }
                    catch (Exception e) {
                        System.out.println("error onBackPressed(): MainActivity couldn't log out");
                    }
                }
            }).start();
            MainActivity.this.finish();
        }
        else {
            mViewPager.setCurrentItem(1);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 0 && resultCode == Activity.RESULT_OK) {
            File dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
            File file = new File(dir.getAbsolutePath() + "/" + Const.selfiepng());
            if (file.exists()) {
                try {
                    Bitmap bm = BitmapFactory.decodeFile(file.getAbsolutePath());
                    if (bm.getWidth() >= bm.getHeight()){
                        bm = Bitmap.createBitmap(
                                bm,
                                bm.getWidth()/2 - bm.getHeight()/2,
                                0,
                                bm.getHeight(),
                                bm.getHeight()
                        );

                    } else {
                        bm = Bitmap.createBitmap(
                                bm,
                                0,
                                bm.getHeight()/2 - bm.getWidth()/2,
                                bm.getWidth(),
                                bm.getWidth()
                        );
                    }
                    Matrix matrix = new Matrix();
                    //matrix.postRotate(-90.0f);
                    final Bitmap bmp = Bitmap.createBitmap(bm, 0, 0, bm.getWidth(), bm.getHeight(), matrix, true);

                    FileOutputStream fOut = new FileOutputStream(file);
                    bmp.compress(Bitmap.CompressFormat.JPEG, 100, fOut);
                    fOut.flush();
                    fOut.close();

                    this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Const.rivSelfie.setImageBitmap(bmp);
                        }
                    });

                    final Bitmap icon = Bitmap.createScaledBitmap(bmp, 250, 250, false);
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                JSONObject auth = new JSONObject();
                                auth.put("uname", Const.uname);
                                auth.put("passwd", Const.passwd);

                                JSONObject resp = new JSONObject(Network.setpic(auth.toString(), icon));
                                if (resp.has("error")) {
                                    final String error = resp.getString("error");
                                    MainActivity.this.runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            Toast.makeText(MainActivity.this, error, Toast.LENGTH_SHORT).show();
                                        }
                                    });
                                }
                                else {
                                    if (Friends.faces.containsKey(Const.uname)) {
                                        Friends.faces.put(Const.uname, Const.getCroppedBitmap(icon, icon.getWidth()));
                                    }
                                }
                            } catch (Exception e) {
                                final String error = "Could not upload selfie to server";
                                MainActivity.this.runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        Toast.makeText(MainActivity.this, error, Toast.LENGTH_SHORT).show();
                                    }
                                });
                            }
                        }
                    }).start();
                }
                catch (Exception e) {
                    System.out.println("error onActivityResult(): MainActivity file not found");
                }
            }
        }
    }
}