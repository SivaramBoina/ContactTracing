package com.sivaram.contacttracing.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.app.AlertDialog;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.sivaram.contacttracing.ContactTracingApplication;
import com.sivaram.contacttracing.R;
import com.sivaram.contacttracing.helpers.User;
import com.sivaram.contacttracing.sharedpref.SharedPref;
import com.sivaram.contacttracing.utils.Constants;
import com.sivaram.contacttracing.utils.Utility;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import static android.Manifest.permission.ACCESS_FINE_LOCATION;
import static android.Manifest.permission.READ_PHONE_STATE;


public class SignUpActivity extends AppCompatActivity {

    Button btnSignUp,btnLogin;
    EditText edtTxtUsername,edtTxtPassword,edtTxtPhone;
    ProgressBar signUpProgressBar;
    String username= Constants.EMPTY,password=Constants.EMPTY,phonenumber=Constants.EMPTY;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup_launcher);
        edtTxtUsername = findViewById(R.id.editTextUsername);
        edtTxtPassword = findViewById(R.id.editTextPassword);
        edtTxtPhone = findViewById(R.id.editTextPhone);
        btnSignUp = findViewById(R.id.button_signup);
        btnLogin = findViewById(R.id.btn_login_signup_activity);
        signUpProgressBar = findViewById(R.id.signup_progress_bar);
    }

    @Override
    protected void onResume() {
        super.onResume();
        signUpProgressBar.setVisibility(View.INVISIBLE);
        Utility.removeSessionVariables();
        edtTxtPhone.setText(phonenumber);
        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goToLoginActivity();
            }
        });
        btnSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signUpProgressBar.setVisibility(View.VISIBLE);
                username = edtTxtUsername.getText().toString().trim();
                password = edtTxtPassword.getText().toString().trim();
                phonenumber = edtTxtPhone.getText().toString().trim();
                if(validateFields(username,password,phonenumber)){
                    Query query = FirebaseDatabase.getInstance().getReference("users").orderByChild("contact").equalTo(phonenumber);
                    query.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            if (dataSnapshot.getChildrenCount() > 0) {
                                edtTxtPhone.setError("User with this contact already exists");
                                edtTxtPhone.requestFocus();
                                ContactTracingApplication.toastMessage("User already exists!");
                            }else{
                                //getting a unique id using push().getKey() method
                                //it will create a unique id and we will use it as the Primary Key for our Artist
                                //String id = databaseUsers.push().getKey();
                                //creating an Artist Object
                                Date time =new Date();
                                SimpleDateFormat dt = new SimpleDateFormat("dd-MM-yyyy HH:mm", Locale.US);
                                String stringDate = dt.format(time);
                                final User user = new User(username,password,phonenumber,stringDate);
                                //Saving the Artist
                                DatabaseReference databaseUsers = FirebaseDatabase.getInstance().getReference("users");
                                databaseUsers.child(phonenumber).setValue(user).addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        ContactTracingApplication.toastMessage("SignUp Successful!");
                                        SharedPref.setStringParams(ContactTracingApplication.getInstance(),Constants.SESSION_CONTACT,phonenumber);
                                        SharedPref.setStringParams(ContactTracingApplication.getInstance(),Constants.SESSION_USERNAME,username);
                                        SharedPref.setStringParams(ContactTracingApplication.getInstance(),Constants.SESSION_USERNAME,password);
                                        gotoMainActivity();
                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        ContactTracingApplication.toastMessage("Turn on data and try again!");
                                        signUpProgressBar.setVisibility(View.INVISIBLE);
                                    }
                                });
                            }
                            signUpProgressBar.setVisibility(View.INVISIBLE);
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {
                            ContactTracingApplication.toastMessage(databaseError.getMessage());
                        }
                    });
                }else{
                    ContactTracingApplication.toastMessage("Please fill all fields to sign up!");
                }
            }
        });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finishAffinity();
    }

    private void gotoMainActivity() {
        Intent mainActivityIntent = new Intent(ContactTracingApplication.getInstance(), MainActivity.class);
        startActivity(mainActivityIntent);
    }

    private void goToLoginActivity(){
        Intent loginActivityIntent = new Intent(ContactTracingApplication.getInstance(), LoginLauncherActivity.class);
        startActivity(loginActivityIntent);
    }

    private boolean validateFields(String locUsername,String locPassword,String locPhonenumber) {
        boolean allFilled;
        if(locUsername.isEmpty()){
            edtTxtUsername.setError("Please fill username");
            edtTxtUsername.requestFocus();
        }
        if(locPassword.isEmpty()){
            edtTxtPassword.setError("Please fill Password");
            edtTxtPassword.requestFocus();
        }
        if(locPhonenumber.length() != 10){
            edtTxtPhone.setError("Please fill Phone Number");
            edtTxtPhone.requestFocus();
        }
        allFilled= !locUsername.isEmpty() && !locPassword.isEmpty() && locPhonenumber.length() == 10;
        return allFilled;
    }
}