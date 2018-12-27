package com.example.hxtruong.grabbikeapp.authentication;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.example.hxtruong.grabbikeapp.R;

import core.Definition;
import core.helper.MyHelper;

@SuppressLint("ValidFragment")
public class LoginFragment extends Fragment implements View.OnClickListener {
    View viewInstance;
    Authentication authenticationInstance;
    TextView tvForgotPassword;
    TextView tvTerm;
    Button btnLogin;
    RelativeLayout relLoginWithFacebook, relLoginWithGoogle;
    EditText edUsername, edPassword;
    String strUsername, strPassword;

    Boolean checkUsername = false, checkPassword = false;
    private boolean checkLoginWithThirdOrg = false;

    @SuppressLint("ValidFragment")
    public LoginFragment(Authentication authentication) {
        this.authenticationInstance = authentication;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        viewInstance = inflater.inflate(R.layout.login_fragment, container, false);
        return viewInstance;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        registerViews();
        handleEmailEdittextEvents();
        handlePasswordEdittextEvents();
        handleViewsOnClickListener();
    }

    private void handleViewsOnClickListener() {
        tvForgotPassword.setOnClickListener(this);
        tvTerm.setOnClickListener(this);

        tvForgotPassword.setMovementMethod(LinkMovementMethod.getInstance());
        tvTerm.setMovementMethod(LinkMovementMethod.getInstance());

        btnLogin.setOnClickListener(this);
        relLoginWithGoogle.setOnClickListener(this);
        relLoginWithFacebook.setOnClickListener(this);
    }

    private void handlePasswordEdittextEvents() {
        edPassword = viewInstance.findViewById(R.id.edLoginPassword);
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
                changeLoginButtonState();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        edPassword.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if(!hasFocus && !checkPassword && !checkLoginWithThirdOrg)
                    MyHelper.toast(getContext(), "Password is at least "+ String.valueOf(Definition.MINIMUM_PASSWORD_LENGTH)+" characters");
            }
        });
    }

    private void handleEmailEdittextEvents() {
        edUsername.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String strUser = String.valueOf(edUsername.getText()).trim();
                if(strUser.matches(Definition.EMAIL_REGEX)) {
                    checkUsername = true;
                    edUsername.setCompoundDrawablesWithIntrinsicBounds(R.drawable.icon_user,0, 0,0);
                }
                else {
                    edUsername.setCompoundDrawablesWithIntrinsicBounds(R.drawable.icon_user,0, R.drawable.icon_alert,0);
                    checkUsername = false;
                }

                changeLoginButtonState();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        edUsername.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if(!hasFocus && !checkUsername && !checkLoginWithThirdOrg)
                    MyHelper.toast(getContext(), "Email is invalid.");
            }
        });
    }

    private void registerViews() {
        tvForgotPassword =  viewInstance.findViewById(R.id.tvForgotPassword);
        tvTerm = viewInstance.findViewById(R.id.tvTermPolicy);
        btnLogin = viewInstance.findViewById(R.id.btnLogin);
        btnLogin.setEnabled(false);

        edUsername = viewInstance.findViewById(R.id.edLoginUsername);
        relLoginWithGoogle = viewInstance.findViewById(R.id.relLoginWithGoogle);
        relLoginWithFacebook = viewInstance.findViewById(R.id.relLoginWithFacebook);
    }

    private void changeLoginButtonState() {
        if(checkUsername && checkPassword)
            btnLogin.setEnabled(true);
        else
            btnLogin.setEnabled(false);
    }

    @Override
    public void onClick(View v) {

        switch (v.getId()){
            case R.id.btnLogin:
                strUsername = edUsername.getText().toString().trim();
                strPassword = edPassword.getText().toString().trim();
                authenticationInstance.loginWithEmailAndPassword(strUsername, strPassword);
                break;
            case R.id.relLoginWithFacebook:
                //checkLoginWithThirdOrg = true;
                //authenticationInstance.loginWIthFacebook();
                MyHelper.toast(getContext(), "Facebook Authentication is unavailable");
                break;
            case R.id.relLoginWithGoogle:
                checkLoginWithThirdOrg = true;
                authenticationInstance.loginWithGoogle();
                break;
            case R.id.tvForgotPassword:
                Intent intentResetPassword = new Intent(getContext(), ResetPasswordActivity.class);
                startActivity(intentResetPassword);
                break;
            case R.id.tvTermPolicy:
                MyHelper.toast(getContext(), "This should open web browser to see term policy!");
                break;

            case R.id.edLoginUsername: case R.id.edLoginPassword:
                checkLoginWithThirdOrg = false;
        }
    }
}