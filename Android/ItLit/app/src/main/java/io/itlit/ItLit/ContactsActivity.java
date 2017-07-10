package io.itlit.ItLit;

import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import java.util.ArrayList;

public class ContactsActivity extends AppCompatActivity {
    //remember, check contacts that are already friends

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contacts);

        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ContactsFragment cf = ContactsFragment.newInstance();
        ft.show(cf).commit();
    }
}
