package io.itlit.ItLit;

import android.*;
import android.Manifest;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;

import org.json.JSONObject;

public class LoginActivity extends AppCompatActivity {

    public String errorless(String uname, String passwd) {
        if (passwd.equals(""))
            return "Please try a password";
        if (uname.equals(""))
            return "Please try a phone number";
        if (uname.length() < 10 || uname.length() > 13)
            return "Phone number must be 10 to 13 digits";
        return null;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        final EditText etUname = (EditText)findViewById(R.id.etUname);
        final EditText etPasswd = (EditText)findViewById(R.id.etPasswd);
        final Button btnLogin = (Button)findViewById(R.id.btnLogin);
        final Button btnRegister = (Button)findViewById(R.id.btnRegister);

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final String uname = Util.phonify(etUname.getText().toString());
                final String passwd = etPasswd.getText().toString();
                final String shapasswd = Util.sha256(passwd);

                String err = errorless(uname, passwd);
                if (err != null) {
                    Toast.makeText(getApplicationContext(), err, Toast.LENGTH_SHORT).show();
                    return;
                }
                Thread t = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            JSONObject auth = new JSONObject();
                            auth.put("uname", uname);
                            auth.put("passwd", shapasswd);

                            JSONObject resp = new JSONObject(Network.login(auth.toString()));
                            if (resp.has("error")) {
                                final String error = resp.getString("error");
                                LoginActivity.this.runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        Toast.makeText(getApplicationContext(), error, Toast.LENGTH_SHORT).show();
                                    }
                                });
                            }
                            else {
                                User.setUser(uname, shapasswd);
                                SharedPreferences settings = getApplicationContext().getSharedPreferences(Util.userprefs(), Context.MODE_PRIVATE);
                                User.rememberMe(settings.edit());
                                Intent mainIntent = new Intent(LoginActivity.this, MainActivity.class);
                                LoginActivity.this.startActivity(mainIntent);
                            }
                        }
                        catch (Exception e) {
                            LoginActivity.this.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(getApplicationContext(), "Please try logging in again later", Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                    }
                });
                t.start();
            }
        });

        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String uname = Util.phonify(etUname.getText().toString());
                final String passwd = etPasswd.getText().toString();
                final String shapasswd = Util.sha256(passwd);

                String err = errorless(uname, passwd);
                if (err != null) {
                    Toast.makeText(getApplicationContext(), err, Toast.LENGTH_SHORT).show();
                    return;
                }
                Thread t = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            JSONObject auth = new JSONObject();
                            auth.put("uname", uname);
                            auth.put("passwd", shapasswd);

                            JSONObject resp = new JSONObject(Network.register(auth.toString()));
                            if (resp.has("error")) {
                                final String error = resp.getString("error");
                                LoginActivity.this.runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        Toast.makeText(getApplicationContext(), error, Toast.LENGTH_SHORT).show();
                                    }
                                });
                            }
                            else {
                                Intent activateIntent = new Intent(LoginActivity.this, ActivateActivity.class);
                                activateIntent.putExtra("uname", uname);
                                activateIntent.putExtra("passwd", shapasswd);
                                LoginActivity.this.startActivity(activateIntent);
                            }
                        }
                        catch (Exception e) {
                            LoginActivity.this.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(getApplicationContext(), "Please try registering again later", Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                    }
                });
                t.start();
            }
        });

        SharedPreferences settings = getApplicationContext().getSharedPreferences(Util.userprefs(), Context.MODE_PRIVATE);
        if (settings.contains("uname") && settings.contains("passwd")) {
            final String uname = settings.getString("uname", "");
            etUname.setText(uname);
            final String passwd = settings.getString("passwd", "");
            if (!uname.equals("") && !passwd.equals("")){
                Thread t = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            JSONObject auth = new JSONObject();
                            auth.put("uname", uname);
                            auth.put("passwd", passwd);

                            JSONObject resp = new JSONObject(Network.login(auth.toString()));
                            if (resp.has("error")) {
                                final String error = resp.getString("error");
                                LoginActivity.this.runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        Toast.makeText(getApplicationContext(), error, Toast.LENGTH_SHORT).show();
                                    }
                                });
                            } else {
                                User.setUser(uname, passwd);
                                Intent mainIntent = new Intent(LoginActivity.this, MainActivity.class);
                                LoginActivity.this.startActivity(mainIntent);
                            }
                        }
                        catch (Exception e) {
                            LoginActivity.this.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(getApplicationContext(), "Server is talking gibberish", Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                    }
                });
                t.start();
            }
        }
    }
}
