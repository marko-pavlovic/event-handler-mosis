package com.example.event_handler;

import androidx.annotation.NonNull;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class LoginActivity extends Activity {

    private EditText emailEdit;
    private EditText passwordEdit;
    private TextView forgotPassTxt;
    private TextView noAccTxt;
    private Button loginBtn, forgotPassBtn, newAccBtn;
    private Context that=this;
    private FirebaseAuth auth;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //region Init
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        auth=FirebaseAuth.getInstance();
        emailEdit=findViewById(R.id.emailEditTxt);
        passwordEdit=findViewById(R.id.passEditTxt);
        forgotPassBtn = findViewById(R.id.newPasswordButton);///////to do ubaci event za novi pass
        newAccBtn = findViewById(R.id.RegisterButton);
        loginBtn=findViewById(R.id.loginButton);
        //endregion

        //region LoginBtn Listener
        loginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                auth.signInWithEmailAndPassword(emailEdit.getText().toString(),passwordEdit.getText().toString())
                        .addOnCompleteListener(LoginActivity.this, new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if (task.isSuccessful()){
                                    that.startActivity(new Intent(that,MainActivity.class));
                                    finish();
                                }
                                else {
                                    Toast.makeText(that, "Email ili lozinka pogresni", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
            }
        });
        //endregion

        loginBtn.setEnabled(false);

        //region TextChange Listeners
        emailEdit.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if(emailEdit.getText().toString().isEmpty() || passwordEdit.getText().toString().isEmpty()){
                    loginBtn.setEnabled(false);
                }
                else {
                    loginBtn.setEnabled(true);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        passwordEdit.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if(emailEdit.getText().toString().isEmpty() || passwordEdit.getText().toString().isEmpty()){
                    loginBtn.setEnabled(false);
                }
                else {
                    loginBtn.setEnabled(true);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        //endregion

        newAccBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                that.startActivity(new Intent(that, CreateAccountActivity.class));
            }
        });

        forgotPassBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                that.startActivity(new Intent(that, ResetPasswordActivity.class));
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser user=auth.getCurrentUser();
        if(user!=null){
            startActivity(new Intent(this,MainActivity.class));
            finish();
        }
    }
}
