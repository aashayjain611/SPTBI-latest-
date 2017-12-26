package com.example.android.sptbi;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputEditText;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

public class SignInActivity extends AppCompatActivity {

    private TextInputEditText email;
    private TextInputEditText password;
    private Button sign_in;
    private FirebaseAuth mAuth;
    private ProgressDialog mProgress;
    private Toolbar toolbar;
    private DatabaseReference mDatabase;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private HashMap<String,String> users=new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);

        toolbar=(Toolbar)findViewById(R.id.sign_in_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Sign In");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        if(!isNetworkAvailable())
            Toast.makeText(SignInActivity.this,"No internet connection",Toast.LENGTH_LONG).show();


        email=(TextInputEditText)findViewById(R.id.email);
        password=(TextInputEditText)findViewById(R.id.password);
        sign_in=(Button)findViewById(R.id.sign_in);
        sign_in.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                signIn();
            }
        });
        mProgress=new ProgressDialog(this);
    }

    private void collectUsers(HashMap<String, Object> value)
    {
        Set mapSet = (Set) value.entrySet();
        Iterator mapIterator = mapSet.iterator();
        while (mapIterator.hasNext())
        {
            HashMap.Entry mapEntry = (HashMap.Entry) mapIterator.next();
            HashMap user=(HashMap)mapEntry.getValue();
            for(Object key:user.keySet())
            {
                String k=(String)key;
                users.put(k,user.get(key).toString());
            }
        }

    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }
    private void signIn() {

        try
        {
            String email_id=email.getText().toString().trim();
            String user_password=password.getText().toString().trim();
            if(TextUtils.isEmpty(email_id) || TextUtils.isEmpty(user_password))
                throw new NullPointerException();
            mProgress.setTitle("Logging in");
            mProgress.setMessage("Please wait...");
            mProgress.show();
            mProgress.setCanceledOnTouchOutside(false);
            mAuth.signInWithEmailAndPassword(email_id,user_password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if(task.isSuccessful())
                    {
                        if(users.containsKey(mAuth.getCurrentUser().getEmail().replace('.',',')))
                        {
                            if(users.get(mAuth.getCurrentUser().getEmail().replace('.',',')).equals("added"))
                                onStart();
                            else if(mAuth.getCurrentUser().getEmail().replace('.',',').equals("removed"))
                                Toast.makeText(SignInActivity.this,"User is removed",Toast.LENGTH_LONG).show();
                        }
                    }
                    else
                        Toast.makeText(SignInActivity.this,"Check your credentials",Toast.LENGTH_LONG).show();
                }
            });
        }
        catch (NullPointerException e)
        {
            Toast.makeText(SignInActivity.this,"Field(s) cannot be blank",Toast.LENGTH_LONG).show();
            mAuth.signOut();
        }
    }
    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.

        mDatabase = FirebaseDatabase.getInstance().getReference().child("Users");
        mAuth=FirebaseAuth.getInstance();
        mAuthListener=new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull final FirebaseAuth firebaseAuth) {
                mDatabase.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                        collectUsers((HashMap<String,Object>)dataSnapshot.getValue());
                        if(firebaseAuth.getCurrentUser() != null)
                        {
                            Intent intent = new Intent(SignInActivity.this,MainActivity.class);
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(intent);
                            finish();
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
            }
        };

        mAuth.addAuthStateListener(mAuthListener);
    }
}
