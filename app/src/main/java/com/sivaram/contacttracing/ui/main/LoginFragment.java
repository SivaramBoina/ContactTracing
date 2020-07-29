package com.sivaram.contacttracing.ui.main;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.sivaram.contacttracing.Constants;
import com.sivaram.contacttracing.ContactTracingApplication;
import com.sivaram.contacttracing.MainActivity;
import com.sivaram.contacttracing.R;
import com.sivaram.contacttracing.SharedPref;

import static com.sivaram.contacttracing.ContactTracingApplication.toastMessage;

public class LoginFragment extends Fragment {

    View v;
    EditText edtTxtLoginUser,edtTxtPassword;
    Button btnLogin;
    String logUser= Constants.EMPTY,logPwd=Constants.EMPTY;

    public static LoginFragment newInstance() {
            return new LoginFragment();
    }
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        v=inflater.inflate(R.layout.login_fragment, container, false);
        edtTxtLoginUser = v.findViewById(R.id.login_editTextUserPhone);
        edtTxtPassword = v.findViewById(R.id.login_editTextNumberPassword);
        btnLogin = v.findViewById(R.id.btn_login);

        edtTxtLoginUser.addTextChangedListener(usrEdtTxtListener);
        edtTxtPassword.addTextChangedListener(pwdEdtTxtListener);

        return v;
    }

    @Override
    public void onResume()
    {
        super.onResume();
        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(validateFields()){
                    if(validate()){
                        Intent mainActivityIntent = new Intent(getContext(), MainActivity.class);
                        startActivity(mainActivityIntent);
                    }else{
                        toastMessage("Please enter correct credentials");
                    }
                }else{
                    toastMessage("Please fill all fields to login");
                }

            }
        });
    }

    private boolean validateFields() {
        boolean allFilled;
        allFilled= !logUser.isEmpty() && !logPwd.isEmpty();
        return allFilled;
    }
    private boolean validate() {
        boolean isValidUser;
        String savedUserName = SharedPref.getStringParams(ContactTracingApplication.getInstance(), Constants.USERNAME, Constants.EMPTY);
        String savedPhoneNumber = SharedPref.getStringParams(ContactTracingApplication.getInstance(),Constants.PHONE,Constants.EMPTY);
        String savedPassword = SharedPref.getStringParams(ContactTracingApplication.getInstance(),Constants.PASSWORD,Constants.EMPTY);
        isValidUser = (savedPhoneNumber.equals(logUser)) && savedPassword.equals(logPwd);
        return isValidUser;
    }

    private TextWatcher usrEdtTxtListener = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {

        }

        @Override
        public void afterTextChanged(Editable s) {
            logUser=s.toString();
        }
    };

    private TextWatcher pwdEdtTxtListener = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {

        }

        @Override
        public void afterTextChanged(Editable s) {
            logPwd=s.toString();
        }
    };
}