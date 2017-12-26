package com.example.android.sptbi;

import android.content.Context;
import android.content.DialogInterface;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;

/**
 * Created by aashayjain611 on 26/12/17.
 */

public class UserAdapter extends ArrayAdapter<User>
{
    public UserAdapter(Context context, ArrayList<User> user)
    {
        super(context,0,user);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {

        View listItem=convertView;
        if(listItem==null)
            listItem= LayoutInflater.from(getContext()).inflate(R.layout.user_item,parent,false);
        final User current=getItem(position);
        TextView email=(TextView)listItem.findViewById(R.id.user);
        email.setText(current.getEmailId());
        TextView uid=(TextView)listItem.findViewById(R.id.uid);
        uid.setText(current.getUid());

        ImageView delete=(ImageView)listItem.findViewById(R.id.delete_user);
        delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new AlertDialog.Builder(getContext())
                        .setMessage("Are you sure you want to delete your incubator?")
                        .setCancelable(false)
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                DatabaseReference mDatabase= FirebaseDatabase.getInstance().getReference().child("Users");
                                mDatabase.child(current.getUid()).child(current.getEmailId().replace('.',',')).setValue("removed");
                                mDatabase=FirebaseDatabase.getInstance().getReference().child("Incubators");
                                mDatabase.child(current.getUid()).removeValue();
                            }
                        })
                        .setNegativeButton("No", null)
                        .show();
            }
        });
        return listItem;
    }
}
