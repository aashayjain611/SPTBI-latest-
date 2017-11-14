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
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class SignInActivity extends AppCompatActivity {

    private TextInputEditText email;
    private TextInputEditText password;
    private Button sign_in;
    private FirebaseAuth mAuth;
    private ProgressDialog mProgress;
    private Toolbar toolbar;

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
        mAuth=FirebaseAuth.getInstance();
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
            mProgress.setTitle("Signing in");
            mProgress.setMessage("Please wait...");
            mProgress.show();
            mProgress.setCanceledOnTouchOutside(false);
            mAuth.signInWithEmailAndPassword(email_id,user_password).addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                @Override
                public void onSuccess(AuthResult authResult) {
                    mProgress.dismiss();
                    startActivity(new Intent(SignInActivity.this,MainActivity.class));
                    finish();
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    mProgress.dismiss();
                    Toast.makeText(SignInActivity.this,"Incorrect password",Toast.LENGTH_LONG).show();
                }
            });
        }
        catch (NullPointerException e)
        {
            Toast.makeText(SignInActivity.this,"Field(s) cannot be blank",Toast.LENGTH_LONG).show();
        }
    }
}
