package io.itlit.ItLit;

import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ListAdapter;
import android.provider.ContactsContract;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;

public class ContactsFragment extends Fragment {

    public ContactsFragment() {}
    public static ContactsFragment newInstance() {
        return new ContactsFragment();
    }

    private ListView listviewContacts;
    private EditText etSearch;
    private TextView tvLoading;
    private Button btnImport;

    public static ArrayList<Friend> contacts;

    private void loadContacts() {
        contacts = new ArrayList<>();

        Cursor phones = getContext().getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,null,null,null,null);
        while (phones.moveToNext())
        {
            String name = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
            String phone = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
            phone = Util.phonify(phone);

            ArrayList<Friend> friends = new ArrayList<>();
            for (Friend friend : Friends.friends) {
                if (friend != null) {
                    friends.add(friend);
                }
            }
            boolean check = false;
            for (Friend f : friends) {
                if (f.name.equals(name) && f.fname.equals(phone))
                    check = true;
            }
            contacts.add(new Friend(phone, name, check));
        }
        phones.close();
    }

    private ArrayList<Friend> filterContacts(String key) {
        ArrayList<Friend> newContacts = new ArrayList<>();
        if (key.equals(""))
            return new ArrayList<>(contacts);
        for (Friend c : contacts) {
            if (c.name.toLowerCase().contains(key.toLowerCase()) || c.fname.toLowerCase().contains(key.toLowerCase()))
                newContacts.add(c);
        }
        return newContacts;
    }

    private void updateContacts(ArrayList<Friend> list) {
        if (listviewContacts != null && list != null) {
            Collections.sort(list);
            ContactsAdapter adapter = new ContactsAdapter(getContext(), list);
            listviewContacts.setAdapter(adapter);
        }
        tvLoading.setText("");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_contacts, container, false);

        listviewContacts = (ListView)view.findViewById(R.id.listviewContacts);
        etSearch = (EditText)view.findViewById(R.id.etSearch);
        tvLoading = (TextView)view.findViewById(R.id.tvLoading);
        btnImport = (Button)view.findViewById(R.id.btnImport);

        return view;
    }

    @Override
    public void onSaveInstanceState(Bundle b) {
        super.onSaveInstanceState(b);

        b.putString("etSearch", etSearch.getText().toString());
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        tvLoading.setText("");

        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                tvLoading.setText("Loading...");
                updateContacts(filterContacts(s.toString()));
            }
            @Override
            public void afterTextChanged(Editable editable) {}
        });

        btnImport.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Friends.addContacts(contacts);
                getActivity().finish();
            }
        });

        loadContacts();
        updateContacts(contacts);
    }
}
