package com.example.event_handler;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;

public class ResetPasswordActivity extends AppCompatActivity {

    EditText Email;
    Button btnNewPass;
    FirebaseAuth firebaseAuth;
    boolean sent = false;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reset_password);
        Email = (EditText) findViewById(R.id.editTextEmail);
        btnNewPass = (Button) findViewById(R.id.btnResetPassword);

        firebaseAuth = FirebaseAuth.getInstance();

        btnNewPass.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = Email.getText().toString();

                if (TextUtils.isEmpty(email)) {
                    Toast.makeText(getApplicationContext(), "Molimo unesite email adresu", Toast.LENGTH_SHORT).show();
                    return;
                }
                if(!sent){
                    firebaseAuth.sendPasswordResetEmail(email)
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {
                                        Toast.makeText(getApplicationContext(), "Link za resetovanje adrese je poslat", Toast.LENGTH_SHORT).show();
                                        sent = true;
                                    } else {
                                        Toast.makeText(getApplicationContext(), "Greska u slanju linka", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                }
                else{
                    Toast.makeText(getApplicationContext(), "Vec je poslat link za resetovanje", Toast.LENGTH_SHORT).show();
                }

            }
        });

    }
}