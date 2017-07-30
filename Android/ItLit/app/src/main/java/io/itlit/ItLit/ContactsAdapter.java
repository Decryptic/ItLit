package io.itlit.ItLit;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.TextView;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;

public class ContactsAdapter extends ArrayAdapter<HashMap<String, Object>> {

    public Context ctx;
    public ArrayList<HashMap<String, Object>> contacts;

    public ContactsAdapter(Context context, ArrayList<HashMap<String, Object>> contacts) {
        super(context, R.layout.contact_row);
        this.ctx = context;
        this.contacts = contacts;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = LayoutInflater.from(getContext());
        final View customRow = inflater.inflate(R.layout.contact_row, parent, false);

        final HashMap<String, Object> contact = contacts.get(position);

        final CheckBox checkbox = (CheckBox)customRow.findViewById(R.id.checkbox);
        checkbox.setChecked((boolean)contact.get("lit"));
        checkbox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                contact.put("lit", checkbox.isChecked());
            }
        });

        final TextView tvContactName = (TextView)customRow.findViewById(R.id.tvContactName);
        tvContactName.setText((String)contact.get("name"));

        final TextView tvContactFname = (TextView)customRow.findViewById(R.id.tvContactFname);
        tvContactFname.setText((String)contact.get("fname"));

        return customRow;
    }

    @Override
    public int getCount() {
        return contacts.size();
    }
}
