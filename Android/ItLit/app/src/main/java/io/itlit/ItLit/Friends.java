package io.itlit.ItLit;

import android.content.Context;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Objects;

import android.app.Activity;
import android.graphics.Bitmap;
import android.os.Handler;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.Toast;
import org.json.JSONObject;
import org.json.JSONArray;

public class Friends {
    public static ArrayList<HashMap<String, Object>> contacts = new ArrayList<>();
    public static ArrayList<HashMap<String, Object>> friends = new ArrayList<>();
    public static HashMap<String, Bitmap> faces = new HashMap<>();

    public static ListView listview; // for updating
    public static Context context;   // set when Friends fragment is created
    public static Activity activity;

    public static void init() {
        friends = new ArrayList<HashMap<String, Object>>();
    }

    public static void fromJSON(JSONArray frens) {
        init();
        int i;
        for (i = 0; i < frens.length(); i++) {
            try {
                JSONObject fren = frens.getJSONObject(i);
                HashMap<String, Object> friend = new HashMap<String, Object>();
                friend.put("fname", fren.getString("fname"));
                friend.put("name", fren.getString("name"));
                friend.put("lit", fren.getBoolean("lit"));

                friends.add(friend);
            }
            catch (Exception e) {
                System.out.println("error fromJSON(): malformed friend from server");
                continue;
            }
        }
    }

    private static void runOnUiThread(Runnable r, Context context) {
        Handler handler = new Handler(context.getMainLooper());
        handler.post(r);
    }
    public static void updateFriends() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    JSONObject auth = new JSONObject();
                    auth.put("uname", Const.uname);
                    auth.put("passwd", Const.passwd);
                    JSONObject respFriends = new JSONObject(Network.getfriends(auth.toString()));

                    if (respFriends.has("error")) {
                        final String error = respFriends.getString("error");
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(context, error, Toast.LENGTH_SHORT);
                            }
                        }, context);
                    }
                    else if (respFriends.has("friends")) {
                        fromJSON(respFriends.getJSONArray("friends"));
                        // TODO Collections.sort(friends);
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                ListAdapter adapter = new FriendsAdapter(context, friends, activity);
                                listview.setAdapter(adapter);
                            }
                        }, context);
                    }
                }
                catch (Exception e) {
                    System.out.println("error updateFriends(): could not update friends on server");
                }
            }
        }).start();
    }

    public static void addContacts(ArrayList<HashMap<String, Object>> contacts) {
        int i;
        for (i = 0; i < contacts.size(); i++) {
            if (contacts.get(i) != null) {
                HashMap<String, Object> c = contacts.get(i);
                int j;
                if ((boolean)c.get("lit")) {
                    boolean exists = false;
                    for (j = 0; j < friends.size(); j++) {
                        if (friends.get(j) != null) {
                            HashMap<String, Object> f = friends.get(j);
                            if (((String)f.get("fname")).equals((String)c.get("fname"))) {
                                exists = true;
                                if (!((String)f.get("name")).equals((String)c.get("name"))) {
                                    friends.remove(j);
                                    friends.add(c);
                                    j--;
                                }
                            }
                        }
                    }
                    if (!exists)
                        friends.add(c);
                }
                else {
                    for (j = 0; j < friends.size(); j++) {
                        if (friends.get(j) != null) {
                            HashMap<String, Object> f = friends.get(j);
                            if (((String)c.get("name")).equals((String)f.get("name"))
                                    && ((String)c.get("fname")).equals((String)f.get("fname"))) {
                                friends.remove(j);
                                j--;
                            }
                        }
                    }
                }
            }
        }

        // TODO Collections.sort(friends); ecName stuff
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                ListAdapter adapter = new FriendsAdapter(context, friends, activity);
                listview.setAdapter(adapter);
            }
        }, context);

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    JSONObject auth = new JSONObject();
                    auth.put("uname", Const.uname);
                    auth.put("passwd", Const.passwd);
                    JSONArray frens = new JSONArray();
                    for (HashMap<String, Object> f : friends) {
                        if (f != null) {
                            JSONObject fren = new JSONObject();
                            fren.put("fname", (String)f.get("fname"));
                            fren.put("name", (String)f.get("name"));
                            fren.put("lit", (boolean)f.get("lit"));
                            frens.put(fren);
                        }
                    }
                    auth.put("friends", frens);

                    JSONObject resp = new JSONObject(Network.setfriends(auth.toString()));
                    if (resp.has("error")) {
                        System.out.println(resp.get("error"));
                    }
                }
                catch (Exception e) {
                    System.out.println("error uploading friends to server");
                }
            }
        }).start();
    }
}
