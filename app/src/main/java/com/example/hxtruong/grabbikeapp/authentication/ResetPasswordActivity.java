package com.example.hxtruong.grabbikeapp.authentication;

import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;

import com.example.hxtruong.grabbikeapp.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;

import core.Definition;
import core.helper.MyHelper;

public class ResetPasswordActivity extends AppCompatActivity {

    EditText edEmail;
    Button btnResetPassword, btnBack;
    ProgressBar progressBar;
    FirebaseAuth auth;
    private boolean checkEmail = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reset_password);

        registerViews();

        auth = FirebaseAuth.getInstance();

        handleViewsClickEvents();
        handleEmailEditextEvent();
    }

    private void handleEmailEditextEvent() {
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

                changeResetEmailButtonState();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
        edEmail.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if(!hasFocus && !checkEmail)
                    MyHelper.toast(getApplicationContext(), "Email is invalid.");
            }
        });
    }

    private void changeResetEmailButtonState() {
        if(checkEmail)
            btnResetPassword.setEnabled(true);
        else
            btnResetPassword.setEnabled(false);
    }

    private void handleViewsClickEvents() {
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        btnResetPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                progressBar.setVisibility(View.VISIBLE);
                String strEmail = edEmail.getText().toString().trim();
                auth.sendPasswordResetEmail(strEmail)
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if(!task.isSuccessful()){
                                    MyHelper.toast(getApplicationContext(), "Failed to send reset email. Check your Email!");
                                }
                                else {
                                    MyHelper.toast(getApplicationContext(), "We have sent you instructions to reset password!");
                                }
                            }
                        });
                progressBar.setVisibility(View.GONE);
            }
        });
    }

    private void registerViews() {
        edEmail = findViewById(R.id.edResetEmail);
        btnResetPassword = findViewById(R.id.btnResetPassword);
        btnBack = findViewById(R.id.btnBack);
        progressBar = findViewById(R.id.progressBarResetPassword);
        btnResetPassword.setEnabled(false);
        progressBar.setVisibility(View.GONE);
    }
}
