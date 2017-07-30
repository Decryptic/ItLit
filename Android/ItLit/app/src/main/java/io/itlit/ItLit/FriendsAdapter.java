package io.itlit.ItLit;

import android.Manifest;
import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.FileProvider;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;
import java.util.ArrayList;
import java.io.File;
import java.util.HashMap;

import android.provider.MediaStore;
import android.net.Uri;
import android.graphics.BitmapFactory;
import android.widget.Toast;

import org.json.JSONObject;

public class FriendsAdapter extends ArrayAdapter<HashMap<String, Object>> {

    public Context ctx;
    public Activity act;
    public ArrayList<HashMap<String, Object>> friends;

    public FriendsAdapter(Context context, ArrayList<HashMap<String, Object>> friends, Activity act) {
        super(context, R.layout.friend_row);
        this.ctx = context;
        this.act = act;
        this.friends = friends;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = LayoutInflater.from(getContext());
        final View customRow;

        int pos = position - 1;

        if (pos == -1) { // selfie row
            customRow = inflater.inflate(R.layout.selfie_row, parent, false);

            final RoundedImageView riv = (RoundedImageView)customRow.findViewById(R.id.selfie);
            Const.rivSelfie = riv; // For MainActivity

            int permission = ActivityCompat.checkSelfPermission(ctx, android.Manifest.permission.WRITE_EXTERNAL_STORAGE);
            if (permission != PackageManager.PERMISSION_GRANTED) {
                riv.setImageResource(R.drawable.nullpic);
            }
            else {
                File dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
                File file = new File(dir.getAbsolutePath() + "/" + Const.selfiepng());
                if (!file.exists() || file.isDirectory())
                    riv.setImageResource(R.drawable.nullpic);
                else {
                    riv.setImageBitmap(BitmapFactory.decodeFile(file.getAbsolutePath()));
                }
            }

            riv.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    int permission = ActivityCompat.checkSelfPermission(ctx, android.Manifest.permission.WRITE_EXTERNAL_STORAGE);
                    if (permission != PackageManager.PERMISSION_GRANTED) {
                        ActivityCompat.requestPermissions(act,
                                new String[] {android.Manifest.permission.READ_EXTERNAL_STORAGE,
                                        Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1
                        );
                    }
                    else {
                        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                        if (takePictureIntent.resolveActivity(ctx.getPackageManager()) != null) {
                            try {
                                File dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
                                File file = new File(dir.getAbsolutePath() + "/" + Const.selfiepng());
                                Uri provider = FileProvider.getUriForFile(ctx, ctx.getApplicationContext().getPackageName() + ".provider", file);
                                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, provider);
                                act.startActivityForResult(takePictureIntent, 0);
                            } catch (Exception e) {
                                e.printStackTrace();
                                final String error = "Could not save picture";
                                act.runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        Toast.makeText(ctx, error, Toast.LENGTH_SHORT).show();
                                    }
                                });
                            }
                        } else {
                            final String error = "Could not open camera";
                            act.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(ctx, error, Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                    }
                }
            });

            final TextView tvPhone = (TextView)customRow.findViewById(R.id.tvPhone);
            tvPhone.setText(Const.uname);

            final Button btnAddFriend = (Button)customRow.findViewById(R.id.btnAddFriend);
            btnAddFriend.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent addIntent = new Intent(getContext(), AddActivity.class);
                    ctx.startActivity(addIntent);
                }
            });

            final Button btnImport = (Button)customRow.findViewById(R.id.btnImport);
            btnImport.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    int permission = ActivityCompat.checkSelfPermission(act, android.Manifest.permission.READ_CONTACTS);
                    if (permission != PackageManager.PERMISSION_GRANTED) {
                        ActivityCompat.requestPermissions(act,
                                new String[] {android.Manifest.permission.READ_CONTACTS}, 0
                        );
                    }
                    else {
                        Intent contactsIntent = new Intent(ctx, ContactsActivity.class);
                        ctx.startActivity(contactsIntent);
                    }
                }
            });
        }

        else {
            customRow = inflater.inflate(R.layout.friend_row, parent, false);

            TextView tvName = (TextView)customRow.findViewById(R.id.tvName);
            TextView tvFname = (TextView)customRow.findViewById(R.id.tvFname);

            HashMap<String, Object> friend = friends.get(pos);
            if (friend != null) {
                tvFname.setText((String)friend.get("fname"));
                tvName.setText((String)friend.get("name"));
                boolean lit = (boolean)friend.get("lit");
                Drawable img;
                img = getContext().getDrawable(lit ? R.drawable.candleon : R.drawable.candleoff);
                tvName.setCompoundDrawablesWithIntrinsicBounds(null, null, img, null);
            }
            else {
                System.out.println("error getView(): FriendsAdapter friend not found");
            }
        }

        return customRow;
    }

    @Override
    public int getCount() {
        return friends.size() + 1;
    }
}
