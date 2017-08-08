package io.itlit.ItLit;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import org.json.JSONObject;

public class ActivateActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_activate);

        final EditText etCode = (EditText)findViewById(R.id.etCode);
        final Button btnActivate = (Button)findViewById(R.id.btnActivate);

        btnActivate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String scode = etCode.getText().toString();

                if (scode.length() != 5) {
                    Toast.makeText(getApplicationContext(), "Please try a 5 digit code", Toast.LENGTH_SHORT).show();
                    return;
                }

                boolean digits = true;
                for (char c : scode.toCharArray())
                    digits = digits && Character.isDigit(c);
                if (!digits) {
                    Toast.makeText(getApplicationContext(), "Code must be all digits", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (Const.activateAttempts >= 3) {
                    Toast.makeText(getApplicationContext(), Const.ptal, Toast.LENGTH_SHORT).show();
                    ActivateActivity.this.finish();
                    return;
                }

                final int code = Integer.parseInt(scode);
                Thread t = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            JSONObject jsob = new JSONObject();
                            jsob.put("uname", getIntent().getStringExtra("uname"));
                            jsob.put("passwd", getIntent().getStringExtra("passwd"));
                            jsob.put("code", code);
                            final JSONObject resp = new JSONObject(Network.activate(jsob.toString()));
                            ActivateActivity.this.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    String sresp = "Welcome, please log in";
                                    boolean active = true;
                                    try {
                                        if (resp.has("error")) {
                                            sresp = resp.getString("error");
                                            Const.activateAttempts++;
                                            active = false;
                                        }
                                    } catch (Exception e) { active = false; }
                                    Toast.makeText(getApplicationContext(), sresp, Toast.LENGTH_SHORT).show();
                                    if (active) {
                                        ActivateActivity.this.finish();
                                    }
                                }
                            });
                        }
                        catch (Exception e) {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(getApplicationContext(), Const.ptal, Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                    }
                });
                t.start();
            }
        });
    }
}
