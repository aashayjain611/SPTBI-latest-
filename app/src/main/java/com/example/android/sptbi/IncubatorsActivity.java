package com.example.android.sptbi;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.Picasso;

public class IncubatorsActivity extends AppCompatActivity {

    private DatabaseReference mDatabase;
    private RecyclerView mIncuList;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_incubators);

        mIncuList=(RecyclerView)findViewById(R.id.incubator_list);
        mIncuList.setHasFixedSize(true);
        mIncuList.setLayoutManager(new LinearLayoutManager(this));
        mDatabase= FirebaseDatabase.getInstance().getReference();
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
            }
        };
        mIncuList.setAdapter(firebaseRecyclerAdapter);
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
            in_founder.setText(founder);
        }
        public void setEmail(String email)
        {
            TextView in_email=(TextView)mView.findViewById(R.id.email);
            in_email.setText(email);
        }
        public void setContact(String contact)
        {
            TextView in_contact=(TextView)mView.findViewById(R.id.contact);
            in_contact.setText(contact);
        }
        public void setImage(Context context,String image)
        {
            ImageView in_image=(ImageView)mView.findViewById(R.id.img);
            Picasso.with(context).load(image).into(in_image);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId()==R.id.action_add)
            startActivity(new Intent(IncubatorsActivity.this,AddIncubator.class));
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        startActivity(new Intent(IncubatorsActivity.this,MainActivity.class));
    }

}
