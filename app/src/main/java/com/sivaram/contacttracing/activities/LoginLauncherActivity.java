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
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ProgressBar;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.sivaram.contacttracing.ContactTracingApplication;
import com.sivaram.contacttracing.R;
import com.sivaram.contacttracing.sharedpref.SharedPref;
import com.sivaram.contacttracing.utils.Constants;

import org.jetbrains.annotations.NotNull;

import static android.Manifest.permission.ACCESS_FINE_LOCATION;
import static android.Manifest.permission.READ_PHONE_STATE;
import static com.sivaram.contacttracing.ContactTracingApplication.toastMessage;
import static com.sivaram.contacttracing.utils.Constants.PERMISSION_REQUEST_CODE;

public class LoginLauncherActivity extends AppCompatActivity {

    EditText edtTxtPhoneNumber,edtTxtPassword;
    Button btnLogin,btnClicktoSignup;
    ProgressBar loginProgressBar;
    CheckBox mCheckBox;
    String sessionAutoLogin = Constants.NO;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        edtTxtPhoneNumber = findViewById(R.id.login_editTextUserPhone);
        edtTxtPassword = findViewById(R.id.login_editTextNumberPassword);
        btnLogin = findViewById(R.id.btn_login);
        btnClicktoSignup = findViewById(R.id.btn_signup_log_activity);
        loginProgressBar = findViewById(R.id.login_progress_bar);
        mCheckBox = findViewById(R.id.checkBox);
    }

    @Override
    public void onResume()
    {
        super.onResume();
        //Checking permissions for the app to perform its functionality
        if (!checkPermission()) {
            requestPermission();
        }
        //Handle progress bar
        loginProgressBar.setVisibility(View.GONE);
        //Handle check box
        sessionAutoLogin = SharedPref.getStringParams(ContactTracingApplication.getInstance(),Constants.SESSION_AUTO_LOGIN,Constants.NO);
        if(sessionAutoLogin.equals(Constants.YES)){
            logUserPhone = SharedPref.getStringParams(ContactTracingApplication.getInstance(),Constants.SESSION_CONTACT,Constants.EMPTY);
            logPwd=SharedPref.getStringParams(ContactTracingApplication.getInstance(),Constants.SESSION_PASSWORD,Constants.EMPTY);
            edtTxtPhoneNumber.setText(logUserPhone);
            edtTxtPassword.setText(logPwd);
            loginValidation();
            mCheckBox.setChecked(true);
        }else{
            mCheckBox.setChecked(false);
        }

        mCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked){
                    SharedPref.setStringParams(ContactTracingApplication.getInstance(),Constants.SESSION_AUTO_LOGIN,Constants.YES);
                }else{
                    SharedPref.setStringParams(ContactTracingApplication.getInstance(),Constants.SESSION_AUTO_LOGIN,Constants.NO);
                }
            }
        });
        btnClicktoSignup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent signUpActivity = new Intent(ContactTracingApplication.getInstance(), SignUpActivity.class);
                startActivity(signUpActivity);
            }
        });
        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                logUserPhone = edtTxtPhoneNumber.getText().toString().trim();
                logPwd = edtTxtPassword.getText().toString().trim();
                loginValidation();
            }
        });
    }
    String logUserPhone,logPwd;

    //check logUserPhone and logPwd before calling this function
    private void loginValidation() {
        if(validateFields(logUserPhone,logPwd)){
            loginProgressBar.setVisibility(View.VISIBLE);
            //validate username and password with database
            Query query = FirebaseDatabase.getInstance().getReference("users").orderByChild("contact").equalTo(logUserPhone);
            query.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if(dataSnapshot.exists()){
                        String pwdFromDB = dataSnapshot.child(logUserPhone).child("password").getValue(String.class);
                        assert pwdFromDB != null;
                        if(pwdFromDB.equals(logPwd)){
                            String userNameFromDB = dataSnapshot.child(logUserPhone).child("username").getValue(String.class);
                            SharedPref.setStringParams(ContactTracingApplication.getInstance(), Constants.SESSION_CONTACT, logUserPhone);
                            SharedPref.setStringParams(ContactTracingApplication.getInstance(),Constants.SESSION_PASSWORD,logPwd);
                            SharedPref.setStringParams(ContactTracingApplication.getInstance(),Constants.SESSION_USERNAME,userNameFromDB);
                            gotoMainActivity();
                        }else{
                            edtTxtPassword.setError("Wrong password");
                            edtTxtPassword.requestFocus();
                        }
                    }else{
                        edtTxtPhoneNumber.setError("User doesn't exists");
                        edtTxtPhoneNumber.requestFocus();
                    }
                    loginProgressBar.setVisibility(View.GONE);
                }

                @Override
                public void onCancelled(@NotNull DatabaseError databaseError) {
                    Log.i(Constants.TAG,databaseError.getMessage());
                    ContactTracingApplication.toastMessage("Internal error occurred!");
                    loginProgressBar.setVisibility(View.INVISIBLE);
                }
            });
        }
    }

    private void gotoMainActivity() {
        Intent mainActivityIntent = new Intent(ContactTracingApplication.getInstance(), MainActivity.class);
        startActivity(mainActivityIntent);
    }

    private boolean validateFields(String username,String password) {
        boolean allFilled;
        if(username.isEmpty()){
            edtTxtPhoneNumber.setError("Please enter Phone Number");
            edtTxtPhoneNumber.requestFocus();
        }
        if(password.isEmpty()){
            edtTxtPassword.setError("Please enter password");
            edtTxtPassword.requestFocus();
        }
        allFilled= !username.isEmpty() && !password.isEmpty();
        return allFilled;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finishAffinity();
    }


    public boolean checkPermission() {
        int result = ContextCompat.checkSelfPermission(ContactTracingApplication.instance.getContext(), ACCESS_FINE_LOCATION);
        int result1 = ContextCompat.checkSelfPermission(ContactTracingApplication.instance.getContext(), READ_PHONE_STATE);
        return result == PackageManager.PERMISSION_GRANTED && result1 == PackageManager.PERMISSION_GRANTED;
    }

    private void requestPermission() {
        ActivityCompat.requestPermissions(this, new String[]{ACCESS_FINE_LOCATION, READ_PHONE_STATE}, PERMISSION_REQUEST_CODE);
    }
    /* This function is called when the user accepts or decline the permission.
   Request Code is used to check which permission called this function.
   This request code is provided when the user is prompt for permission.*/
    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_REQUEST_CODE:
                if (grantResults.length > 0) {

                    boolean locationAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    boolean readPhoneStateAccepted = grantResults[1] == PackageManager.PERMISSION_GRANTED;

                    if (locationAccepted && readPhoneStateAccepted)
                        toastMessage("Required permissions granted.");
                    else {
                        toastMessage("Please accept permission to continue!");
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            if (shouldShowRequestPermissionRationale(ACCESS_FINE_LOCATION)) {
                                showMessageOKCancel("You need to allow access to both the permissions",
                                        new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                                    requestPermissions(new String[]{ACCESS_FINE_LOCATION, READ_PHONE_STATE},
                                                            PERMISSION_REQUEST_CODE);
                                                }
                                            }
                                        });
                                return;
                            }
                        }
                    }
                }
                break;
        }
    }

    private void showMessageOKCancel(String message, DialogInterface.OnClickListener okListener) {
        new AlertDialog.Builder(ContactTracingApplication.getInstance())
                .setMessage(message)
                .setPositiveButton("OK", okListener)
                .setNegativeButton("Cancel", null)
                .create()
                .show();
    }
}