package com.example.hxtruong.grabbikeapp.authentication;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;

import com.example.hxtruong.grabbikeapp.R;

import core.Definition;
import core.helper.MyHelper;


@SuppressLint("ValidFragment")
public class SignupFragment extends Fragment implements View.OnClickListener {
    private Authentication authenticationInstance;
    protected View mViewInstance;
    protected EditText edEmail, edPassword, edPasswordConfirm;
    protected Button btnRegister;
    protected TextView tvTerm;
    protected CheckBox ckbAgreeTerm;
    protected String strPassword, strEmail, strConfirmPassword;

    boolean checkEmail = false, checkPassword = false, checkConfimPassword = false, checkPolicy = false;
    @SuppressLint("ValidFragment")
    public SignupFragment(Authentication authentication) {
        this.authenticationInstance = authentication;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        this.mViewInstance =   inflater.inflate(R.layout.signup_fragment, container, false);
        return mViewInstance;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        registerViews();
        handleViewsOnClickEvent();
        handleEmailEdittextEvents();
        handlePasswordEdittextEvents();
        handleConfirmPasswordEdittextEvents();
    }

    private void handleViewsOnClickEvent() {
        btnRegister.setEnabled(false);
        btnRegister.setOnClickListener(this);
        ckbAgreeTerm.setOnClickListener(this);
        tvTerm.setOnClickListener(this);
    }

    private void handleConfirmPasswordEdittextEvents() {
        edPasswordConfirm.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String strPassConfirm = String.valueOf(edPasswordConfirm.getText()).trim();
                String strPass = String.valueOf(edPassword.getText()).trim();
                if(strPass.length() >= Definition.MINIMUM_PASSWORD_LENGTH && strPass.compareTo(strPassConfirm)==0) {
                    checkConfimPassword = true;
                    edPasswordConfirm.setCompoundDrawablesWithIntrinsicBounds(R.drawable.icon_password,0, 0,0);
                }
                else{
                    checkConfimPassword = false;
                    edPasswordConfirm.setCompoundDrawablesWithIntrinsicBounds(R.drawable.icon_password,0, R.drawable.icon_alert,0);

                }

                changeRegisterButtonState();

            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
        edPasswordConfirm.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if(!hasFocus && !checkConfimPassword )
                        MyHelper.toast(getContext(), "Confirm password is not matched.");
            }
        });
    }

    private void handlePasswordEdittextEvents() {
        edPassword.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String strPass = String.valueOf(edPassword.getText()).trim();
                if(strPass.length() >= Definition.MINIMUM_PASSWORD_LENGTH) {
                    checkPassword = true;
                    edPassword.setCompoundDrawablesWithIntrinsicBounds(R.drawable.icon_password,0, 0,0);
                }
                else{
                    checkPassword = false;
                    edPassword.setCompoundDrawablesWithIntrinsicBounds(R.drawable.icon_password,0, R.drawable.icon_alert,0);

                }

                changeRegisterButtonState();

            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
        edPassword.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if(!hasFocus && !checkPassword)
                    MyHelper.toast(getContext(), "Password is at least "+ String.valueOf(Definition.MINIMUM_PASSWORD_LENGTH)+" characters");
            }
        });
    }

    private void handleEmailEdittextEvents() {
        edEmail.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String strUser = String.valueOf(edEmail.getText()).trim();
                if(strUser.matches(Definition.EMAIL_REGEX)) {
                    checkEmail = true;
                    edEmail.setCompoundDrawablesWithIntrinsicBounds(R.drawable.icon_user,0, 0,0);
                }
                else {
                    edEmail.setCompoundDrawablesWithIntrinsicBounds(R.drawable.icon_user,0, R.drawable.icon_alert,0);
                    checkEmail = false;
                }

                changeRegisterButtonState();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
        edEmail.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if(!hasFocus && !checkEmail)
                    MyHelper.toast(getContext(), "Email is invalid.");
            }
        });
    }

    private void registerViews() {
        edEmail = mViewInstance.findViewById(R.id.edSignUpEmail);
        edPassword = mViewInstance.findViewById(R.id.edSignUpPassword);
        edPasswordConfirm = mViewInstance.findViewById(R.id.edSignUpPasswordConfirm);
        tvTerm = mViewInstance.findViewById(R.id.tvTermPolicy);
        ckbAgreeTerm = mViewInstance.findViewById(R.id.ckbAgreeTerm);
        btnRegister = mViewInstance.findViewById(R.id.btnRegister);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.ckbAgreeTerm:
                checkPolicy = ckbAgreeTerm.isChecked();
                changeRegisterButtonState();
                break;
            case R.id.btnRegister:
                //do something here
                strEmail = edEmail.getText().toString().trim();
                strPassword = edPassword.getText().toString().trim();
                strConfirmPassword = edPasswordConfirm.getText().toString().trim();
                authenticationInstance.createUserWithEmailAndPasswor(strEmail, strPassword);
                break;
        }
    }

    private void changeRegisterButtonState() {
        if(checkPolicy && checkEmail && checkPassword && checkConfimPassword)
            btnRegister.setEnabled(true);
        else
            btnRegister.setEnabled(false);
    }

    void clearSignFields() {
        edEmail.setText("");
        edPassword.setText("");
        edPasswordConfirm.setText("");
    }
}
