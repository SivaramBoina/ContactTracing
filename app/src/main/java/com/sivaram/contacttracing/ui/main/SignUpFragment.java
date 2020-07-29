package com.sivaram.contacttracing.ui.main;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.telephony.TelephonyManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import com.sivaram.contacttracing.Constants;
import com.sivaram.contacttracing.ContactTracingApplication;
import com.sivaram.contacttracing.LauncherActivity;
import com.sivaram.contacttracing.MainActivity;
import com.sivaram.contacttracing.R;
import com.sivaram.contacttracing.SharedPref;

public class SignUpFragment extends Fragment {

    private MainViewModel mViewModel;


    public static SignUpFragment newInstance() {
        return new SignUpFragment();
    }

    Button btnSignUp;
    EditText edtTxtUsername,edtTxtPassword,edtTxtPhone;
    String username= Constants.EMPTY,password=Constants.EMPTY,phonenumber=Constants.EMPTY;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.signup_fragment, container, false);
        edtTxtUsername = v.findViewById(R.id.editTextUsername);
        edtTxtPassword = v.findViewById(R.id.editTextPassword);
        edtTxtPhone = v.findViewById(R.id.editTextPhone);
        btnSignUp = v.findViewById(R.id.button_signup);

        edtTxtUsername.addTextChangedListener(usernameListener);
        edtTxtPassword.addTextChangedListener(passwordListener);
        edtTxtPhone.addTextChangedListener(phoneListener);

        return v;
    }

    @Override
    public void onResume() {
        super.onResume();
        phonenumber=getTelephoneNumber();
        if(phonenumber.length()>10){
            phonenumber=phonenumber.substring(phonenumber.length()-10);
        }
        edtTxtPhone.setText(phonenumber);
        btnSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(validateFields()){
                    addSharedPreferences();
                    Intent mainActivityIntent = new Intent(getContext(), MainActivity.class);
                    startActivity(mainActivityIntent);
                }else{
                    ContactTracingApplication.toastMessage("Please fill all fields to sign up!");
                }
            }
        });

    }

    private void addSharedPreferences() {
        SharedPref.setStringParams(ContactTracingApplication.getInstance(),Constants.USERNAME,username.replace(" ","").toLowerCase());
        SharedPref.setStringParams(ContactTracingApplication.getInstance(),Constants.PASSWORD,password);
        SharedPref.setStringParams(ContactTracingApplication.getInstance(),Constants.PHONE,phonenumber);
        SharedPref.setStringParams(ContactTracingApplication.getInstance(),Constants.SIGNUP,Constants.SIGNUP_YES);
    }

    private boolean validateFields() {
        boolean allFilled;
        allFilled= !username.isEmpty() && !password.isEmpty() && phonenumber.length() == 10;
        return allFilled;
    }

    private TextWatcher usernameListener = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {

        }

        @Override
        public void afterTextChanged(Editable s) {
            username=s.toString();
        }
    };

    private TextWatcher passwordListener = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {

        }

        @Override
        public void afterTextChanged(Editable s) {
            password=s.toString();
        }
    };
    private TextWatcher phoneListener = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {

        }

        @Override
        public void afterTextChanged(Editable s) {
            phonenumber=s.toString();
        }
    };

    public String getTelephoneNumber() {
        String phoneNumber;
        TelephonyManager tMgr = (TelephonyManager) ContactTracingApplication.instance.getContext().getSystemService(Context.TELEPHONY_SERVICE);
        if(new LauncherActivity().checkPermission()){
            phoneNumber = tMgr.getLine1Number();
        }else{
            phoneNumber=Constants.EMPTY;
        }
        return phoneNumber;
    }
}