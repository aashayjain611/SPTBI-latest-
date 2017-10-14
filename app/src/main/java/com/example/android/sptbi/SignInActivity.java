package com.example.android.sptbi;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class SignInActivity extends AppCompatActivity {

    private EditText email;
    private EditText password;
    private Button sign_in;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);

        mAuth=FirebaseAuth.getInstance();
        email=(EditText)findViewById(R.id.email);
        password=(EditText)findViewById(R.id.password);
        sign_in=(Button)findViewById(R.id.sign_in);
        sign_in.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                signIn();
            }
        });
    }

    private void signIn() {

        String email_id=email.getText().toString().trim();
        String user_password=password.getText().toString().trim();
        if(!TextUtils.isEmpty(email_id) && !TextUtils.isEmpty(user_password))
            mAuth.signInWithEmailAndPassword(email_id,user_password).addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                @Override
                public void onSuccess(AuthResult authResult) {
                    startActivity(new Intent(SignInActivity.this,MainActivity.class));
                }
            });

    }
}
