package com.example.android.sptbi;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

public class IncubatorDetails extends AppCompatActivity {

    private String key;
    private DatabaseReference mDatabase;
    private Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_incubator_details);

        toolbar=(Toolbar)findViewById(R.id.details_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Profile");
        key=getIntent().getExtras().getString("UID");
        mDatabase= FirebaseDatabase.getInstance().getReference();
        mDatabase.keepSynced(true);
        mDatabase.child("Incubators").child(key).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                try
                {
                    String name=dataSnapshot.child("Name").getValue().toString();
                    String founder=dataSnapshot.child("Founder").getValue().toString();
                    String email=dataSnapshot.child("Email").getValue().toString();
                    String joined=dataSnapshot.child("Joined").getValue().toString();
                    String contact=dataSnapshot.child("Contact").getValue().toString();
                    final String image=dataSnapshot.child("Image").getValue().toString();
                    TextView name_text=(TextView)findViewById(R.id.detail_name);
                    name_text.setText(name);
                    TextView email_text=(TextView)findViewById(R.id.detail_email);
                    email_text.setText(email);
                    TextView contact_text=(TextView)findViewById(R.id.detail_contact);
                    contact_text.setText(contact);
                    TextView founder_text=(TextView)findViewById(R.id.detail_fndr);
                    founder_text.setText(founder);
                    TextView join_text=(TextView)findViewById(R.id.detail_jfrm);
                    join_text.setText(joined);
                    final ImageView imageView=(ImageView)findViewById(R.id.detail_img);
                    Picasso.with(IncubatorDetails.this).load(image).networkPolicy(NetworkPolicy.OFFLINE).into(imageView, new Callback() {
                        @Override
                        public void onSuccess() {

                        }

                        @Override
                        public void onError() {
                            Picasso.with(IncubatorDetails.this).load(image).into(imageView);
                        }
                    });
                }
                catch (NullPointerException npe)
                {
                    Toast.makeText(IncubatorDetails.this,"No incubator registration found",Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }
}
