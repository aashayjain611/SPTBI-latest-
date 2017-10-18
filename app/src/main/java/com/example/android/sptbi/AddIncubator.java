package com.example.android.sptbi;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;
import java.util.HashMap;

public class AddIncubator extends AppCompatActivity {

    private EditText inc_name,inc_fndr,inc_jfrm,inc_email,inc_contact;
    private ImageView inc_img,add_pic;
    private static int GALLERY_PICK=2;
    private Button save;
    private DatabaseReference mDatabase;
    private StorageReference mStorage;
    private ProgressDialog mProgress;
    private Uri downloadUrl,uri;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_incubator);
        initialize();
        if(!isNetworkAvailable())
            Toast.makeText(AddIncubator.this,"No internet connection",Toast.LENGTH_LONG).show();
        add_pic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent=new Intent(Intent.ACTION_PICK);
                intent.setType("image/*");
                startActivityForResult(intent,GALLERY_PICK);
            }
        });
        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try
                {
                    HashMap<String,String> dataMap=new HashMap<String,String>();
                    String name=inc_name.getText().toString().trim();
                    String email=inc_email.getText().toString().trim();
                    String fndr_name=inc_fndr.getText().toString().trim();
                    String jfrm=inc_jfrm.getText().toString().trim();
                    String contact=inc_contact.getText().toString().trim();
                    dataMap.put("Name",name);
                    dataMap.put("Founder",fndr_name);
                    dataMap.put("Joined",jfrm);
                    dataMap.put("Email",email);
                    dataMap.put("Contact",contact);
                    dataMap.put("Image",downloadUrl.toString());
                    mDatabase.child(name+jfrm).setValue(dataMap);
                    startActivity(new Intent(AddIncubator.this, IncubatorsActivity.class));
                    finish();
                }
                catch (NullPointerException e)
                {
                    Toast.makeText(AddIncubator.this,"Field(s) cannot be blank",Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==GALLERY_PICK && resultCode==RESULT_OK)
        {
            mProgress.setMessage("Uploading...");
            mProgress.show();
            uri=data.getData();
            StorageReference filepath=mStorage.child("Photos").child(uri.getLastPathSegment());
            filepath.putFile(uri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    mProgress.dismiss();
                    Toast.makeText(AddIncubator.this,"Upload done",Toast.LENGTH_SHORT).show();
                    downloadUrl=taskSnapshot.getDownloadUrl();
                    Picasso.with(AddIncubator.this).load(downloadUrl).into(inc_img);
                }
            });
        }
    }

    public void initialize()
    {
        inc_name=(EditText)findViewById(R.id.incu_name);
        inc_fndr=(EditText)findViewById(R.id.incu_fndr);
        inc_jfrm=(EditText)findViewById(R.id.incu_jfrm);
        inc_email=(EditText)findViewById(R.id.incu_email);
        inc_contact=(EditText)findViewById(R.id.incu_contact);
        inc_img=(ImageView)findViewById(R.id.incu_img);
        add_pic=(ImageView) findViewById(R.id.action_add);
        save=(Button)findViewById(R.id.save);
        mDatabase= FirebaseDatabase.getInstance().getReference();
        mStorage= FirebaseStorage.getInstance().getReference();
        mProgress=new ProgressDialog(this);
    }
    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        startActivity(new Intent(AddIncubator.this,IncubatorsActivity.class));
    }
}
