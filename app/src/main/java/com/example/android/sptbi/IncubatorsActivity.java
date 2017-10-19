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
    private boolean FLAG=true;
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
                final String uid=getRef(position).getKey();
                viewHolder.setName(model.getName());
                viewHolder.setImage(getApplicationContext(),model.getImage());
                viewHolder.mView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent details=new Intent(IncubatorsActivity.this,IncubatorDetails.class);
                        details.putExtra("UID",uid);
                        startActivity(details);
                    }
                });
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
        public void setImage(Context context,String image)
        {
            ImageView in_image=(ImageView)mView.findViewById(R.id.img);
            Picasso.with(context).load(image).into(in_image);
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

}
