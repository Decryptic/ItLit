package io.itlit.ItLit;

import android.*;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.ActivityCompat;
import org.json.JSONObject;
import android.content.SharedPreferences;

public class GPSService extends Service {
    public GPSService() {
    }

    public static Location lastKnown; // The last known location of the user
    private LocationManager locationManager;
    private LocationListener locationListener;

    private final long TIME = 2000; // Every 2 seconds
    private final float DIST = 5.0f; // Every 10 meters

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        if (locationManager != null && locationListener != null) {
            int permission = ActivityCompat.checkSelfPermission(getApplicationContext(), android.Manifest.permission.ACCESS_FINE_LOCATION);
            if (permission == PackageManager.PERMISSION_GRANTED) {
                locationManager.removeUpdates(locationListener);
            }
        }
        lastKnown = null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        int permission = ActivityCompat.checkSelfPermission(getApplicationContext(), android.Manifest.permission.ACCESS_FINE_LOCATION);
        if (permission == PackageManager.PERMISSION_GRANTED) {
            locationListener = new LocationListener() {
                @Override
                public void onLocationChanged(final Location location) {
                    if (location.getLatitude() != 0.0f && location.getLongitude() != 0.0f && User.lit) {
                        if (lastKnown == null) {
                            lastKnown = location;
                        }
                        else {
                            if (isBetterLocation(location, lastKnown)) {
                                lastKnown = location;
                            }
                        }
                        Thread t = new Thread(new Runnable() {
                            @Override
                            public void run() {
                                if (lastKnown != null) {
                                    try {
                                        JSONObject auth = new JSONObject();
                                        auth.put("uname", User.uname);
                                        auth.put("passwd", User.passwd);
                                        auth.put("lat", lastKnown.getLatitude());
                                        auth.put("lon", lastKnown.getLongitude());

                                        JSONObject resp = new JSONObject(Network.move(auth.toString()));
                                        if (resp.has("error")) {
                                            System.out.println(resp.get("error"));
                                        }
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                }
                            }
                        });
                        t.start();
                    }
                }

                @Override
                public void onStatusChanged(String s, int i, Bundle bundle) {}
                @Override
                public void onProviderEnabled(String s) {}
                @Override
                public void onProviderDisabled(String s) {}
            };
            locationManager = (LocationManager) getApplicationContext().getSystemService(Context.LOCATION_SERVICE);
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, TIME, DIST, locationListener);
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, TIME, DIST, locationListener);
        }
    }

    private boolean isBetterLocation(Location location, Location currentBestLocation) {
        if (currentBestLocation == null) {
            return true;
        }

        // Check whether the new location fix is newer or older
        final long TWO_MINUTES = 1000 * 60 * 2;
        long timeDelta = location.getTime() - currentBestLocation.getTime();
        boolean isSignificantlyNewer = timeDelta > TWO_MINUTES;
        boolean isSignificantlyOlder = timeDelta < -TWO_MINUTES;
        boolean isNewer = timeDelta > 0;

        if (isSignificantlyNewer) {
            return true;
            // If the new location is more than two minutes older, it must be worse
        } else if (isSignificantlyOlder) {
            return false;
        }

        // Check whether the new location fix is more or less accurate
        int accuracyDelta = (int) (location.getAccuracy() - currentBestLocation.getAccuracy());
        boolean isLessAccurate = accuracyDelta > 0;
        boolean isMoreAccurate = accuracyDelta < 0;
        boolean isSignificantlyLessAccurate = accuracyDelta > 200;

        // Check if the old and new location are from the same provider
        boolean isFromSameProvider;
        if (location.getProvider() == null) {
            isFromSameProvider = currentBestLocation.getProvider() == null;
        } else {
            isFromSameProvider = location.getProvider().equals(currentBestLocation.getProvider());
        }

        // Determine location quality using a combination of timeliness and accuracy
        if (isMoreAccurate) {
            return true;
        } else if (isNewer && !isLessAccurate) {
            return true;
        } else if (isNewer && !isSignificantlyLessAccurate && isFromSameProvider) {
            return true;
        }
        return false;
    }
}
