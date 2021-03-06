package com.example.sociochat;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.provider.DocumentsContract;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.viewpager.widget.ViewPager;

import com.example.sociochat.ChatsFragment;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;

public class MainActivity extends AppCompatActivity {

    private Toolbar mToolbar;
    //private ViewPager myViewPager;
    //private TabLayout myTabLayout;
    //private TabAccessorAdapter myTabAccessorAdapter;


    private FirebaseAuth mAuth;
    private DatabaseReference RootRef;



    private BottomNavigationView navView;
    // private  AppBarConfiguration appBarConfiguration;
    //private NavController navController;

    private ChatsFragment chatsFragment;
    private ContactsFragment contactsFragment;
    private GroupsFragment groupsFragment;
    private RequestsFragment requestsFragment;
    private static MainActivity instance;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        instance=this;

        chatsFragment = new ChatsFragment();
        contactsFragment = new ContactsFragment();
        groupsFragment = new GroupsFragment();
        requestsFragment = new RequestsFragment();

        navView = findViewById(R.id.nav_view);


        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        //appBarConfiguration = new AppBarConfiguration.Builder(
        //      R.id.navigation_chats, R.id.navigation_groups, R.id.navigation_contacts,R.id.navigation_request)
        //      .build();
        //navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        // NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
        //NavigationUI.setupWithNavController(navView, navController);*/



        mAuth = FirebaseAuth.getInstance();

        RootRef = FirebaseDatabase.getInstance().getReference();

        mToolbar=findViewById(R.id.main_page_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Sociochat");

        navView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {

                switch(menuItem.getItemId())
                {
                    case R.id.navigation_chats :
                        InitializeFragments(chatsFragment);
                        return true;

                    case R.id.navigation_groups :
                        InitializeFragments(groupsFragment);
                        return true;

                    case R.id.navigation_contacts :
                        InitializeFragments(contactsFragment);
                        return true;

                    case R.id.navigation_request :
                        InitializeFragments(requestsFragment);
                        return true;
                }
                return false;
            }
        });

        /*myViewPager= findViewById(R.id.main_tabs_pager);
        myTabAccessorAdapter=new TabAccessorAdapter(getSupportFragmentManager());
        myViewPager.setAdapter(myTabAccessorAdapter);

        myTabLayout=(TabLayout) findViewById(R.id.main_tabs);
        myTabLayout.setupWithViewPager(myViewPager);*/





    }
    public static MainActivity getInstance()
    {
        return instance;
    }


    private void InitializeFragments(Fragment fragment)
    {

        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.nav_host_fragment,fragment);
        fragmentTransaction.commit();
    }

    @Override
    protected void onStart() {
        super.onStart();

        FirebaseUser currentuser = mAuth.getCurrentUser();

        if (currentuser == null)
        {

            SendUserToLoginActivity();
        }
        else
        {

            updateUserStatus("online");

            VerifyUserExistance();
            InitializeFragments(chatsFragment);
        }
    }


   /* @Override
    protected void onStop()
    {
        super.onStop();

        FirebaseUser currentuser = mAuth.getCurrentUser();

        if(currentuser != null)
        {
            updateUserStatus("offline");
        }
    }*/


    @Override
    protected void onDestroy()
    {
        super.onDestroy();

        FirebaseUser currentuser = mAuth.getCurrentUser();

        if(currentuser != null)
        {
            updateUserStatus("offline");
        }

    }

    private void VerifyUserExistance() {

        String currentuserID = mAuth.getCurrentUser().getUid();
        RootRef.child("Users").child(currentuserID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                if(dataSnapshot.child("name").exists()){

                    Toast.makeText(MainActivity.this,"Welcome",Toast.LENGTH_SHORT).show();
                }
                else
                {
                    SendUserToSettingsActivity();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.options_menu,menu);
        return true;


    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        super.onOptionsItemSelected(item);

        if(item.getItemId() == R.id.main_logout_option)
        {

            updateUserStatus("offline");
            mAuth.signOut();
            SendUserToLoginActivity();
        }
        if(item.getItemId() == R.id.main_settings_option)
        {

            SendUserToSettingsActivity();

        }
        if(item.getItemId() == R.id.main_create_group_option)
        {
            RequestNewGroup();
        }
        if(item.getItemId() == R.id.main_find_friends_option)
        {
            SendUserToFindFriendsActivity();
        }

        return true;
    }

    private void RequestNewGroup()
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this,R.style.AlertDialog);
        builder.setTitle("Enter the Group Name: ");

        final EditText groupNmaeField = new EditText(MainActivity.this);
        groupNmaeField.setHint("e.g My Group");
        builder.setView(groupNmaeField);

        builder.setPositiveButton("Create", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which)
            {

                String groupName = groupNmaeField.getText().toString();

                if(TextUtils.isEmpty(groupName))
                {

                    Toast.makeText(MainActivity.this,"Plaese Enter the Group Name",Toast.LENGTH_SHORT).show();

                }
                else
                {
                    CreateNewGroup(groupName);
                }

            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int which)
            {

                dialogInterface.cancel();

            }
        });
        builder.show();
    }

    private void CreateNewGroup(final String groupName)
    {
        RootRef.child("Groups").child(groupName).setValue("")
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {

                        if(task.isSuccessful())
                        {
                            Toast.makeText(MainActivity.this,groupName+" group created Successfully",Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void SendUserToLoginActivity() {

        Intent loginIntent = new Intent(MainActivity.this,PhoneLoginActivity.class);
        loginIntent.addFlags((Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
        startActivity(loginIntent);
        finish();
    }

    private void SendUserToSettingsActivity() {

        Intent settingsIntent = new Intent(MainActivity.this,SettingsActivity.class);

        startActivity(settingsIntent);

    }

    private void SendUserToFindFriendsActivity()
    {

        Intent FindFriendIntent = new Intent(MainActivity.this,FindFriendsActivity.class);
        startActivity(FindFriendIntent);

    }

    public  void updateUserStatus(String state)
    {
        String saveCurrentTime,saveCurrentDate,currentUserID;

        Calendar calendar = Calendar.getInstance();

        SimpleDateFormat currentDate = new SimpleDateFormat("MMM dd,yyyy");
        saveCurrentDate = currentDate.format(calendar.getTime());

        SimpleDateFormat currentTime = new SimpleDateFormat("hh:mm a");
        saveCurrentTime = currentTime.format(calendar.getTime());

        HashMap<String,Object> onlineStateMap = new HashMap<>();
        onlineStateMap.put("time",saveCurrentTime);
        onlineStateMap.put("date",saveCurrentDate);
        onlineStateMap.put("state",state);



        currentUserID = mAuth.getCurrentUser().getUid();

        RootRef.child("Users").child(currentUserID).child("userState")
                .updateChildren(onlineStateMap);


    }


}
