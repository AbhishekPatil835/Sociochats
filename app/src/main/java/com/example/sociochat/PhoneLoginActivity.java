package com.example.sociochat;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.sociochat.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;
import com.hbb20.CountryCodePicker;

import java.util.concurrent.TimeUnit;

public class PhoneLoginActivity extends AppCompatActivity {

    private Button SendVerificationCodeButton,VerifyButton;
    private EditText InputPhoneNumber,InputVerificationCode;
    private PhoneAuthProvider.OnVerificationStateChangedCallbacks callbacks;
    private String mVerificationId;
    private PhoneAuthProvider.ForceResendingToken mResendToken;
    private ProgressDialog loadingBar;
    private FirebaseAuth mAuth;
    private String deviceToken,currentUserID;
    private DatabaseReference UserRef;
    private CountryCodePicker countryCodePicker;


    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_phone_login);

        mAuth = FirebaseAuth.getInstance();
        UserRef = FirebaseDatabase.getInstance().getReference().child("Users");

        countryCodePicker = findViewById(R.id.ccp);
        SendVerificationCodeButton = findViewById(R.id.send_ver_code_button);
        VerifyButton = findViewById(R.id.verify_button);
        InputPhoneNumber = findViewById(R.id.phone_number_input);
        InputVerificationCode = findViewById(R.id.verification_code_input);
        loadingBar = new ProgressDialog(this);

        SendVerificationCodeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {


                  String phoneNumber = (countryCodePicker.getSelectedCountryCodeWithPlus().toString() + InputPhoneNumber.getText().toString());

                  if(TextUtils.isEmpty(phoneNumber))
                  {
                      Toast.makeText(PhoneLoginActivity.this, "Please Enter you phone number first..", Toast.LENGTH_SHORT).show();

                  }
                  else
                  {
                      loadingBar.setTitle("Phone Verification");
                      loadingBar.setMessage("Plaese wait, while we are authentication you phone...");
                      loadingBar.setCanceledOnTouchOutside(false);
                      loadingBar.show();

                      PhoneAuthProvider.getInstance().verifyPhoneNumber(
                              phoneNumber,        // Phone number to verify
                              60,                 // Timeout duration
                              TimeUnit.SECONDS,   // Unit of timeout
                              PhoneLoginActivity.this,               // Activity (for callback binding)
                              callbacks);        // OnVerificationStateChangedCallbacks
                  }
            }
        });

        VerifyButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                SendVerificationCodeButton.setVisibility(View.INVISIBLE);
                InputPhoneNumber.setVisibility(View.INVISIBLE);

               String verificationCode = InputVerificationCode.getText().toString();

               if(TextUtils.isEmpty(verificationCode))
               {
                   Toast.makeText(PhoneLoginActivity.this, "Please write first", Toast.LENGTH_SHORT).show();


               }
               else
               {
                   loadingBar.setTitle("Verification Code");
                   loadingBar.setMessage("Plaese wait, while we are Verifying...");
                   loadingBar.setCanceledOnTouchOutside(false);
                   loadingBar.show();

                   PhoneAuthCredential credential = PhoneAuthProvider.getCredential(mVerificationId, verificationCode);
                   signInWithPhoneAuthCredential(credential);
               }
            }
        });

         callbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
             @Override
             public void onVerificationCompleted(PhoneAuthCredential phoneAuthCredential)
             {
                    signInWithPhoneAuthCredential(phoneAuthCredential);
             }

             @Override
             public void onVerificationFailed(FirebaseException e)
             {
                 loadingBar.dismiss();
                 Toast.makeText(PhoneLoginActivity.this, "Invalid Phone number,Plaese Enter the correct phone number with your country code..", Toast.LENGTH_SHORT).show();

                 SendVerificationCodeButton.setVisibility(View.VISIBLE);
                 InputPhoneNumber.setVisibility(View.VISIBLE);

                 VerifyButton.setVisibility(View.INVISIBLE);
                 InputVerificationCode.setVisibility(View.INVISIBLE);

             }

             public void onCodeSent(@NonNull String verificationId,
                                    @NonNull PhoneAuthProvider.ForceResendingToken token) {


                 mVerificationId = verificationId;
                 mResendToken = token;

                 loadingBar.dismiss();
                 Toast.makeText(PhoneLoginActivity.this, "Code has been sent Successfully", Toast.LENGTH_SHORT).show();
                 SendVerificationCodeButton.setVisibility(View.INVISIBLE);
                 InputPhoneNumber.setVisibility(View.INVISIBLE);

                 VerifyButton.setVisibility(View.VISIBLE);
                 InputVerificationCode.setVisibility(View.VISIBLE);
             }
         };
    }

    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful())
                        {

                          currentUserID = mAuth.getCurrentUser().getUid();
                            FirebaseInstanceId.getInstance().getInstanceId().addOnSuccessListener(new OnSuccessListener<InstanceIdResult>()
                            {
                                        @Override
                                        public void onSuccess(InstanceIdResult instanceIdResult) {

                                    deviceToken = instanceIdResult.getToken();
                                            UserRef.child(currentUserID).child("device_token")
                                                    .setValue(deviceToken)
                                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                        @Override
                                                        public void onComplete(@NonNull Task<Void> task)
                                                        {
                                                            if (task.isSuccessful())
                                                            {
                                                                loadingBar.dismiss();
                                                                Toast.makeText(PhoneLoginActivity.this, "Congratulations you logged in successfully...", Toast.LENGTH_SHORT).show();
                                                                SendUserToMainActivity();

                                                            }

                                                        }
                                                    });


                                        }
                            });


                        }
                        else
                        {
                            String message = task.getException().toString();
                            Toast.makeText(PhoneLoginActivity.this, "Error: "+message, Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void SendUserToMainActivity()
    {
        Intent mainIntent = new Intent(PhoneLoginActivity.this,MainActivity.class);
        startActivity(mainIntent);
        finish();
    }
}
