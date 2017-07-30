package io.itlit.ItLit;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import org.json.JSONObject;
import android.widget.Toast;

import java.util.HashMap;

public class AddActivity extends AppCompatActivity {

    private String errorless(String name, String fname) {
        if (name.length() > 30)
            return "Name may not exceed 30 characters";
        if (fname.length() == 0)
            return "Please try a phone number";
        if (fname.length() < 10 || fname.length() > 13)
            return "Phone number must be 10 to 13 digits";
        return null;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add);

        final EditText etName = (EditText)findViewById(R.id.etName);
        final EditText etFname = (EditText)findViewById(R.id.etFname);

        if (Const.ecFname != null) {
            etName.setText(Const.ecName);
            etFname.setText(Const.ecFname);
        }

        final Button btnAdd = (Button)findViewById(R.id.btnAdd);
        final Button btnAddCancel = (Button)findViewById(R.id.btnAddCancel);

        btnAddCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AddActivity.this.finish();
            }
        });

        btnAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                final String name = etName.getText().toString();
                final String fname = Const.phonify(etFname.getText().toString());

                String error = errorless(name, fname);
                if (error != null) {
                    Toast.makeText(getApplicationContext(), error, Toast.LENGTH_SHORT).show();
                    return;
                }

                boolean deleteOld = false;
                String oldUname = "";
                if (Const.ecOldIndex != null) {
                    oldUname = (String)Friends.friends.get(Const.ecOldIndex).get("fname");
                    deleteOld = !oldUname.equals(fname);
                }
                final String foldUname = oldUname;

                if (deleteOld) {
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                JSONObject auth = new JSONObject();
                                auth.put("uname", Const.uname);
                                auth.put("passwd", Const.passwd);
                                JSONObject fren = new JSONObject();
                                fren.put("fname", foldUname);
                                fren.put("name", name);
                                fren.put("lit", false);
                                auth.put("friend", fren);
                                JSONObject resp = new JSONObject(Network.delfriend(auth.toString()));
                                if (resp.has("error")) {
                                    final String error = resp.getString("error");
                                    System.out.println(error);
                                }
                                else {
                                    Friends.friends.remove(Const.ecOldIndex);
                                }
                            }
                            catch (Exception e) {
                                System.out.println(e.toString());
                            }
                        }
                    }).start();
                }

                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            JSONObject auth = new JSONObject();
                            auth.put("uname", Const.uname);
                            auth.put("passwd", Const.passwd);
                            JSONObject fren = new JSONObject();
                            fren.put("fname", fname);
                            fren.put("name", name);
                            fren.put("lit", false);
                            auth.put("friend", fren);
                            JSONObject resp = new JSONObject(Network.setfriend(auth.toString()));
                            if (resp.has("error")) {
                                final String error = resp.getString("error");
                                AddActivity.this.runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        Toast.makeText(getApplicationContext(), error, Toast.LENGTH_SHORT).show();
                                    }
                                });
                            }
                            else {
                                HashMap<String, Object> friend = new HashMap<String, Object>();
                                friend.put("fname", fname);
                                friend.put("name", name);
                                friend.put("lit", false);
                                Friends.friends.add(friend);
                            }

                            Const.ecName = null;
                            Const.ecFname = null;
                            Const.ecOldIndex = null;

                            Friends.updateFriends();

                            AddActivity.this.finish();
                        }
                        catch (Exception e) {
                            AddActivity.this.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(getApplicationContext(), Const.ptal, Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                    }
                }).start();
            }
        });
    }
}
