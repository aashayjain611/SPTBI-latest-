package com.example.android.sptbi;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputEditText;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.Calendar;
import java.util.HashMap;
import id.zelory.compressor.Compressor;

public class AddIncubator extends AppCompatActivity {

    private TextInputEditText inc_name,inc_fndr,inc_email,inc_contact;
    private static TextView inc_jfrm;
    private ImageView inc_img,add_pic;
    private static int GALLERY_PICK=2;
    private Button save;
    private DatabaseReference mDatabase;
    private StorageReference mStorage;
    private ProgressDialog mProgress;
    private Uri downloadUrl,uri;
    private static String date;
    private FirebaseAuth mAuth;
    private Toolbar toolbar;
    private Uri thumb_uri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_incubator);


        initialize();
        toolbar=(Toolbar)findViewById(R.id.add_incubator_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Add Incubator");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        final String uid=mAuth.getCurrentUser().getUid();
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
                    mProgress.setTitle("Uploading");
                    mProgress.setMessage("Please wait...");
                    mProgress.setCanceledOnTouchOutside(false);
                    mProgress.show();
                    HashMap<String,String> dataMap=new HashMap<String,String>();
                    String name=inc_name.getText().toString().trim();
                    String email=inc_email.getText().toString().trim();
                    if(!isValidEmail(email))
                        throw new IllegalArgumentException();
                    String fndr_name=inc_fndr.getText().toString().trim();
                    String jfrm=inc_jfrm.getText().toString().trim();
                    if(TextUtils.isEmpty(jfrm))
                        throw new NullPointerException();
                    String contact=inc_contact.getText().toString().trim();
                    if(!validContact(contact))
                        throw new ArrayIndexOutOfBoundsException();
                    dataMap.put("Name",name);
                    dataMap.put("Founder",fndr_name);
                    dataMap.put("Joined",jfrm);
                    dataMap.put("Email",email);
                    dataMap.put("Contact",contact);
                    Log.e("thumbnail ",thumb_uri.toString());
                    dataMap.put("Image",thumb_uri.toString());
                    dataMap.put("UID",uid);
                    mDatabase.child(uid).setValue(dataMap).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            mProgress.dismiss();
                        }
                    });
                    startActivity(new Intent(AddIncubator.this,MainActivity.class));
                    finish();
                }
                catch (NullPointerException e)
                {
                    Toast.makeText(AddIncubator.this,"Field(s) cannot be blank",Toast.LENGTH_LONG).show();
                }
                catch (IllegalArgumentException iae)
                {
                    Toast.makeText(AddIncubator.this,"Invalid Email-ID",Toast.LENGTH_LONG).show();
                }
                catch (ArrayIndexOutOfBoundsException aiobe)
                {
                    Toast.makeText(AddIncubator.this,"Invalid contact",Toast.LENGTH_LONG).show();
                }
            }
        });
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==GALLERY_PICK && resultCode==RESULT_OK)
        {
            uri=data.getData();
            CropImage.activity(uri)
                    .setAspectRatio(1,1)
                    .setMinCropWindowSize(500,500)
                    .start(this);

        }
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                ProgressBar progressBar=new ProgressBar(this);
                progressBar=(ProgressBar)findViewById(R.id.CropProgressBar);
                progressBar.setVisibility(View.VISIBLE);
                Uri resultUri = result.getUri();
                downloadUrl=resultUri;
                Bitmap thumb_bitmap=null;

                File thumb_pathfile=new File(resultUri.getPath());
                try {
                    thumb_bitmap= new Compressor(this)
                            .setMaxHeight(200)
                            .setMaxWidth(200)
                            .setQuality(75)
                            .compressToBitmap(thumb_pathfile);
                } catch (IOException e) {
                    e.printStackTrace();
                }

                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                thumb_bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                final byte[] thumb_byte = baos.toByteArray();

                final StorageReference thumb_filepath=mStorage.child("thumbnails").child(mAuth.getCurrentUser().getUid().toString());

                StorageReference filepath=mStorage.child("Photos").child(mAuth.getCurrentUser().getUid().toString());
                filepath.putFile(resultUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                        if(task.isSuccessful())
                        {
                            downloadUrl=task.getResult().getDownloadUrl();

                            UploadTask uploadTask=thumb_filepath.putBytes(thumb_byte);
                            uploadTask.addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                                @Override
                                public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> thumb_task) {
                                    if(thumb_task.isSuccessful())
                                    {
                                        mProgress.dismiss();
                                        thumb_uri=thumb_task.getResult().getDownloadUrl();
                                        Log.e("thumbnail ",thumb_uri.toString());
                                        Picasso.with(AddIncubator.this).load(thumb_uri).into(inc_img);
                                    }
                                }
                            });
                        }
                    }
                });
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
            }
        }
    }


    public void initialize()
    {
        inc_name=(TextInputEditText) findViewById(R.id.incu_name);
        inc_fndr=(TextInputEditText)findViewById(R.id.incu_fndr);
        inc_jfrm=(TextView)findViewById(R.id.incu_jfrm);
        inc_email=(TextInputEditText)findViewById(R.id.incu_email);
        inc_contact=(TextInputEditText)findViewById(R.id.incu_contact);
        inc_img=(ImageView)findViewById(R.id.incu_img);
        add_pic=(ImageView) findViewById(R.id.action_add);
        save=(Button)findViewById(R.id.save);
        mDatabase= FirebaseDatabase.getInstance().getReference().child("Incubators");
        mDatabase.keepSynced(true);
        mStorage= FirebaseStorage.getInstance().getReference();
        mProgress=new ProgressDialog(this);
        mAuth=FirebaseAuth.getInstance();
    }
    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    public void showDatePickerDialog(View v) {
        DialogFragment newFragment = new DatePickerFragment();
        newFragment.show(getSupportFragmentManager(), "datePicker");
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }
    public static class DatePickerFragment extends DialogFragment
            implements DatePickerDialog.OnDateSetListener {

        private int month,year,day;

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState)
        {
            // Use the current date as the default date in the picker
            final Calendar c = Calendar.getInstance();
            year = c.get(Calendar.YEAR);
            month = c.get(Calendar.MONTH);
            day = c.get(Calendar.DAY_OF_MONTH);

            // Create a new instance of DatePickerDialog and return it
            return new DatePickerDialog(getActivity(), this, year, month, day);
        }

        public void onDateSet(DatePicker view, int year, int month, int day) {
            // Do something with the date chosen by the user

            date=day+"-"+(month+1)+"-"+year;
            setDate();
        }
    }

    public static void setDate() {
        inc_jfrm.setText(date);
    }
    public final boolean isValidEmail(CharSequence target) {
        if (android.util.Patterns.EMAIL_ADDRESS.matcher(target).matches())
            return true;
        return false;
    }
    public boolean validContact(String number)
    {
        if(number.length()==10)
            return true ;
        return false;
    }
}
