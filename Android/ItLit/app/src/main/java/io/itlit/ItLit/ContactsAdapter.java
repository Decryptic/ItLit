package io.itlit.ItLit;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.TextView;
import java.util.ArrayList;

public class ContactsAdapter extends ArrayAdapter<Friend> {
    public Context ctx;
    public ArrayList<Friend> contacts;

    public ContactsAdapter(Context context, ArrayList<Friend> contacts) {
        super(context, R.layout.contact_row);
        this.ctx = context;
        this.contacts = contacts;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = LayoutInflater.from(getContext());
        final View customRow = inflater.inflate(R.layout.contact_row, parent, false);

        final Friend contact = contacts.get(position);

        final CheckBox checkbox = (CheckBox)customRow.findViewById(R.id.checkbox);
        checkbox.setChecked(contact.lit);
        checkbox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                contact.lit = checkbox.isChecked();
            }
        });

        final TextView tvContactName = (TextView)customRow.findViewById(R.id.tvContactName);
        tvContactName.setText(contact.name);

        final TextView tvContactFname = (TextView)customRow.findViewById(R.id.tvContactFname);
        tvContactFname.setText(contact.fname);

        return customRow;
    }

    @Override
    public int getCount() {
        return  contacts.size();
    }
}
