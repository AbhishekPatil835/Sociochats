package com.example.sociochat;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.DocumentsContract;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.widget.Toolbar;

import com.google.android.gms.auth.api.signin.internal.Storage;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.File;
import java.util.HashMap;
import java.util.UUID;

import de.hdodenhof.circleimageview.CircleImageView;

public class SettingsActivity extends AppCompatActivity {

    private Button UpdateAccountSettings,GotoMaps;
    private EditText userName,userStatus;
    private CircleImageView userProfileImage;
    private  String currentuserID;
    private FirebaseAuth mAuth;
    private DatabaseReference RootRef;
    private static final int GalleryPick = 1;
    private StorageReference UserProfileImagesRef;
    private ProgressDialog loadingBar;
    private String downloadUrl;
    HashMap<String,Object> ProfileMap = new HashMap<>();
    private Toolbar SettingsToolbar;



    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        mAuth = FirebaseAuth.getInstance();
        currentuserID = mAuth.getCurrentUser().getUid();
        RootRef = FirebaseDatabase.getInstance().getReference();
        UserProfileImagesRef = FirebaseStorage.getInstance().getReference().child("Profile Images");

        Initializefields();



        UpdateAccountSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                UpdateSettings();

            }
        });

        GotoMaps.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                GotoMapsActivity();
            }
        });



        RetriveUserInfo();

        userProfileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                Intent galleryIntent = new Intent();
                galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
                galleryIntent.setType("image/*");
                startActivityForResult(galleryIntent,GalleryPick);

            }
        });
    }




    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == GalleryPick && resultCode == RESULT_OK && data != null)
        {
            Uri ImageUri = data.getData();

            CropImage.activity()
                    .setGuidelines(CropImageView.Guidelines.ON)
                    .setAspectRatio(1,1)
                    .start(this);

        }

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE)
        {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);

            if(resultCode == RESULT_OK)
            {
                loadingBar.setTitle("Set Profile image");
                loadingBar.setMessage("Please wait, your image is uploading...");
                loadingBar.setCanceledOnTouchOutside(false);
                loadingBar.show();

                final Uri resultUri = result.getUri();

                final StorageReference filePath = UserProfileImagesRef.child(currentuserID + ".jpg");

                filePath.putFile(resultUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(@NonNull UploadTask.TaskSnapshot taskSnapshot)
                    {

                      //  if(task.isSuccessful())
                      //  {

                        filePath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>()
                        {
                            @Override
                            public void onSuccess(Uri uri)
                            {

                                downloadUrl = uri.toString();

                                //ProfileMap.put("image",String.valueOf(uri));

                                RootRef.child("Users").child(currentuserID).child("image").setValue(downloadUrl).addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {

                                        Toast.makeText(SettingsActivity.this, "Finally complete", Toast.LENGTH_SHORT).show();
                                        loadingBar.dismiss();
                                    }
                                });
                            }
                        });
                        /* filePath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {


                                    downloadUrl = uri.toString();
                                    //This is your image url do whatever you want with it.
                                }
                            });*/

                        Toast.makeText(SettingsActivity.this, "Profile Image Uploaded Successfully", Toast.LENGTH_SHORT).show();
                    }
                });

                        /*final String downloadUrl = task.getResult().getStorage().getDownloadUrl().toString();

                            RootRef.child("Users").child(currentuserID).child("image")
                                    .setValue(downloadUrl)
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task)
                                        {
                                              if(task.isSuccessful())
                                              {
                                                  Toast.makeText(SettingsActivity.this, "Image saved in database successfully", Toast.LENGTH_SHORT).show();
                                                  loadingBar.dismiss();
                                              }
                                              else
                                              {
                                                  String message = task.getException().toString();
                                                  Toast.makeText(SettingsActivity.this, "Error " + message, Toast.LENGTH_SHORT).show();

                                                  loadingBar.dismiss();
                                              }
                                        }
                                    });*/

                       // }
                       /* else
                        {
                            String message = task.getException().toString();
                            Toast.makeText(SettingsActivity.this, "Error: " + message, Toast.LENGTH_SHORT).show();

                            loadingBar.dismiss();
                        }*/

            }

        }

    }

    private void UpdateSettings() {

        String setUsername = userName.getText().toString();
        String setStatus = userStatus.getText().toString();

        if(TextUtils.isEmpty(setUsername)){

            Toast.makeText(this,"Please write your Username...",Toast.LENGTH_SHORT).show();

        }
        if(TextUtils.isEmpty(setUsername)){

            Toast.makeText(this,"Please write your Status...",Toast.LENGTH_SHORT).show();

        }
        else
        {


            ProfileMap.put("uid",currentuserID);
            ProfileMap.put("name",setUsername);
            ProfileMap.put("Status",setStatus);
           // ProfileMap.put("image",downloadUrl);
            RootRef.child("Users").child(currentuserID).updateChildren(ProfileMap)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {

                            if(task.isSuccessful()){
                                SendUserToMainActivity();
                                Toast.makeText(SettingsActivity.this,"Profile Updated Successfully",Toast.LENGTH_SHORT).show();
                            }
                            else
                            {
                                String message = task.getException().toString();
                                Toast.makeText(SettingsActivity.this,"Error: "+message,Toast.LENGTH_SHORT).show();

                            }

                        }
                    });

        }

    }

    private void RetriveUserInfo() {

        RootRef.child("Users").child(currentuserID)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                        if((dataSnapshot.exists()) && (dataSnapshot.hasChild("name")) && (dataSnapshot.hasChild("image")))
                        {
                            String retrieveUsername = dataSnapshot.child("name").getValue().toString();
                            String retrieveStatus = dataSnapshot.child("Status").getValue().toString();
                            String retrieveImage = dataSnapshot.child("image").getValue().toString();

                            userName.setText(retrieveUsername);
                            userStatus.setText(retrieveStatus);
                            Picasso.get().load(retrieveImage).into(userProfileImage);

                        }
                        else if((dataSnapshot.exists()) && (dataSnapshot.hasChild("name")))
                        {
                            String retrieveUsername = dataSnapshot.child("name").getValue().toString();
                            String retrieveStatus = dataSnapshot.child("Status").getValue().toString();


                            userName.setText(retrieveUsername);
                            userStatus.setText(retrieveStatus);
                        }
                        else
                        {
                            userName.setVisibility(View.VISIBLE);

                            Toast.makeText(SettingsActivity.this,"please update your profile Informtaion...",Toast.LENGTH_SHORT).show();
                        }

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });


    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void Initializefields() {

        UpdateAccountSettings = findViewById(R.id.upadat_settings_button);
        GotoMaps = findViewById(R.id.go_to_maps_button);
        userName = findViewById(R.id.set_user_name);
        userStatus = findViewById(R.id.set_profile_status);
        userProfileImage = findViewById(R.id.set_profile_image);
        loadingBar = new ProgressDialog(this);

        SettingsToolbar = (Toolbar) findViewById(R.id.settings_toolbar);
        setSupportActionBar(SettingsToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowCustomEnabled(true);
        getSupportActionBar().setTitle("Account Settings");
    }

    private void SendUserToMainActivity() {

        Intent MainIntent = new Intent(SettingsActivity.this,MainActivity.class);
        MainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(MainIntent);
        finish();
    }
    private void GotoMapsActivity()
    {

         Intent intent = new Intent(SettingsActivity.this,MapsActivity.class);
         startActivity(intent);


    }




}
