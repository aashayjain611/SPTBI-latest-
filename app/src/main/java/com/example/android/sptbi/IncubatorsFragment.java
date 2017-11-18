package com.example.android.sptbi;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.ListFragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

public class IncubatorsFragment extends ListFragment {

    private DatabaseReference mDatabase;
    private RecyclerView mIncuList;
    View myView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstance){
        myView = inflater.inflate(R.layout.activity_incubators,container,false);
        return myView;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
    }
    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState)
    {
        Toolbar toolbar=(Toolbar)myView.findViewById(R.id.toolbar);
        toolbar.inflateMenu(R.menu.main);
        if(!isNetworkAvailable())
            Toast.makeText(getContext(),"No internet connection",Toast.LENGTH_LONG).show();
        mIncuList=(RecyclerView)getView().findViewById(R.id.incubator_list);
        mIncuList.setHasFixedSize(false);
        mIncuList.setLayoutManager(new LinearLayoutManager(getContext()));
        mDatabase= FirebaseDatabase.getInstance().getReference();
        mDatabase.keepSynced(true);
    }

    @Override
    public void onStart() {
        super.onStart();
        FirebaseRecyclerAdapter<Incubator,IncuViewHolder> firebaseRecyclerAdapter=new FirebaseRecyclerAdapter<Incubator, IncuViewHolder>(Incubator.class,R.layout.incu_row,IncuViewHolder.class,mDatabase) {


            @Override
            protected void populateViewHolder(IncuViewHolder viewHolder, Incubator model, int position) {
                final String uid=getRef(position).getKey();
                viewHolder.setName(model.getName());
                viewHolder.setFounder(model.getFounder());
                viewHolder.setEmail(model.getEmail());
                viewHolder.setContact(model.getContact());
                viewHolder.setImage(getContext(),model.getImage());
                viewHolder.mView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent details=new Intent(getContext(),IncubatorDetails.class);
                        details.putExtra("UID",uid);
                        startActivity(details);
                    }
                });
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
        public void setFounder(String founder)
        {
            TextView in_name=(TextView)mView.findViewById(R.id.founder);
            in_name.setText(founder);
        }
        public void setName(String name)
        {
            TextView in_name=(TextView)mView.findViewById(R.id.name);
            in_name.setText(name);
        }
        public void setEmail(String email)
        {
            TextView in_name=(TextView)mView.findViewById(R.id.email);
            in_name.setText(email);
        }
        public void setContact(String contact)
        {
            TextView in_name=(TextView)mView.findViewById(R.id.contact);
            in_name.setText(contact);
        }
        public void setImage(final Context context, final String image)
        {
            final ImageView in_image=(ImageView)mView.findViewById(R.id.img);
            Picasso.with(context).load(image).networkPolicy(NetworkPolicy.OFFLINE).into(in_image, new Callback() {
                @Override
                public void onSuccess() {
                    in_image.setBackgroundColor(Color.parseColor("#ffffff"));
                }

                @Override
                public void onError() {

                    Picasso.with(context).load(image).into(in_image);

                }
            });
        }
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager = (ConnectivityManager)getContext(). getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {


        switch (item.getItemId())
        {
            // Respond to the action bar's Up/Home button
            case R.id.action_add:
                startActivity(new Intent(getContext(),AddIncubator.class));
                Log.e("hello","adding");
                break;

            default:
                return false;
        }

        return super.onOptionsItemSelected(item);
    }
}
