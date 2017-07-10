package io.itlit.ItLit;

import android.*;
import android.Manifest;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.location.Address;
import android.location.Geocoder;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.os.Bundle;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import org.json.JSONObject;
import java.util.List;
import java.util.Locale;
import android.net.Uri;

public class TabFragment extends Fragment {
    static final String TNUM = "num";
    public int mNum;

    public static GoogleMap googleMap;

    public static TabFragment newInstance(int num) {
        TabFragment f = new TabFragment();

        Bundle args = new Bundle();
        args.putInt(TNUM, num);
        f.setArguments(args);

        return f;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mNum = getArguments() != null ? getArguments().getInt(TNUM) : 1;
    }

    @Override
    public void onStop() {
        super.onStop();


    }

    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        Bundle args = getArguments();
        int page = args.getInt(TNUM);
        final View rootView;


        if (page == 0) { // friends tab

            rootView = inflater.inflate(R.layout.fragment_friends, container, false);

            final ListView listview = (ListView) rootView.findViewById(R.id.listview);
            listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(final AdapterView<?> adapterView, View view, int i, long l) {
                    if (i == 0)
                        return; // do nothing if the selfie row is clicked

                    final int j = i;
                    final View v = view;
                    final TextView tvName = (TextView) v.findViewById(R.id.tvName);
                    Thread t = new Thread(new Runnable() {
                        @Override
                        public void run() { // Change the little light bulb on the server
                            final Friend f = Friends.friends.get(j);
                            if (f == null)
                                return; // This should never happen with the previous return

                            try {
                                JSONObject auth = new JSONObject();
                                auth.put("uname", User.uname);
                                auth.put("passwd", User.passwd);
                                JSONObject fren = new JSONObject();
                                fren.put("fname", f.fname);
                                fren.put("name", f.name);
                                fren.put("lit", !f.lit);
                                auth.put("friend", fren);

                                JSONObject resp = new JSONObject(Network.setfriend(auth.toString()));
                                if (resp.has("error")) {
                                    final String error = resp.getString("error");
                                    getActivity().runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            Toast.makeText(getContext(), error, Toast.LENGTH_SHORT).show();
                                        }
                                    });
                                } else { // If the server flipped the little bulb, flip the little bulb
                                    f.lit = !f.lit;
                                    getActivity().runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            Drawable img;
                                            img = getContext().getDrawable(f.lit ? R.drawable.candleon : R.drawable.candleoff);
                                            tvName.setCompoundDrawablesWithIntrinsicBounds(null, null, img, null);
                                        }
                                    });
                                }
                            } catch (Exception e) {
                                String whatdo = f.lit ? "unlighting" : "lighting";
                                final String error = "Please try " + whatdo + " fam later";
                                getActivity().runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        Toast.makeText(rootView.getContext(), error, Toast.LENGTH_SHORT).show();
                                    }
                                });
                                e.printStackTrace();
                            }
                        }
                    });
                    t.start();
                }
            });

            listview.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
                @Override
                public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) { // Edit / delete friend
                    final Friend f = Friends.friends.get(i);
                    if (f == null)
                        return false; // can't delete the selfie row

                    DialogInterface.OnClickListener noyes = new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            switch (i) {
                                case DialogInterface.BUTTON_POSITIVE: // Edit friend
                                    Intent editIntent = new Intent(getActivity(), AddActivity.class);
                                    editIntent.putExtra("fname", f.fname);
                                    editIntent.putExtra("name", f.name);
                                    getActivity().startActivity(editIntent);
                                    break;
                                case DialogInterface.BUTTON_NEGATIVE: // Delete friend
                                    Thread t = new Thread(new Runnable() {
                                        @Override
                                        public void run() {
                                            try {
                                                JSONObject auth = new JSONObject();
                                                auth.put("uname", User.uname);
                                                auth.put("passwd", User.passwd);
                                                JSONObject fren = new JSONObject();
                                                fren.put("fname", f.fname);
                                                fren.put("name", f.name);
                                                auth.put("friend", fren);

                                                JSONObject resp = new JSONObject(Network.delfriend(auth.toString()));
                                                if (resp.has("error")) {
                                                    final String error = resp.getString("error");
                                                    getActivity().runOnUiThread(new Runnable() {
                                                        @Override
                                                        public void run() {
                                                            Toast.makeText(getContext(), error, Toast.LENGTH_SHORT).show();
                                                        }
                                                    });
                                                } else {
                                                    Friends.updateFriends(); // Redownloaded friends
                                                }
                                            } catch (Exception e) {
                                                getActivity().runOnUiThread(new Runnable() {
                                                    @Override
                                                    public void run() {
                                                        Toast.makeText(getContext(), "Please try deleting again later", Toast.LENGTH_SHORT).show();
                                                    }
                                                });
                                            }
                                        }
                                    });
                                    t.start();
                                    break;
                                default:
                                    dialogInterface.dismiss();
                            }
                        }
                    };

                    AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                    builder.setMessage(f.name + "\n\n" + f.fname);
                    builder.setPositiveButton("Edit", noyes);
                    builder.setNegativeButton("Delete", noyes);
                    builder.show();
                    return true;
                }
            });

            Friends.listview = listview;
            Friends.context = getContext();
            Friends.activity = getActivity(); // necessary to mod listview
            Friends.updateFriends(); // init listview

            return rootView;

        } else if (page == 2) { // Map tab

            rootView = inflater.inflate(R.layout.fragment_map, container, false);

            GoogleApiAvailability api = GoogleApiAvailability.getInstance();
            int permission = api.isGooglePlayServicesAvailable(getActivity());
            if (permission == ConnectionResult.SUCCESS) {

                final SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.mapFragment);
                mapFragment.getMapAsync(new OnMapReadyCallback() {
                    @Override
                    public void onMapReady(GoogleMap gm) {
                        googleMap = gm;
                        googleMap.getUiSettings().setTiltGesturesEnabled(false);
                        googleMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
                            @Override
                            public boolean onMarkerClick(final Marker marker) {
                                getActivity().runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        if (googleMap != null) {
                                            float newZoom = googleMap.getCameraPosition().zoom;
                                            if (newZoom < 7.2f) {
                                                newZoom = 10.0f;
                                            } else if (newZoom < 10.3f) {
                                                newZoom = 18.0f;
                                            }
                                            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(marker.getPosition(), newZoom));
                                        }
                                    }
                                });
                                return false;
                            }
                        });

                        googleMap.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {
                            @Override
                            public void onMapLongClick(final LatLng latLng) {
                                getActivity().runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        if (googleMap != null) {
                                            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 3.0f));
                                        }
                                    }
                                });
                            }
                        });

                        final View statusWindow = getActivity().getLayoutInflater().inflate(R.layout.status_window, null);
                        googleMap.setInfoWindowAdapter(new GoogleMap.InfoWindowAdapter() {
                            @Override
                            public View getInfoWindow(Marker marker) {
                                return null; // name\n\nstatus\n\nbusiness name or st address part only
                            }

                            @Override
                            public View getInfoContents(Marker marker) {
                                final TextView tvName = (TextView)statusWindow.findViewById(R.id.statusName);
                                final TextView tvStatus = (TextView)statusWindow.findViewById(R.id.statusStatus);
                                String name = "?";
                                StringBuilder sbStatus = new StringBuilder();
                                try {
                                    if (GetLit.oldFriends != null) {
                                        int x;
                                        for (x=0; x < GetLit.oldFriends.length(); ++x) {
                                            JSONObject fren = GetLit.oldFriends.getJSONObject(x);
                                            if (fren.getString("fname").equals(marker.getTitle())) {
                                                name = fren.getString("name");
                                                if (name.equals("")) {
                                                    name = marker.getTitle();
                                                } else if (!name.equals(marker.getTitle())) {
                                                    sbStatus.append(marker.getTitle());
                                                    sbStatus.append("\n");
                                                }
                                                String status = fren.getString("status");
                                                if (!status.equals("")) {
                                                    sbStatus.append("\n");
                                                    sbStatus.append(status);
                                                    sbStatus.append("\n");
                                                }
                                                sbStatus.append("\n");
                                                /*Geocoder gc = new Geocoder(getContext(), Locale.getDefault());
                                                LatLng psn = marker.getPosition();
                                                List<Address> addresses = gc.getFromLocation(psn.latitude, psn.longitude, 1);
                                                if (addresses.size() > 0) {
                                                    String street = addresses.get(0).getAddressLine(0);
                                                    String city = addresses.get(0).getAddressLine(1);
                                                    if (street != null && !street.equals("")) {
                                                        sbStatus.append(street);
                                                        sbStatus.append("\n");
                                                    }
                                                    if (city != null && !city.equals("")) {
                                                        sbStatus.append(city);
                                                        sbStatus.append("\n");
                                                    }
                                                }*/
                                            }
                                        }
                                    } else {
                                        name = "null";
                                    }
                                } catch (Exception e) {
                                    name = "??";
                                    System.out.println(e.toString());
                                }
                                tvName.setText(name);
                                tvStatus.setText(sbStatus.toString());
                                return statusWindow;
                            }
                        });
                        googleMap.setOnInfoWindowLongClickListener(new GoogleMap.OnInfoWindowLongClickListener() {
                            @Override
                            public void onInfoWindowLongClick(Marker marker) {
                                Intent textIntent = new Intent(Intent.ACTION_VIEW);
                                textIntent.setData(Uri.parse("sms:" + marker.getTitle()));
                                startActivity(textIntent);
                            }
                        });
                        googleMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
                            @Override
                            public void onInfoWindowClick(Marker marker) {
                                if (marker.isInfoWindowShown())
                                    marker.hideInfoWindow();
                            }
                        });
                    }
                });
            } else if (api.isUserResolvableError(permission)) {
                Dialog box = api.getErrorDialog(getActivity(), permission, 0);
                box.show();
            } else {
                final String error = "App needs permission to use maps";
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getContext(), error, Toast.LENGTH_SHORT).show();
                    }
                });
            }

            Faces.nullpic = BitmapFactory.decodeResource(getContext().getResources(), R.drawable.nullpic);

            return rootView;
        } else if (page == 1) { // light tab
            rootView = inflater.inflate(R.layout.fragment_light, container, false);

            final TextView tvLogout = (TextView) rootView.findViewById(R.id.tvLogout);
            tvLogout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    SharedPreferences settings = getContext().getSharedPreferences(Util.userprefs(), Context.MODE_PRIVATE);
                    User.forgetMe(settings.edit());
                    getActivity().onBackPressed();
                }
            });

            final TextView tvChars = (TextView) rootView.findViewById(R.id.tvChars);
            final EditText etStatus = (EditText) rootView.findViewById(R.id.etStatus);

            final ImageView ivLight = (ImageView) rootView.findViewById(R.id.ivLight);
            if (!User.lit) {
                ivLight.setImageResource(R.drawable.lightoff);
            }
            else {
                ivLight.setImageResource(R.drawable.lighton);
            }
            ivLight.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    int permission = ActivityCompat.checkSelfPermission(getContext(), android.Manifest.permission.ACCESS_FINE_LOCATION);
                    if (permission != PackageManager.PERMISSION_GRANTED) {
                        ActivityCompat.requestPermissions(getActivity(),
                                new String[] {Manifest.permission.ACCESS_COARSE_LOCATION,
                                        Manifest.permission.ACCESS_FINE_LOCATION}, 0
                        );
                    }
                    else {
                        final ImageView ivLight = (ImageView) getActivity().findViewById(R.id.ivLight);
                        final TextView tvLighttalk = (TextView) getActivity().findViewById(R.id.tvLighttalk);
                        Thread t = new Thread(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    JSONObject auth = new JSONObject();
                                    auth.put("uname", User.uname);
                                    auth.put("passwd", User.passwd);
                                    auth.put("lit", !User.lit);

                                    JSONObject resp = new JSONObject(Network.light(auth.toString()));
                                    if (resp.has("error")) {
                                        final String error = resp.getString("error");
                                        getActivity().runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                Toast.makeText(getContext(), error, Toast.LENGTH_SHORT).show();
                                            }
                                        });
                                    } else {
                                        User.setLight(!User.lit);
                                        if (User.lit)
                                            getActivity().startService(new Intent(getContext(), GPSService.class));
                                        else
                                            getActivity().stopService(new Intent(getContext(), GPSService.class));
                                        final int d = User.lit ? R.drawable.lighton : R.drawable.lightoff;
                                        String offline = "offline";
                                        String online = "friends can see you";
                                        final String s = User.lit ? online : offline;

                                        getActivity().runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                ivLight.setImageResource(d);
                                                tvLighttalk.setText(s);
                                            }
                                        });
                                    }
                                } catch (Exception e) {
                                    e.printStackTrace();
                                    final String error = "Please try lighting up later";
                                    getActivity().runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            Toast.makeText(rootView.getContext(), error, Toast.LENGTH_SHORT).show();
                                        }
                                    });
                                }
                            }
                        });
                        t.start();
                    }
                }
            });

            etStatus.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}
                @Override
                public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {}
                @Override
                public void afterTextChanged(Editable editable) {
                    int len = etStatus.getText().toString().length();
                    if (len <= 50) {
                        tvChars.setText(len + " characters");
                    }

                    final String status = etStatus.getText().toString();
                    Thread t = new Thread(new Runnable() {
                        @Override
                        public void run() { // Send status to server
                            try {
                                JSONObject auth = new JSONObject();
                                auth.put("uname", User.uname);
                                auth.put("passwd", User.passwd);
                                auth.put("status", status);

                                JSONObject respStatus = new JSONObject(Network.status(auth.toString()));
                                if (respStatus.has("error")) {
                                    final String error = respStatus.getString("error");
                                    getActivity().runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            Toast.makeText(getContext(), error, Toast.LENGTH_SHORT).show();
                                        }
                                    });
                                }
                            } catch (Exception e) {
                                getActivity().runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        Toast.makeText(getContext(), "Error posting status to server", Toast.LENGTH_SHORT).show();
                                    }
                                });
                            }
                        }
                    });
                    t.start();
                } // this might not be the most efficient way to do this
            });

            Thread tstat = new Thread(new Runnable() {
                @Override
                public void run() { // get status on startup
                    try {
                        JSONObject jsob = new JSONObject();
                        jsob.put("uname", User.uname);
                        jsob.put("passwd", User.passwd);

                        JSONObject respStat = new JSONObject(Network.statusget(jsob.toString()));
                        if (respStat.has("error")) {
                            final String error = respStat.getString("error");
                            getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(getContext(), error, Toast.LENGTH_SHORT);
                                }
                            });
                        } else if (respStat.has("status")) {
                            final String status = respStat.getString("status");
                            getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    etStatus.setText(status);
                                }
                            });
                        }
                    } catch (Exception e) {
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(getContext(), "Error getting status from server", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                }
            });
            tstat.start();

            return rootView;
        }
        return null;
    }
}
