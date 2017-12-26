package com.example.android.sptbi;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.ListView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;

public class RemoveUserActivity extends AppCompatActivity {

    private HashMap<String,Object> userWithUid;
    private DatabaseReference mUser;
    private DatabaseReference mIncu;
    private DatabaseReference mAdmin;
    private ArrayList<String> admin=new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_remove_user);

        android.support.v7.widget.Toolbar toolbar = (android.support.v7.widget.Toolbar) findViewById(R.id.remove_user_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Remove user");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        final ArrayList<User> userList=new ArrayList<>();
        mAdmin=FirebaseDatabase.getInstance().getReference().child("Admin");
        mAdmin.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                HashMap<String,String> adminMap=(HashMap<String,String>)dataSnapshot.getValue();
                ArrayList<String> keyArrayList=new ArrayList<>(adminMap.keySet());
                System.out.println(keyArrayList);
                for(String keyAdmin:keyArrayList)
                {
                    admin.add(adminMap.get(keyAdmin).replace('.',','));
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        mUser= FirebaseDatabase.getInstance().getReference().child("Users");
        mUser.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                userWithUid=(HashMap<String,Object>)dataSnapshot.getValue();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        mIncu=FirebaseDatabase.getInstance().getReference().child("Incubators");
        mIncu.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                ArrayList<String> keyset=new ArrayList<>(userWithUid.keySet());
                System.out.println(keyset);
                for(String key:keyset)
                {
                    HashMap singleUser=(HashMap)userWithUid.get(key);
                    Object[] emailKey=singleUser.keySet().toArray();
                    String email=(String)emailKey[0];

                    Log.e("Hello","Almost there");

                    if(singleUser.get(email).equals("added"))
                    {
                        if(dataSnapshot.hasChild(key))
                        {
                            if(!admin.contains(email))
                                userList.add(new User(key,(String)dataSnapshot.child(key).child("Name").getValue()));
                        }
                    }
                }
                UserAdapter userAdapter=new UserAdapter(RemoveUserActivity.this,userList);
                ListView listView=(ListView)findViewById(R.id.user_list);
                listView.setAdapter(userAdapter);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }
}
