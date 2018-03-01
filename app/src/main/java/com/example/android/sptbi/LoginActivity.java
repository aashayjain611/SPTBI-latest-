package com.example.android.sptbi;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputEditText;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

public class LoginActivity extends AppCompatActivity {

    private ImageView google_sign_in;
    private GoogleApiClient mGoogleApiClient;
    private int RC_SIGN_IN=2;
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private String TAG="LOGIN ACTIVITY";
    private Button create_acc;
    private TextView sign_in;
    private ProgressDialog mProgress;
    private DatabaseReference mDatabase;
    private HashMap<String,String> users=new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        if(!isNetworkAvailable())
            Toast.makeText(LoginActivity.this,"No internet connection",Toast.LENGTH_SHORT).show();

        sign_in=(TextView)findViewById(R.id.sign_in);
        sign_in.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(LoginActivity.this,SignInActivity.class));
                finish();
            }
        });
        create_acc=(Button)findViewById(R.id.create_acc);
        create_acc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startRegister();
            }
        });
        google_sign_in=(ImageView)findViewById(R.id.google);
        google_sign_in.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                signIn();
                Log.e("Track","1");
            }
        });

        Log.e("Track","2");
        // Configure Google Sign In
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        mGoogleApiClient=new GoogleApiClient.Builder(getApplicationContext())
                .enableAutoManage(this, new GoogleApiClient.OnConnectionFailedListener() {
                    @Override
                    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

                        Toast.makeText(LoginActivity.this,"Connection failed...",Toast.LENGTH_LONG).show();

                    }
                })
                .addApi(Auth.GOOGLE_SIGN_IN_API,gso)
                .build();

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
                System.out.println(k+"  "+user.get(k));
                users.put(k,user.get(k).toString());
            }
        }
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }
    private void startRegister() {

        try {
            TextInputEditText email = (TextInputEditText) findViewById(R.id.email_id_login);
            String email_id = email.getText().toString().trim();
            if (!isValidEmail(email_id))
                throw new IllegalArgumentException();
            TextInputEditText password = (TextInputEditText) findViewById(R.id.password_login);
            String password_login = password.getText().toString().trim();
            if (TextUtils.isEmpty(email_id) || TextUtils.isEmpty(password_login))
                throw new NullPointerException();

            mProgress.setMessage("Creating account...");
            mProgress.show();
            mProgress.setCanceledOnTouchOutside(false);
            mAuth.createUserWithEmailAndPassword(email_id, password_login).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if (task.isSuccessful()) {
                        users.put(mAuth.getCurrentUser().getEmail().replace('.',','),"added");
                        HashMap<String,String> user=new HashMap<>();
                        user.put(mAuth.getCurrentUser().getEmail().replace('.',','),"added");
                        mDatabase.child(mAuth.getCurrentUser().getUid()).setValue(user);
                        mProgress.dismiss();
                        onStart();
                    }
                    else {
                        mProgress.dismiss();
                        if (task.getException() instanceof FirebaseAuthUserCollisionException)
                        {
                            mDatabase.addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {

                                    if(dataSnapshot.child(mAuth.getCurrentUser().getUid()).child(mAuth.getCurrentUser().getEmail().replace('.',',')).getValue().equals("added"))
                                        Toast.makeText(LoginActivity.this,"User with this credentials already exists",Toast.LENGTH_LONG).show();
                                    else if(dataSnapshot.child(mAuth.getCurrentUser().getUid()).child(mAuth.getCurrentUser().getEmail().replace('.',',')).getValue().equals("removed"))
                                        Toast.makeText(LoginActivity.this,"User is removed",Toast.LENGTH_LONG).show();
                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {

                                }
                            });
                        }
                    }
                }
            });
        }
        catch(NullPointerException e)
        {
            Toast.makeText(LoginActivity.this,"Field(s) cannot be blank",Toast.LENGTH_LONG).show();
        }
        catch (IllegalArgumentException iae)
        {
            Toast.makeText(LoginActivity.this,"Invalid Email-ID",Toast.LENGTH_LONG).show();
        }
    }

    private void signIn() {
        Log.e("Track","3");
        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        Log.e("Track","4");

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            if (result.isSuccess()) {
                mProgress.setTitle("Logging in");
                mProgress.setMessage("Please wait...");
                mProgress.show();
                mProgress.setCanceledOnTouchOutside(false);
                // Google Sign In was successful, authenticate with Firebase
                Log.e("Track","5");
                GoogleSignInAccount account = result.getSignInAccount();
                firebaseAuthWithGoogle(account);
            } else {
                // Google Sign In failed, update UI appropriately
                // ...
            }
        }
    }

    public final boolean isValidEmail(CharSequence target) {
        if (android.util.Patterns.EMAIL_ADDRESS.matcher(target).matches())
            return true;

        return false;
    }

    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.

        mAuth=FirebaseAuth.getInstance();
        mDatabase= FirebaseDatabase.getInstance().getReference().child("Users");

        mAuthListener=new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull final FirebaseAuth firebaseAuth) {
                if(firebaseAuth.getCurrentUser() != null)
                {
                    mDatabase.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            collectUsers((HashMap<String,Object>)dataSnapshot.getValue());
                            System.out.println(users);
                            System.out.println(firebaseAuth.getCurrentUser());
                            if(firebaseAuth.getCurrentUser()!=null && users!=null)
                            {
                                if(!users.containsKey(firebaseAuth.getCurrentUser().getEmail().replace('.',',')))
                                {
                                    users.put(firebaseAuth.getCurrentUser().getEmail().replace('.',','),"added");
                                    HashMap<String,String> user=new HashMap<>();
                                    user.put(mAuth.getCurrentUser().getEmail().replace('.',','),"added");
                                    mDatabase.child(mAuth.getCurrentUser().getUid()).setValue(user);
                                }
                                if(users.get(firebaseAuth.getCurrentUser().getEmail().replace('.',',')).equals("added"))
                                {
                                    Intent intent = new Intent(LoginActivity.this,MainActivity.class);
                                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                    startActivity(intent);
                                    finish();
                                }
                                else if(users.get(firebaseAuth.getCurrentUser().getEmail().replace('.',',')).equals("removed"))
                                {
                                    Toast.makeText(LoginActivity.this,"User is removed",Toast.LENGTH_LONG).show();
                                    mAuth.signOut();
                                }
                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });

                }
            }
        };
        mAuth.addAuthStateListener(mAuthListener);
    }
    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {

        Log.d(TAG, "firebaseAuthWithGoogle:" + acct.getId());

        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        mAuth.signInWithCredential(credential).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {

                    Log.e("Track","6");
                    // Sign in success, update UI with the signed-in user's information
                    Log.d(TAG, "signInWithCredential:success");
                    if(!users.isEmpty() && !users.containsKey(mAuth.getCurrentUser().getEmail().replace('.',',')))
                    {
                        Log.e("REMOVE",""+users);
                        users.put(mAuth.getCurrentUser().getEmail().replace('.',','),"added");
                        HashMap<String,String> user=new HashMap<>();
                        user.put(mAuth.getCurrentUser().getEmail().replace('.',','),"added");
                        mDatabase.child(mAuth.getCurrentUser().getUid()).setValue(user);
                    }
                    mProgress.dismiss();
                    onStart();
                }
                else if(users.containsKey(mAuth.getCurrentUser().getEmail().replace('.',',')))
                {
                    if(users.get(mAuth.getCurrentUser().getEmail().replace('.',',')).equals("removed"))
                        Toast.makeText(LoginActivity.this, "User is removed", Toast.LENGTH_SHORT).show();
                    Log.e("REMOVE",""+users);
                }
                else {
                    // If sign in fails, display a message to the user.
                    mProgress.dismiss();
                    Log.w(TAG, "signInWithCredential:failure", task.getException());
                    Toast.makeText(LoginActivity.this, "Authentication failed.",Toast.LENGTH_SHORT).show();
                }
                // ...
            }
        });
    }

    @Override
    public void onBackPressed() {
        new AlertDialog.Builder(this)
                .setMessage("Are you sure you want to exit?")
                .setCancelable(false)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        finish();
                    }
                })
                .setNegativeButton("No", null)
                .show();
    }
}
