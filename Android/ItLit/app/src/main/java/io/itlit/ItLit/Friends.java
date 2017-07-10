package io.itlit.ItLit;

import android.content.Context;
import java.util.ArrayList;
import java.util.Collections;
import android.app.Activity;
import android.os.Handler;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.Toast;
import org.json.JSONObject;
import org.json.JSONArray;

public class Friends {
    public static ArrayList<Friend> friends; // list of friends, first element (You) is null
    public static ListView listview; // for updating
    public static Context context;   // set when Friends fragment is created
    public static Activity activity;

    private static void ginit() {
        if (friends == null)
            friends = new ArrayList<Friend>();
        if (friends.isEmpty())
            friends.add(null);
    }

    public static void deleteAll() {
        friends = null;
        ginit();
    }

    public static void fromJSON(JSONArray frens) {
        deleteAll();
        int i;
        for (i = 0; i < frens.length(); i++) {
            try {
                JSONObject fren = frens.getJSONObject(i);
                String fname = fren.getString("fname");
                String name  = fren.getString("name");
                Boolean lit  = fren.getBoolean("lit");
                friends.add(new Friend(fname, name, lit));
            }
            catch (Exception e) {
                System.out.println("malformed friend from server");
                continue;
            }
        }
    }

    private static void runOnUiThread(Runnable r, Context context) {
        Handler handler = new Handler(context.getMainLooper());
        handler.post(r);
    }
    public static void updateFriends() {
        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    JSONObject auth = new JSONObject();
                    auth.put("uname", User.uname);
                    auth.put("passwd", User.passwd);
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
                        Collections.sort(friends);
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
                    final String error = "Could not update friends from server";
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(context, error, Toast.LENGTH_SHORT);
                        }
                    }, context);
                }
            }
        });
        t.start();
    }

    public static void addContacts(ArrayList<Friend> contacts) {
        int i;
        for (i = 0; i < contacts.size(); i++) {
            if (contacts.get(i) != null) {
                Friend c = contacts.get(i);
                int j;
                if (c.lit) {
                    boolean exists = false;
                    for (j = 0; j < friends.size(); j++) {
                        if (friends.get(j) != null) {
                            Friend f = friends.get(j);
                            if (f.fname.equals(c.fname)) {
                                exists = true;
                                if (!f.name.equals(c.name)) {
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
                            Friend f = friends.get(j);
                            if (c.name.equals(f.name) && c.fname.equals(f.fname)) {
                                friends.remove(j);
                                j--;
                            }
                        }
                    }
                }
            }
        }

        Collections.sort(friends);
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
                    auth.put("uname", User.uname);
                    auth.put("passwd", User.passwd);
                    JSONArray frens = new JSONArray();
                    for (Friend f : friends) {
                        if (f != null) {
                            JSONObject fren = new JSONObject();
                            fren.put("fname", f.fname);
                            fren.put("name", f.name);
                            fren.put("lit", f.lit);
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
