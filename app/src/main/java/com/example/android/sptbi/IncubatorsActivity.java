package com.example.android.sptbi;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.Picasso;

public class IncubatorsActivity extends AppCompatActivity {

    private DatabaseReference mDatabase;
    private RecyclerView mIncuList;
    private boolean FLAG=false;
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_incubators);

        if(!isNetworkAvailable())
            Toast.makeText(IncubatorsActivity.this,"No internet connection",Toast.LENGTH_LONG).show();
        mIncuList=(RecyclerView)findViewById(R.id.incubator_list);
        mIncuList.setHasFixedSize(false);
        mIncuList.setLayoutManager(new LinearLayoutManager(this));
        mDatabase= FirebaseDatabase.getInstance().getReference();
        mAuth=FirebaseAuth.getInstance();
        mAuthListener=new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                if(firebaseAuth.getCurrentUser() == null)
                {
                    startActivity(new Intent(IncubatorsActivity.this,LoginActivity.class));
                    finish();
                }
            }
        };
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main,menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseRecyclerAdapter<Incubator,IncuViewHolder> firebaseRecyclerAdapter=new FirebaseRecyclerAdapter<Incubator, IncuViewHolder>(Incubator.class,R.layout.incu_row,IncuViewHolder.class,mDatabase) {
            @Override
            protected void populateViewHolder(IncuViewHolder viewHolder, Incubator model, int position) {
                viewHolder.setFounder(model.getFounder());
                viewHolder.setEmail(model.getEmail());
                viewHolder.setContact(model.getContact());
                viewHolder.setName(model.getName());
                viewHolder.setImage(getApplicationContext(),model.getImage());
                viewHolder.setJoined(model.getJoined());
            }
        };
        mIncuList.setAdapter(firebaseRecyclerAdapter);
        mAuth.addAuthStateListener(mAuthListener);
    }
    public static class IncuViewHolder extends RecyclerView.ViewHolder
    {
        View mView;
        public IncuViewHolder(View itemView)
        {
            super(itemView);
            mView=itemView;
        }
        public void setName(String name)
        {
            TextView in_name=(TextView)mView.findViewById(R.id.name);
            in_name.setText(name);
        }
        public void setFounder(String founder)
        {
            TextView in_founder=(TextView)mView.findViewById(R.id.founder);
            in_founder.setText("Founder: "+founder);
        }
        public void setEmail(String email)
        {
            TextView in_email=(TextView)mView.findViewById(R.id.email);
            in_email.setText("Email: "+email);
        }
        public void setContact(String contact)
        {
            TextView in_contact=(TextView)mView.findViewById(R.id.contact);
            in_contact.setText("Contact: "+contact);
        }
        public void setImage(Context context,String image)
        {
            ImageView in_image=(ImageView)mView.findViewById(R.id.img);
            Picasso.with(context).load(image).into(in_image);
        }
        public void setJoined(String joined)
        {
            TextView in_joined=(TextView)mView.findViewById(R.id.joined);
            in_joined.setText("Joined: "+joined);
        }
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_logout)
        {
            mAuth.signOut();
            finish();
        }
        if(id == R.id.action_add)
        {
            startActivity(new Intent(IncubatorsActivity.this,AddIncubator.class));
            finish();
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        startActivity(new Intent(IncubatorsActivity.this,MainActivity.class));
        finish();
    }
    public void setVisibilty(View view)
    {
        final TextView textView=(TextView)findViewById(R.id.hide);
        textView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(FLAG==true)
                {
                    textView.setText("learn more");
                    TextView t=(TextView)findViewById(R.id.founder);
                    t.setVisibility(view.GONE);
                    t=(TextView)findViewById(R.id.email);
                    t.setVisibility(view.GONE);
                    t=(TextView)findViewById(R.id.contact);
                    t.setVisibility(view.GONE);
                    t=(TextView)findViewById(R.id.joined);
                    t.setVisibility(view.GONE);
                    Log.e("HELLO GUYS","LEARN MORE");
                    FLAG=false;
                }
                else
                {
                    textView.setText("show less");
                    TextView t=(TextView)findViewById(R.id.founder);
                    t.setVisibility(view.VISIBLE);
                    t=(TextView)findViewById(R.id.email);
                    t.setVisibility(view.VISIBLE);
                    t=(TextView)findViewById(R.id.contact);
                    t.setVisibility(view.VISIBLE);
                    t=(TextView)findViewById(R.id.joined);
                    t.setVisibility(view.VISIBLE);
                    Log.e("HELLO GUYS","SHOW LESS");
                    FLAG=true;
                }
            }
        });
    }
}
