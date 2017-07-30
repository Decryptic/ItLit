package io.itlit.ItLit;

import android.app.Activity;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import org.json.JSONArray;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.HashMap;

public class GetLit extends AsyncTask<Activity, Void, Void> {

    public static JSONArray oldFriends = null; // This is a copy that the info window can query
    private static HashMap<String, Marker> oldMarkers; // The last set of markers drawn on TabFragment.googleMap
    private static ArrayList<Marker> burnMarkers;
    private static float oldZoom = 0.0f;

    @Override
    protected Void doInBackground(Activity... activities) {
        if (activities.length == 0)
            return null;
        Activity ui = activities[0];

        if (Const.googleMap != null) {
            try {
                JSONObject auth = new JSONObject();
                auth.put("uname", Const.uname);
                auth.put("passwd", Const.passwd);

                final JSONObject resp = new JSONObject(Network.getlit(auth.toString()));
                if (resp.has("error")) {
                    return null;
                } else if (resp.has("friends")) {
                    ui.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                JSONArray frens = resp.getJSONArray("friends");
                                oldFriends = frens;
                                HashMap<String, Marker> newMarkers = new HashMap<>();
                                int i;
                                for (i = 0; i < frens.length(); i++) {
                                    JSONObject fren = frens.getJSONObject(i);
                                    MarkerOptions mo = new MarkerOptions();
                                    final String fname = fren.getString("fname");
                                    mo.title(fname);
                                    LatLng latlon = new LatLng(fren.getDouble("lat"), fren.getDouble("lon"));
                                    mo.position(latlon);
                                    final Bitmap icon;

                                    if (Friends.faces.containsKey(fname)) {
                                        icon = Friends.faces.get(fname);
                                    } else {
                                        icon = Const.nullpic;
                                        // If the friend's icon is not already cached, try to download it
                                        new Thread(new Runnable() {
                                            @Override
                                            public void run() {
                                                try {
                                                    JSONObject jsob = new JSONObject();
                                                    jsob.put("uname", Const.uname);
                                                    jsob.put("passwd", Const.passwd);
                                                    jsob.put("fname", fname);

                                                    Bitmap bmp = Network.getpic(jsob.toString());
                                                    Friends.faces.put(fname, Const.getCroppedBitmap(bmp, bmp.getWidth()));
                                                } catch (Exception e) {
                                                    System.out.println("error doInBackground(): GetLit" + e.getMessage());
                                                }
                                            }
                                        }).start();
                                    }

                                    float zoom = Const.googleMap.getCameraPosition().zoom;
                                    int side = Const.scaleSize(zoom);
                                    mo.icon(BitmapDescriptorFactory.fromBitmap(Bitmap.createScaledBitmap(icon, side, side, false)));

                                    if (oldMarkers != null) {
                                        if (oldMarkers.containsKey(fname) && oldMarkers.get(fname) != null) {
                                            Marker oldm = oldMarkers.get(fname);
                                            if (oldm.getPosition().equals(latlon) && oldZoom == zoom) {
                                                newMarkers.put(fname, oldm);
                                                oldMarkers.remove(fname);
                                            } else {
                                                boolean b = oldm.isInfoWindowShown();
                                                oldm.remove();
                                                oldMarkers.remove(fname);
                                                Marker newm = Const.googleMap.addMarker(mo);
                                                newMarkers.put(fname, newm);
                                                if (b)
                                                    newm.showInfoWindow();
                                            }
                                        } else {
                                            newMarkers.put(fname, Const.googleMap.addMarker(mo));
                                        }
                                    } else {
                                        newMarkers.put(fname, Const.googleMap.addMarker(mo));
                                    }
                                }
                                if (oldMarkers != null)
                                    for (Marker m : oldMarkers.values())
                                        if (m != null)
                                            m.remove();
                                oldMarkers = newMarkers;
                                oldZoom = Const.googleMap.getCameraPosition().zoom;
                            } catch (Exception e) {
                                System.out.println("error doInBackground(): GetLit " + e.getMessage());
                            }
                        }
                    });
                }
            } catch (Exception e) {
                System.out.println("error doInBackground(): GetLit " + e.getMessage());
            }
        }
        return null;
    }
}
