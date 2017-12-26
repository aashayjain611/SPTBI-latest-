package com.example.android.sptbi;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.util.ArrayList;
import java.util.HashMap;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private FirebaseUser user;
    private Toolbar toolbar;
    private DatabaseReference mDatabase;
    private Menu mMenu;
    private TextView title;
    private android.support.v4.app.FragmentManager fragmentManager;
    private DatabaseReference mAdmin;
    private ArrayList<String> admin=new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("SP-TBI");
        toolbar.inflateMenu(R.menu.main);


        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        if(!isNetworkAvailable())
            Toast.makeText(MainActivity.this,"No internet connection",Toast.LENGTH_LONG).show();
        mAuth=FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference().child("Incubators");
        user=mAuth.getCurrentUser();

        Log.e("Current user",user.toString());
        if(user != null)
        {
            View hView =  navigationView.getHeaderView(0);
            TextView nav_user = (TextView)hView.findViewById(R.id.textView);
            nav_user.setText(user.getEmail());
        }
        mAuthListener=new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                if(firebaseAuth.getCurrentUser() == null)
                {
                    Intent intent=new Intent(MainActivity.this,LoginActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                }
            }
        };

        fragmentManager=getSupportFragmentManager();
        fragmentManager.beginTransaction().replace(R.id.app_bar_main,new IncubatorsFragment()).commit();

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

    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }
    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.

        getMenuInflater().inflate(R.menu.main, menu);
        mMenu=menu;
        System.out.println(admin);
        if(admin.contains(mAuth.getCurrentUser().getEmail().replace('.',','))) {
            MenuItem menuItem = menu.findItem(R.id.remove_user);
            menuItem.setVisible(true);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if(id == R.id.action_add)
        {
            startActivity(new Intent(MainActivity.this,AddIncubator.class));
        }
        if(id == R.id.my_profile)
        {
            Intent intent = new Intent(MainActivity.this,IncubatorDetails.class);
            intent.putExtra("UID",mAuth.getCurrentUser().getUid());
            startActivity(intent);
        }
        if(id == R.id.edit_profile)
        {
            startActivity(new Intent(MainActivity.this,EditProfileActivity.class));
        }
        if(id == R.id.action_delete)
        {
            //delete your incubator
            mDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if(dataSnapshot.hasChild(mAuth.getCurrentUser().getUid()))
                    {
                        new AlertDialog.Builder(MainActivity.this)
                                .setMessage("Are you sure you want to delete your incubator?")
                                .setCancelable(false)
                                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        mDatabase.child(mAuth.getCurrentUser().getUid()).removeValue();
                                    }
                                })
                                .setNegativeButton("No", null)
                                .show();
                    }
                    else
                    {
                        Toast.makeText(MainActivity.this,"Couldn't find your incubator",Toast.LENGTH_LONG).show();
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }
        if(id == R.id.remove_user)
        {
            startActivity(new Intent(MainActivity.this,RemoveUserActivity.class));
        }
        /*if(id == R.id.action_search)
        {
            toolbar.setBackgroundColor(Color.WHITE);
            toolbar.setNavigationIcon(null);
            MenuItem menuItem = mMenu.findItem(R.id.remove_user);
            menuItem.setVisible(false);
            menuItem = mMenu.findItem(R.id.action_add);
            menuItem.setVisible(false);
            menuItem = mMenu.findItem(R.id.action_delete);
            menuItem.setVisible(false);
            menuItem = mMenu.findItem(R.id.edit_profile);
            menuItem.setVisible(false);
            menuItem = mMenu.findItem(R.id.my_profile);
            menuItem.setVisible(false);
            title.setVisibility(View.GONE);
            final ImageView upButton=(ImageView)findViewById(R.id.upButton);
            upButton.setVisibility(View.VISIBLE);
            final EditText search=(EditText)findViewById(R.id.search);
            search.setVisibility(View.VISIBLE);
            final ImageView searchButton=(ImageView)findViewById(R.id.searchButton);
            searchButton.setVisibility(View.VISIBLE);
            searchButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    String searchIncu=search.getText().toString();
                    Bundle bundle=new Bundle();
                    bundle.putString("searchIncu",searchIncu);
                    Query searchQuery=mDatabase.orderByChild("Name").startAt(searchIncu).endAt(searchIncu + "\uf8ff");
                    Log.e("query",""+searchQuery);
                    FragmentManager fragmentManager1=getSupportFragmentManager();
                    fragmentManager1.beginTransaction().replace(R.id.app_bar_main,new SearchFragment()).commit();
                    SearchFragment fragment=new SearchFragment();
                    fragment.setArguments(bundle);
                }
            });
            upButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    toolbar.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
                    toolbar.setNavigationIcon(R.drawable.ic_menu_white_24dp);
                    MenuItem menuItem = mMenu.findItem(R.id.remove_user);
                    menuItem.setVisible(true);
                    menuItem = mMenu.findItem(R.id.action_add);
                    menuItem.setVisible(true);
                    menuItem = mMenu.findItem(R.id.action_delete);
                    menuItem.setVisible(true);
                    menuItem = mMenu.findItem(R.id.edit_profile);
                    menuItem.setVisible(true);
                    menuItem = mMenu.findItem(R.id.my_profile);
                    menuItem.setVisible(true);
                    upButton.setVisibility(View.GONE);
                    search.setVisibility(View.GONE);
                    searchButton.setVisibility(View.GONE);
                    search.setText("");
                    title.setVisibility(View.VISIBLE);
                }
            });
        }*/
        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();
        android.support.v4.app.FragmentManager fragmentManager=getSupportFragmentManager();
        if (id == R.id.nav_incubator)
        {
            fragmentManager.beginTransaction().replace(R.id.app_bar_main,new IncubatorsFragment()).commit();
            getSupportActionBar().setTitle("Incubators");
        }

        else if (id == R.id.nav_notification) {
            getSupportActionBar().setTitle("Notifications");
        }
        else if (id == R.id.nav_chats) {
            getSupportActionBar().setTitle("Chats");
        }
        else if (id == R.id.nav_agreement) {
            getSupportActionBar().setTitle("Agreements");
        }
        else if(id == R.id.nav_logout){
            mAuth.signOut();
            finish();
        }
        else if (id == R.id.nav_about) {
            getSupportActionBar().setTitle("About");
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    protected void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(mAuthListener);
    }
}
