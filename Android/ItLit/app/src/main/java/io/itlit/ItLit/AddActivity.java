package io.itlit.ItLit;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import org.json.JSONObject;
import android.widget.Toast;

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
    protected void onSaveInstanceState(Bundle b) {
        super.onSaveInstanceState(b);

        final EditText etName = (EditText)findViewById(R.id.etName);
        final EditText etFname = (EditText)findViewById(R.id.etFname);
        b.putString("etName", etName.getText().toString());
        b.putString("etFname", etFname.getText().toString());
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add);

        final EditText etName = (EditText)findViewById(R.id.etName);
        final EditText etFname = (EditText)findViewById(R.id.etFname);
        if (savedInstanceState != null) {
            etName.setText(savedInstanceState.getString("etName", ""));
            etFname.setText(savedInstanceState.getString("etFname", ""));
        }
        else {
            String fn = getIntent().getStringExtra("fname");
            String n  = getIntent().getStringExtra("name");
            if (fn != null)
                etFname.setText(fn);
            if (n != null)
                etName.setText(n);
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
                final String fname = Util.phonify(etFname.getText().toString());
                String error = errorless(name, fname);
                if (error != null) {
                    Toast.makeText(getApplicationContext(), error, Toast.LENGTH_SHORT).show();
                    return;
                }

                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            JSONObject auth = new JSONObject();
                            auth.put("uname", User.uname);
                            auth.put("passwd", User.passwd);
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
                                Friends.updateFriends();
                            }
                            AddActivity.this.finish();
                        }
                        catch (Exception e) {
                            final String error = "Please try adding this friend later";
                            AddActivity.this.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(getApplicationContext(), error, Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                    }
                }).start();
            }
        });
    }
}
